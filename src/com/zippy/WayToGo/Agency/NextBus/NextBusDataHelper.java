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
package com.zippy.WayToGo.Agency.NextBus;

import com.zippy.WayToGo.Util.Direction;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Agency.CommonDBHelper;
import com.zippy.WayToGo.Util.Route;
import com.zippy.WayToGo.Util.Stop;
import java.util.ArrayList;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

/**
 * This should be turned into a proxy class so that we can use a timestamp
 * as the DB version and pass it in as needed. Also for handling stuff on SD.
 * @author alex
 */
public abstract class NextBusDataHelper extends CommonDBHelper {

    private static final String LOG_NAME = NextBusDataHelper.class.getCanonicalName();
    protected NextBusAgency theAgency = null;
    private ContentValues theRow;
    /**
     * The precompile tools set the version to one and omit the index to save
     * space.  By setting the version to 2 here, we've an easy way of creating
     * the extra index for the shrunken DBs.
     */
    private static final int DATABASE_VERSION = 2;
    private final Context theContext;
    private static final String DATABASE_NAME = "nextbus_data";
    private static final String ROUTES_TABLE =
            "CREATE TABLE routes ( "
            + "tag VARCHAR UNIQUE NOT NULL, "
            + "title VARCHAR NOT NULL, "
            + "lat_min INTEGER, "
            + "lat_max INTEGER, "
            + "lng_min INTEGER, "
            + "lng_max INTEGER, "
            + "color VARCHAR NOT NULL);";
    private static final String DIRECTIONS_TABLE =
            "CREATE TABLE directions ( "
            + "route_tag VARCHAR NOT NULL, "
            + "tag VARCHAR UNIQUE NOT NULL,"
            + "title VARCHAR NOT NULL, "
            + "visible BOOLEAN NOT NULL, "
            + "direction VARCHAR NOT NULL);";
    private static final String DIRECTIONS_STOPS_TABLE =
            "CREATE TABLE directions_stops ( "
            + "direction_tag VARCHAR NOT NULL, "
            + "stop_tag VARCHAR NOT NULL,"
            + "position INT NOT NULL);";
    private static final String STOPS_TABLE =
            "CREATE TABLE stops ( "
            + "tag VARCHAR UNIQUE NOT NULL, "
            + "stop_id INTEGER UNIQUE NOT NULL, "
            + "title VARCHAR NOT NULL,"
            + "lat INTEGER NOT NULL, "
            + "lng INTEGER NOT NULL);";
    private static final String ROUTES_STOPS_TABLE =
            "CREATE TABLE routes_stops ( "
            + "route_tag VARCHAR NOT NULL, "
            + "stop_tag VARCHAR NOT NULL);";

    private static final String EXTRA_INDEX_1 = "CREATE INDEX ds_dt_idx ON directions_stops(direction_tag);";
    private static final String TAG_COLUMN = "tag";
    private static final String TITLE_COLUMN = "title";
    private static final String LAT_COLUMN = "lat";
    private static final String LNG_COLUMN = "lng";

    public NextBusDataHelper(final Context context, final NextBusAgency anAgency) {
        super(TheApp.getContext(),
                TheApp.getDatabaseFileName(getDBName(anAgency)), null, DATABASE_VERSION);

        theContext = context;
        theAgency = anAgency;
        theRow = new ContentValues();
    }

    @Override
    public final void onCreate(SQLiteDatabase db) {
        // Routes
        db.execSQL(ROUTES_TABLE);
        db.execSQL(DIRECTIONS_TABLE);
        db.execSQL(ROUTES_STOPS_TABLE);

        // Create list of stops
        db.execSQL(STOPS_TABLE);
        db.execSQL(DIRECTIONS_STOPS_TABLE);

        db.execSQL(EXTRA_INDEX_1);
    }

    @Override
    public final void onUpgrade(SQLiteDatabase aDB, int anOldVersion, int aNewVersion) {
        if ((anOldVersion == 1) && (aNewVersion == 2)) {
            aDB.execSQL(EXTRA_INDEX_1);
        }
    }

    private static String getDBName(final NextBusAgency anAgency) {
        return DATABASE_NAME + "_" + slightlySanitize(anAgency.getNextBusName());
    }

    protected String getDBName() {
        return getDBName(theAgency);
    }

