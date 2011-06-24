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
package com.zippy.WayToGo.Agency.GTFS;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.Time;
import android.util.Log;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.ListUtils;
import com.zippy.WayToGo.Agency.CommonDBHelper;
import com.zippy.WayToGo.Util.Direction;
import com.zippy.WayToGo.Util.Prediction;
import com.zippy.WayToGo.Util.Route;
import com.zippy.WayToGo.Util.Stop;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

/**
 *
 * @author alex
 */
public class GTFSDataHelper extends CommonDBHelper {

    private final static String LOG_NAME = GTFSDataHelper.class.getCanonicalName();
    private final Context theContext;
    private final GTFSAgency theAgency;
    private final static String DATABASE_NAME = "gtfs_data";
    private final static int DATABASE_VERSION = 1;

    public GTFSDataHelper(final Context context, final GTFSAgency anAgency) {
        super(TheApp.getContext(),
                TheApp.getDatabaseFileName(getDBName(anAgency)), null, DATABASE_VERSION);

        theContext = context;
        theAgency = anAgency;
    }

    protected static String getDBName(final GTFSAgency anAgency) {
        return DATABASE_NAME + "_" + slightlySanitize(anAgency.getGTFSName());
    }

    protected String getDBName() {
        return getDBName(theAgency);
    }

