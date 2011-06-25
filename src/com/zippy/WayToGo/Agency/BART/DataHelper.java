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
package com.zippy.WayToGo.Agency.BART;

import com.zippy.WayToGo.Agency.BARTAgency;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.zippy.WayToGo.Agency.CommonDBHelper;
import com.zippy.WayToGo.Util.Direction;
import com.zippy.WayToGo.Util.Route;
import com.zippy.WayToGo.Util.Stop;
import java.util.ArrayList;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

/**
 *
 * @author alex
 */
public final class DataHelper extends CommonDBHelper {

    private final static String LOG_NAME = DataHelper.class.getCanonicalName();
    protected BARTAgency theAgency = null;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "bart_data.sqlite3";
    private static final String ROUTES_TABLE =
            "CREATE TABLE routes ( "
            + "name VARCHAR UNIQUE NOT NULL, "
            + "tag VARCHAR UNIQUE NOT NULL, "
            + "number INT UNIQUE NOT NULL,"
            + "id VARCHAR UNIQUE NOT NULL,"
            + "color VARCHAR NOT NULL, "
            + "start_station VARCHAR, "
            + "end_station VARCHAR);";
    private static final String STATIONS_TABLE =
            "CREATE TABLE stations ( "
            + "name VARCHAR UNIQUE NOT NULL, "
            + "tag VARCHAR UNIQUE NOT NULL,"
            + "address VARCHAR NOT NULL,"
            + "lat INTEGER NOT NULL,"
            + "lng INTEGER NOT NULL);";

    public DataHelper(final Context aContext, final BARTAgency anAgency) {
        super(aContext, DATABASE_NAME, null, DATABASE_VERSION);
        theAgency = anAgency;
    }

    @Override
    public void onCreate(final SQLiteDatabase aDatabase) {
        aDatabase.execSQL(ROUTES_TABLE);
        aDatabase.execSQL(STATIONS_TABLE);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase aDatabase, final int anOldVersion, final int aNewVersion) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public final void addStation(final ContentValues aStation) {
        addRowToTable("stations", aStation);
    }

    public final void addRoute(final ContentValues aRoute) {
        addRowToTable("routes", aRoute);
    }

    public final int getNumberOfStations() {
        checkReadDb();
        final String[] columns = {"count(name)"};
        Cursor theCursor = theReadDB.query("stations", columns, null, null, null, null, null);
        int count = 0;

        if (theCursor.getCount() != 1) {
            // Error out because we should have gotten only one row by calling count()
            count = 0;
        }
        theCursor.moveToFirst();
        count = theCursor.getInt(0);
        theCursor.close();
        return count;
    }

    @Override
    public final ArrayList<Stop> getStops(final BoundingBoxE6 aBoundingBox) {
        checkReadDb();
        final Cursor theCursor;
        if (aBoundingBox != null) {
            final String[] theBindArgs = {
                String.valueOf(aBoundingBox.getLatSouthE6()),
                String.valueOf(aBoundingBox.getLatNorthE6()),
                String.valueOf(aBoundingBox.getLonWestE6()),
                String.valueOf(aBoundingBox.getLonEastE6())};

            theCursor = theReadDB.rawQuery("SELECT name,tag,address,lat,lng FROM stations WHERE (lat BETWEEN ? AND ?) AND (lng BETWEEN ? AND ?) ORDER BY name ASC", theBindArgs);
        } else {
            theCursor = theReadDB.rawQuery("SELECT name,tag,address,lat,lng FROM stations ORDER BY name ASC", null);
        }
        final ArrayList<Stop> ret = new ArrayList<Stop>(theCursor.getCount());
        theCursor.moveToFirst();
        while (theCursor.isAfterLast() == false) {
            ret.add(getStationFromCursor(theCursor));
            theCursor.moveToNext();
        }
        theCursor.close();

        return ret;
    }

    @Override
    public final Stop getStop(final String aStationTag) {
        checkReadDb();
        final Stop ret;
        String[] bindVars = {aStationTag};
        Cursor ourCursor = theReadDB.rawQuery(
                "SELECT name,tag,address,lat,lng FROM stations WHERE tag = ? LIMIT 1",
                bindVars);
        if (ourCursor.getCount() != 1) {
            ret = null;
        } else {
            ourCursor.moveToFirst();
            ret = getStationFromCursor(ourCursor);
        }
        ourCursor.close();
        return ret;
    }

    private Stop getStationFromCursor(final Cursor aCursor) {
        final int lat = aCursor.getInt(3);
        final int lng = aCursor.getInt(4);
        final GeoPoint ourPoint = new GeoPoint(lat, lng);
        final String ourStationName = aCursor.getString(0).replaceAll("/", " / ");
        return new Stop(ourPoint, ourStationName, aCursor.getString(1), aCursor.getString(2), theAgency.getClass());

    }

    @Override
    protected String getDBName() {
        return DATABASE_NAME;
    }

    /**
     * We return an empty route because BART trains aren't really identified
     * by route (which are either colors, private numbers, or the start/end station),
     * but are almost exclusively referred to by terminus.
     * @param aRouteTag
     * @return An empty route.
     */
    @Override
    protected Route getRouteFromTag(final String aRouteTag) {
        return new Route();
    }

    @Override
    protected final Direction getDirectionForTag(final String aDirectionTag) {
        //final String anAgencyClassName, final String aRouteTag, final String aTag, final String aTitle) {
        return new Direction(theAgency.getClass().getCanonicalName(), null, aDirectionTag, aDirectionTag);
    }

    @Override
    protected ArrayList<Route> getRoutes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected ArrayList<Direction> getDirections() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