    /**
     * Get all stops that fit within a bounding box.
     * @param aBoundingBox
     * @return
     */
    @Override
    protected final ArrayList<Stop> getStops(final BoundingBoxE6 aBoundingBox) {
        checkReadDb();
        final Cursor theCursor;
        if (aBoundingBox != null) {
            final String[] theBindArgs = {
                String.valueOf(aBoundingBox.getLatSouthE6()),
                String.valueOf(aBoundingBox.getLatNorthE6()),
                String.valueOf(aBoundingBox.getLonWestE6()),
                String.valueOf(aBoundingBox.getLonEastE6())};
            theCursor = theReadDB.rawQuery("SELECT stop_id,title,lat,lng FROM stops WHERE (lat BETWEEN ? AND ?) AND (lng BETWEEN ? AND ?) ORDER BY title ASC", theBindArgs);
        } else {
            theCursor = theReadDB.rawQuery("SELECT stop_id,title,lat,lng FROM stops ORDER BY title ASC", null);
        }
        Log.d(LOG_NAME, "Done querying database.");
        Log.d(LOG_NAME, "Allocating arraylist.");
        final ArrayList<Stop> ret = new ArrayList<Stop>(theCursor.getCount());
        Log.d(LOG_NAME, "Done");
        theCursor.moveToFirst();

        while (!theCursor.isAfterLast()) {
            ret.add(getStopFromCursor(theCursor));
            theCursor.moveToNext();
        }
        theCursor.close();
        return ret;

    }

    protected final Stop getStop(final String aStopId) {
        checkReadDb();
        final String[] theBindArgs = {aStopId};
        final Cursor theCursor = theReadDB.rawQuery("SELECT stop_id,title,lat,lng FROM stops WHERE stop_id = ? LIMIT 1", theBindArgs);

        final Stop theStop;
        if (theCursor.getCount() != 1) {
            theStop = null;
        } else {
            theCursor.moveToFirst();
            theStop = getStopFromCursor(theCursor);
        }
        theCursor.close();

        return theStop;
    }

    protected final ArrayList<Stop> getStopsForDirectionTag(final String aDirectionTag) {
        checkReadDb();
        final ArrayList<Stop> ret = new ArrayList<Stop>();
        final String theQuery = "SELECT stop_id,title,lat,lng FROM stops JOIN directions_stops AS ds ON ds.stop_tag = stops.tag WHERE ds.direction_tag = ?";
        final String[] theBindArgs = {aDirectionTag};
        Cursor theCursor = theReadDB.rawQuery(theQuery, theBindArgs);
        theCursor.moveToFirst();
        while (theCursor.isAfterLast() == false) {
            ret.add(getStopFromCursor(theCursor));
            theCursor.moveToNext();
        }
        theCursor.close();

        return ret;
    }

    protected final Stop getStopAtIndexOnDirection(final String aDirectionTag, final int anIndex) {
        checkReadDb();

        final String theQuery = "SELECT stop_id,title,lat,lng FROM stops JOIN directions_stops AS ds ON stops.tag = ds.stop_tag WHERE ds.direction_tag = ? AND ds.position = ? LIMIT 1;";
        final String[] theBindArgs = {aDirectionTag, String.valueOf(anIndex)};
        final Cursor theCursor = theReadDB.rawQuery(theQuery, theBindArgs);

        final Stop theStop;

        if (theCursor.getCount() != 1) {
            theStop = null;
        } else {
            theCursor.moveToFirst();
            theStop = getStopFromCursor(theCursor);
        }

        theCursor.close();

        return theStop;
    }

    private Stop getStopFromCursor(final Cursor aCursor) {
        final int lat = aCursor.getInt(2);
        final int lng = aCursor.getInt(3);
        final GeoPoint thePoint = new GeoPoint(lat, lng);
        return new Stop(thePoint, getSanitizedStopName(aCursor.getString(1)), aCursor.getString(0), theAgency.getClass());

    }

    protected final int getNumberOfRoutes() {
        checkReadDb();
        final Cursor theCursor = theReadDB.rawQuery("SELECT count(tag) FROM routes", null);
        int count = 0;
        // error out
        if (theCursor.getCount() != 1) {
            count = 0;
        }
        theCursor.moveToFirst();
        count = theCursor.getInt(0);
        theCursor.close();
        return count;
    }

    protected final void addRoute(final ContentValues theRoute) {
        addRowToTable("routes", theRoute);
    }

    protected final void addStop(final ContentValues theStop) {
        addConstrainedRowToTable("stops", theStop);
        // Ignore, the only constraint here is the unique stop
        // and because NB doesn't provide us a list of stops
        // we will get duplicates
    }

    protected final void addDirection(final ContentValues theDirection) {
        addRowToTable("directions", theDirection);
    }

    protected final void addStopToRoute(final String aRouteTag, final String aStopTag) {
        theRow.clear();
        theRow.put("route_tag", aRouteTag);
        theRow.put("stop_tag", aStopTag);
        addRowToTable("routes_stops", theRow);
    }

    protected final void addStopToDirection(String aDirectionTag, String aStopTag, int aPosition) {
        theRow.clear();
        theRow.put("direction_tag", aDirectionTag);
        theRow.put("stop_tag", aStopTag);
        theRow.put("position", aPosition);
        addRowToTable("directions_stops", theRow);
    }