    @Override
    public void onCreate(SQLiteDatabase arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected final Direction getDirectionForTag(String aDirectionTag) {
        checkReadDb();
        final Direction ret;
        String theQuery = "SELECT DISTINCT route_id,trip_id,trip_headsign FROM trips WHERE trip_id = :trip_id: ORDER BY trip_headsign LIMIT 1;";
        theQuery = theQuery.replace(":trip_id:", DatabaseUtils.sqlEscapeString(aDirectionTag));

        final Cursor theCursor = theReadDB.rawQuery(theQuery, null);
        if (theCursor.getCount() != 1) {
            ret = null;
        } else {
            theCursor.moveToFirst();
            final String theRouteId = theCursor.getString(0);
            final String theDirectionTag = theCursor.getString(1);
            final String theTitle = theCursor.getString(2).replaceAll("\\s\\s", " ");
            //final String anAgencyClassName, final String aRouteTag, final String aTag, final String aTitle, final String aShortTitle
            ret = new Direction(theAgency.getClass().getCanonicalName(), theRouteId, theDirectionTag, theTitle);
        }
        theCursor.close();
        return ret;
    }

    public ArrayList<Prediction> getPredictionsForStop(final Stop aStop) {
        checkReadDb();
        final ArrayList<Prediction> ourPredictions = new ArrayList<Prediction>();
        final ArrayList<String> theCalendars = getCalendars();
        final ArrayList<String> theTrips = getTripsForCalendars(theCalendars);

        String theQuery = "SELECT st.arrival_time,st.departure_time,st.stop_sequence,r.route_id,t.trip_id,st.stop_headsign FROM stop_times AS st JOIN trips AS t ON t.trip_id = st.trip_id, routes AS r ON r.route_id = t.route_id WHERE st.trip_id IN (:trip_ids:) AND stop_id = :stop_id: ORDER BY st.arrival_time ASC;";
        //Log.d(LOG_NAME, "BASE QUERY IS: " + theQuery);
        theQuery = theQuery.replace(":trip_ids:", ListUtils.sqlEscapedJoin(theTrips, ", "));
        //Log.d(LOG_NAME, "First replace: " + theQuery);
        theQuery = theQuery.replace(":stop_id:", DatabaseUtils.sqlEscapeString(aStop.getTheId()));
        //Log.d(LOG_NAME, "getPredictionsForStop: query = " + theQuery);

        final Cursor theCursor = theReadDB.rawQuery(theQuery, null);
        theCursor.moveToFirst();
        final int nowEpoch = (int) (System.currentTimeMillis() / 1000);

        while (theCursor.isAfterLast() == false) {
            //(final BaseAgency anAgency, final String aRouteTag, final String aStopTag, final String aDirectionTag, final int aMinutes

            // Get internet time
            final Date now = new Date();
            final SimpleDateFormat theDayOfWeekFormat = new SimpleDateFormat("yyyy-MM-dd");
            String departDate = theDayOfWeekFormat.format(now);
            departDate = departDate + "T" + theCursor.getString(1);
            Time departTime = new Time();

            departTime.parse3339(departDate);
            final int departEpoch = (int) (departTime.toMillis(false) / 1000);

            if (departEpoch <= nowEpoch) {
                theCursor.moveToNext();
                continue;
            }

            int minutes = (departEpoch - nowEpoch) / 60;

            // Ignore stuff more than two hours out
            if (minutes > 120) {
                theCursor.moveToNext();
                continue;
            }

            final Prediction pred = new Prediction(theAgency, theCursor.getString(3), aStop.getTheId(), theCursor.getString(4), minutes);
            ourPredictions.add(pred);
            theCursor.moveToNext();
        }
        theCursor.close();

        return ourPredictions;
    }

    public Stop getStop(String aStopId) {
        checkReadDb();
        final Stop ret;
        final ArrayList<String> theCalendars = getCalendars();
        final ArrayList<String> theTrips = getTripsForCalendars(theCalendars);

        String theQuery = "SELECT DISTINCT s.stop_name,s.stop_id,lat,lng,s.stop_desc FROM stops AS s JOIN stop_times AS st ON st.stop_id = s.stop_id WHERE trip_id IN (:trip_ids:) AND s.stop_id = :stop_id: ORDER BY stop_name LIMIT 1;";
        theQuery = theQuery.replace(":trip_ids:", ListUtils.sqlEscapedJoin(theTrips, ", "));
        theQuery = theQuery.replace(":stop_id:", DatabaseUtils.sqlEscapeString(aStopId));

        final Cursor theCursor = theReadDB.rawQuery(theQuery, null);
        if (theCursor.getCount() != 1) {
            return null;
        }
        theCursor.moveToFirst();
        ret = getStopFromCursor(theCursor);
        theCursor.close();
        return ret;
    }

    public ArrayList<Stop> getStops(BoundingBoxE6 aBoundingBox) {
        checkReadDb();
        final ArrayList<Stop> ret = new ArrayList<Stop>();
        final ArrayList<String> theCalendars = getCalendars();
        final ArrayList<String> theTrips = getTripsForCalendars(theCalendars);

        final String theBaseQuery;
        final String[] theBindArgs;
        if (aBoundingBox == null) {
            theBaseQuery = "SELECT DISTINCT s.stop_name,s.stop_id,lat,lng,s.stop_desc FROM stops AS s JOIN stop_times AS st ON st.stop_id = s.stop_id WHERE trip_id IN (:trip_ids:) ORDER BY stop_name;";
            theBindArgs = null;
        } else {
            theBindArgs = new String[] {
                        String.valueOf(aBoundingBox.getLatSouthE6()),
                        String.valueOf(aBoundingBox.getLatNorthE6()),
                        String.valueOf(aBoundingBox.getLonWestE6()),
                        String.valueOf(aBoundingBox.getLonEastE6())};
            theBaseQuery = "SELECT DISTINCT s.stop_name,s.stop_id,lat,lng,s.stop_desc FROM stops AS s JOIN stop_times AS st ON st.stop_id = s.stop_id WHERE (trip_id IN (:trip_ids:)) AND (lat BETWEEN ? AND ?) AND (lng BETWEEN ? AND ?) ORDER BY stop_name;";
        }
        //Log.d(LOG_NAME, "Base query: " + theQuery);
        final String theQuery = theBaseQuery.replace(":trip_ids:", ListUtils.sqlEscapedJoin(theTrips, ", "));

        //Log.d(LOG_NAME, "getAllStops: " + theQuery);
        final Cursor theCursor = theReadDB.rawQuery(theQuery, theBindArgs);
        theCursor.moveToFirst();
        while (theCursor.isAfterLast() == false) {
            ret.add(getStopFromCursor(theCursor));
            theCursor.moveToNext();
        }
        theCursor.close();
        return ret;
    }

    private Stop getStopFromCursor(final Cursor aCursor) {
        //(final GeoPoint aPoint, final String aName, final String anId, Class anAgencyClass)
        final int lat = aCursor.getInt(2);
        final int lng = aCursor.getInt(3);
        final GeoPoint thePoint = new GeoPoint(lat, lng);

        final Stop ourStop = new Stop(thePoint, aCursor.getString(0), aCursor.getString(1), aCursor.getString(4), theAgency.getClass());
        return ourStop;
    }

    // http://snippets.dzone.com/posts/show/91
    private ArrayList<String> getTripsForCalendars(final ArrayList<String> someCalendars) {
        checkReadDb();
        final ArrayList<String> ret = new ArrayList<String>();

        String theQuery = "SELECT DISTINCT trip_id FROM trips WHERE service_id IN (:service_ids:);";
        theQuery = theQuery.replace(":service_ids:", ListUtils.sqlEscapedJoin(someCalendars, ", "));
        //Log.d(LOG_NAME, "getTripsForCalendars: " + theQuery.toString());
        final Cursor theCursor = theReadDB.rawQuery(theQuery.toString(), null);
        theCursor.moveToFirst();
        while (theCursor.isAfterLast() == false) {
            ret.add(theCursor.getString(0));
            theCursor.moveToNext();
        }
        theCursor.close();
        return ret;
    }

    private ArrayList<String> getCalendars() {
        checkReadDb();
        final ArrayList<String> ret = new ArrayList<String>();
        final SimpleDateFormat theDateFormat = new SimpleDateFormat("yyyyMMdd");
        final Date now = new Date();
        final String nowString = theDateFormat.format(now);


        final SimpleDateFormat theDayOfWeekFormat = new SimpleDateFormat("EEEE");
        final String dayString = theDayOfWeekFormat.format(now).toLowerCase();

        checkReadDb();
        final Cursor theCursor;

        // Unless SimpleDateFormat gets compromised, the unescaped stuff should fine
        final String theBaseQuery = "SELECT service_id FROM calendars WHERE (start_date <= :now:) AND (end_date >= :now:) AND (:day_of_week: ='1') ORDER BY service_id ASC";
        final String theQuery = theBaseQuery.replaceAll(":now:", DatabaseUtils.sqlEscapeString(nowString)).replaceFirst(":day_of_week:", dayString);
        //Log.d(LOG_NAME, "Calendar query is: " + theQuery.toString());

        theCursor = theReadDB.rawQuery(theQuery, null);
        theCursor.moveToFirst();
        while (theCursor.isAfterLast() == false) {
            //Log.d(LOG_NAME, "Adding calendar: " + theCursor.getString(0));
            ret.add(theCursor.getString(0));
            theCursor.moveToNext();
        }
        theCursor.close();
        return ret;
    }

    public Route getRouteFromTag(final String aRouteTag) {
        checkReadDb();
        final Route ret;

        String theQuery = "SELECT DISTINCT route_id,route_long_name,route_short_name FROM routes WHERE route_id = :route_id: ORDER BY route_long_name LIMIT 1;";
        theQuery = theQuery.replace(":route_id:", DatabaseUtils.sqlEscapeString(aRouteTag));

        final Cursor theCursor = theReadDB.rawQuery(theQuery, null);
        if (theCursor.getCount() != 1) {
            ret = new Route();
        } else {
            theCursor.moveToFirst();

            final String ourAgencyName = theAgency.getShortName();
            final String ourRouteName = theCursor.getString(1);
            final String ourRouteRawTag = theCursor.getString(0);
            final String ourRouteTag = theCursor.getString(2);
            ret = new Route(ourAgencyName, ourRouteName, ourRouteTag, ourRouteRawTag);
        }
        theCursor.close();
        return ret;
    }

    @Override
    protected final ArrayList<Route> getRoutes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected ArrayList<Direction> getDirections() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
