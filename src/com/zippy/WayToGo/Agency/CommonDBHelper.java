/*
 *  Copyright (C) 2011 Inferior Human Organs Software
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.zippy.WayToGo.Agency;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.CopyDBListener;
import com.zippy.WayToGo.Util.CopyDBTask;
import com.zippy.WayToGo.Util.Direction;
import com.zippy.WayToGo.Util.Route;
import com.zippy.WayToGo.Util.Stop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.osmdroid.util.BoundingBoxE6;

/**
 *
 * @author alex
 */
public abstract class CommonDBHelper extends SQLiteOpenHelper {

    private static final String LOG_NAME = CommonDBHelper.class.getCanonicalName();
    protected SQLiteDatabase theReadDB = null;
    protected SQLiteDatabase theWriteDB = null;
    
    protected CopyDBTask theCopyTask;

    protected CommonDBHelper(Context aContext, String aName, SQLiteDatabase.CursorFactory aFactory, int aVersion) {
        super(aContext, aName, aFactory, aVersion);
    }

    /**
     * Call this to close up the DB connections.
     */
    public void cleanUp() {
        if (theReadDB != null) {
            theReadDB.close();
        }

        if (theWriteDB != null) {
            theWriteDB.close();
        }
    }

    public void beginTransaction() {
        checkWriteDb();
        theWriteDB.beginTransaction();
    }

    public void endTransaction() {
        checkWriteDb();
        theWriteDB.setTransactionSuccessful();
        theWriteDB.endTransaction();
    }

    protected final void checkReadDb() {
        if (theReadDB == null || !theReadDB.isOpen()) {
            theReadDB = getReadableDatabase();
        }
    }

    protected final void checkWriteDb() {
        if (theWriteDB == null || !theWriteDB.isOpen()) {
            theWriteDB = getWritableDatabase();
        }
    }

    protected void addConstrainedRowToTable(final String theTable, final ContentValues theValues) {
        checkWriteDb();
        try {
            theWriteDB.insertOrThrow(theTable, null, theValues);
        } catch (SQLiteConstraintException theConstraint) {
            // Ignore
        }
    }

    protected synchronized void addRowToTable(final String theTable, final ContentValues theValues) {
        checkWriteDb();
        theWriteDB.insert(theTable, null, theValues);
    }

    protected static String slightlySanitize(final String anInputString) {
        Log.i(LOG_NAME, "Input is: " + anInputString);
        return anInputString.replace("-", "_").replace(" ", "_") + ".sqlite3";
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transferring bytestream.
     * http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/
     * */
    public final void copyDatabase(final CopyDBListener aListener) {
        //Log.d(LOG_NAME, "copyDataBase");
        this.close();
        if (checkDataBase()) {
            //Log.d(LOG_NAME, "Database already exists.");
            if (aListener != null) {
                aListener.copyingFinished();
            }
            return;
        }
        if (aListener != null) {
            aListener.copyingStarted();
        }

        int fileSize = 0;
        try {
            InputStream myInput = TheApp.getContext().getAssets().open("databases/" + getDBName());
            fileSize = myInput.available();
            myInput.close();
        } catch (IOException theEx) {
        }

        if (fileSize == 0) {
            if (aListener != null) {
                aListener.copyingFinished();
            }
            return;
        }

        theCopyTask = new CopyDBTask(TheApp.getContext(), aListener);
        //Log.d(LOG_NAME, "Calling copydbworktask");
        theCopyTask.execute(getDBName());
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    public boolean checkDataBase() {
        final String thePath = TheApp.getContext().getDatabasePath(getDBName()).getPath();
        Log.d(LOG_NAME, "checkDatabase(), looking for: " + thePath);
        return new File(thePath).exists();
    }


    // Stops
    abstract protected Stop getStop(final String aStopId);
    abstract protected ArrayList<Stop> getStops(final BoundingBoxE6 aBoundingBox);

    // Routes
    abstract protected Route getRouteFromTag(final String aRouteTag);
    abstract protected ArrayList<Route> getRoutes();

    // Directions
    abstract protected Direction getDirectionForTag(final String aDirectionTag);
    abstract protected ArrayList<Direction> getDirections();

    abstract protected String getDBName();
    
}
