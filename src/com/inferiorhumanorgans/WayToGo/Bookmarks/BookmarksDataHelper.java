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
package com.inferiorhumanorgans.WayToGo.Bookmarks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.inferiorhumanorgans.WayToGo.Agency.BaseAgency;
import com.inferiorhumanorgans.WayToGo.TheApp;
import com.inferiorhumanorgans.WayToGo.Util.Stop;
import com.inferiorhumanorgans.WayToGo.Util.StringPairList.StringPair;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alex
 */
public final class BookmarksDataHelper extends SQLiteOpenHelper {

    private static final String LOG_NAME = BookmarksDataHelper.class.getCanonicalName();
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "bookmarks.sqlite";
    private static final String BOOKMARKS_TABLE =
            "CREATE TABLE bookmarks ( "
            + "id INTEGER PRIMARY KEY, "
            + "agency VARCHAR NOT NULL, "
            + "stop_tag VARCHAR NOT NULL);";
    private static final String AGENCY_COL = "agency";
    private static final String STOP_COL = "stop_tag";
    protected SQLiteDatabase theReadDB = null;
    protected SQLiteDatabase theWriteDB = null;

    public BookmarksDataHelper(Context aContext) {
        super(aContext, DATABASE_NAME, null, DATABASE_VERSION);
        theWriteDB = this.getWritableDatabase();
        theReadDB = this.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(BOOKMARKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
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

    protected synchronized void addRowToTable(final String theTable, final ContentValues theValues) {
        checkWriteDb();
        theWriteDB.insert(theTable, null, theValues);
    }

    /**
     *
     * @return the number of bookmarks currently stored in the database.
     */
    public int getNumberOfBookmarks() {
        final Cursor theCursor = theReadDB.rawQuery("SELECT COUNT(id) FROM bookmarks", null);
        final int ret;
        if (theCursor.getCount() != 1) {
            ret = 0;
        } else {
            theCursor.moveToFirst();
            ret = theCursor.getInt(0);
        }
        theCursor.close();
        return ret;
    }

    /**
     *
     * @return an array of Bookmark objects from the database
     */
    public final ArrayList<Bookmark> getAllBookmarks() {
        checkReadDb();
        final Cursor ourCursor = theReadDB.rawQuery("SELECT id,agency,stop_tag FROM bookmarks", null);
        final ArrayList<Bookmark> ret = new ArrayList<Bookmark>();
        ourCursor.moveToFirst();
        while (ourCursor.isAfterLast() == false) {

            final int ourId = ourCursor.getInt(0);
            final String ourClassName = ourCursor.getString(1);
            final String ourStopTag = ourCursor.getString(2);

            final BaseAgency ourAgency = TheApp.theAgencies.get(ourClassName);

            final Stop ourStop = ourAgency.getStop(ourStopTag);
            ourCursor.moveToNext();

            // Oops we don't have that stop any longer.
            // DB glitch or should we prompt the user to look for another one?
            // Or silently delete it? Or just ignore?
            // Prompt the user to OTA update?
            if (ourStop == null) {
                continue;
            }
            final Bookmark theBookmark = new Bookmark(ourId, ourClassName, ourStop);
            theBookmark.setTheIntent(ourAgency.getPredictionIntentForStop(ourStop));
            ret.add(theBookmark);
        }

        ourCursor.close();
        SQLiteDatabase.releaseMemory();
        return ret;

    }

    public final void addBookmark(StringPair aBookmark) {
        final ContentValues theRow = new ContentValues(2);
        theRow.put(AGENCY_COL, aBookmark.first);
        theRow.put(STOP_COL, aBookmark.second);
        addRowToTable("bookmarks", theRow);
    }

    public final void deleteBookmark(Bookmark aBookmark) {
        checkWriteDb();
        theWriteDB.delete("bookmarks", "id = " + aBookmark.getTheId(), null);
        return;
    }
}