    @Override
    public final ArrayList<Route> getRoutes() {
        checkReadDb();
        final Cursor theCursor = theReadDB.rawQuery("SELECT title,tag FROM routes ORDER BY title ASC", null);
        final ArrayList ret = new ArrayList<Route>(theCursor.getCount());
        theCursor.moveToFirst();
        while (theCursor.isAfterLast() == false) {
            ret.add(routeFromCursor(theCursor));
            theCursor.moveToNext();
        }
        theCursor.close();

        return ret;
    }

    @Override
    protected final Route getRouteFromTag(final String aRouteTag) {
        if (aRouteTag == null) {
            return null;
        }
        checkReadDb();
        final String[] bindArgs = {aRouteTag};
        final Cursor theCursor = theReadDB.rawQuery("SELECT title,tag FROM routes WHERE tag = ? ORDER BY title ASC LIMIT 1", bindArgs);

        final Route ret;
        if (theCursor.getCount() != 1) {
            ret = new Route();
        } else {
            theCursor.moveToFirst();
            ret = routeFromCursor(theCursor);
        }
        theCursor.close();

        return ret;
    }

    private Route routeFromCursor(final Cursor aCursor) {
        final String ourAgencyName = theAgency.getShortName();
        final String ourRouteName = getSanitizedRouteName(aCursor.getString(0));
        final String ourRawTag = aCursor.getString(1);
        final String ourTag = getSanitizedRouteTag(ourRawTag);
        final int ourColor = getBadgeColorForRouteTag(ourRawTag);
        final Route theRoute = new Route(ourAgencyName, ourRouteName, ourTag, ourRawTag, ourColor);
        return theRoute;
    }

    protected final ArrayList<Direction> getDirections() {
        checkReadDb();
        final String theQuery;
        theQuery = "SELECT DISTINCT route_tag,tag,title FROM directions ORDER BY title ASC;";
        final Cursor theCursor = theReadDB.rawQuery(theQuery, null);
        final ArrayList<Direction> ret = new ArrayList<Direction>();
        theCursor.moveToFirst();
        while (theCursor.isAfterLast() == false) {
            ret.add(getDirectionFromCursor(theCursor));
            theCursor.moveToNext();
        }
        theCursor.close();

        return ret;
    }

    protected final Direction getDirectionForTag(final String aDirectionTag) {
        checkReadDb();
        final String[] bindArgs = {aDirectionTag};
        final String theQuery = "SELECT route_tag,tag,title FROM directions WHERE tag = ? LIMIT 1";
        final Cursor theCursor = theReadDB.rawQuery(theQuery, bindArgs);
        final Direction ret;

        if (theCursor.getCount() != 1) {
            ret = null;
        } else {
            theCursor.moveToFirst();
            ret = getDirectionFromCursor(theCursor);
        }

        theCursor.close();

        return ret;
    }

    protected final ArrayList<Direction> getDirectionsForRouteTag(final String aRouteTag) {
        checkReadDb();
        final ArrayList<Direction> ret = new ArrayList<Direction>();
        final String theQuery = "SELECT route_tag, tag, title FROM directions WHERE (visible = 1) AND (directions.route_tag = ?) ORDER BY directions.title ASC";
        final String[] theBindArgs = {aRouteTag};
        final Cursor theCursor = theReadDB.rawQuery(theQuery, theBindArgs);
        theCursor.moveToFirst();
        while (theCursor.isAfterLast() == false) {
            ret.add(getDirectionFromCursor(theCursor));
            theCursor.moveToNext();
        }
        theCursor.close();

        return ret;
    }

    private Direction getDirectionFromCursor(final Cursor aCursor) {
        final String directionTitle = getSanitizedDirection(aCursor.getString(2));
        //final String anAgencyClassName, final String aRouteTag, final String aTag, final String aTitle) {
        return new Direction(theAgency.getClass().getCanonicalName(), aCursor.getString(0), aCursor.getString(1), directionTitle, getTerseDirectionName(directionTitle));
    }

    // Stops
    protected String getSanitizedStopName(final String aStopTitle) {
        return aStopTitle;
    }

    protected String getTerseStopName(final String aStopTitle) {
        return aStopTitle;
    }

    // Routes
    protected String getSanitizedRouteTag(final String aRouteTag) {
        return aRouteTag;
    }

    protected String getSanitizedRouteName(final String aRouteTitle) {
        return aRouteTitle;
    }

    // Directions
    protected String getSanitizedDirection(final String aDirectionTitle) {
        return aDirectionTitle;
    }

    protected String getTerseDirectionName(final String aDirectionTitle) {
        return aDirectionTitle;
    }

    // Badge
    protected int getBadgeColorForRouteTag(String aRouteTag) {
        return Color.LTGRAY;
    }
}