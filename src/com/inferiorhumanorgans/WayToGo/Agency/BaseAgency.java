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
package com.inferiorhumanorgans.WayToGo.Agency;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.inferiorhumanorgans.WayToGo.Util.CopyDBListener;
import com.inferiorhumanorgans.WayToGo.Util.Direction;
import com.inferiorhumanorgans.WayToGo.Util.Route;
import com.inferiorhumanorgans.WayToGo.Util.Stop;
import java.util.ArrayList;
import org.osmdroid.util.BoundingBoxE6;

/**
 *
 * @author alex
 */
public abstract class BaseAgency {

    private final static String LOG_NAME = BaseAgency.class.getCanonicalName();
    protected String theURL;
    protected String theShortName;
    protected String theLongName;
    protected Context theContext;
    private CommonDBHelper theCommonDBHelper;
    protected int theLogoId = 0;
    public static final int AGENCY_READY = 0;
    public static final int AGENCY_DB_CLOSED = 1;
    public static final int AGENCY_DB_MISSING = 2;
    protected static final boolean TRACE_FINISH = false;

    public BaseAgency() {
    }

    /**
     * Initializes the database connection,
     * potentially creating a new dataset either OTA or from the APK
     * @param aContext
     */
    public synchronized void init(final Context aContext) {
        theContext = aContext;
    }

    /**
     * Typically just closes up the database connection.
     */
    abstract public void finish();

    /**
     * 0 = Ready
     * 1 = No DB Helper
     * 2 = No DB at all
     * @return
     */
    public int getStatus() {
        if (theCommonDBHelper == null) {
            return AGENCY_DB_CLOSED;
        }

        if (!theCommonDBHelper.checkDataBase()) {
            return AGENCY_DB_MISSING;
        }

        return AGENCY_READY;
    }

    protected final CommonDBHelper setTheDBHelper(final CommonDBHelper aDBHelper) {
        theCommonDBHelper = aDBHelper;
        return aDBHelper;
    }

    /**
     *
     * @return the resource ID of the Agency's logo or -1 if we don't have one.
     */
    public final int getLogo() {
        return theLogoId;
    }

    /**
     *
     * @return A long name for the agency. Ex: San Francisco Municipal Railway
     */
    public final String getLongName() {
        return theLongName;
    }

    /**
     *
     * @return a short name for the agency Ex: Muni
     */
    public final String getShortName() {
        return theShortName;
    }

    /**
     * @return The URL for the agency's main web site
     */
    public final String getURL() {
        return theURL;
    }

    // Stops

    
    /**
     *
     * @param aStopTag
     * @return
     */
    public final Stop getStop(final String aStopId) {
        return theCommonDBHelper.getStop(aStopId);
    }

    /**
     *
     * @param aBoundingBox
     * @return An ArrayList of Stop objects representing all the stops in an
     * agency that fall inside the bounding box.
     */
    public final ArrayList<Stop> getStops(final BoundingBoxE6 aBoundingBox) {
        return theCommonDBHelper.getStops(aBoundingBox);
    }

    /**
     *
     * @param aStop The Stop object which we want to represent with an intent.
     * @return An intent that can be used to view predictions for a stop
     */
    public final Intent getPredictionIntentForStop(final Stop aStop) {
        return getPredictionIntentForStop(aStop, null);
    }

    /**
     *
     * @param aStop The Stop object which we want to represent with an intent.
     * @param aDirection An optional direction object if we only want
     * predictions for a specific route/direction combo.
     * @return An intent that can be used to view predictions for a stop
     */
    abstract public Intent getPredictionIntentForStop(final Stop aStop, final Direction aDirection);

    /**
     *
     * @param aStop
     * @param aListener
     * @return The AsyncTask object that we just launched.
     */
    abstract public AsyncTask fetchPredictionsForStop(final Stop aStop, final PredictionListener aListener);

    // Routes

    /**
     *
     * @return An ArrayList of Route objects representing all the routes in an
     * agency.
     */
    public final ArrayList<Route> getRoutes() {
        return theCommonDBHelper.getRoutes();
    }

    public final Route getRouteFromTag(final String aRouteTag) {
        return theCommonDBHelper.getRouteFromTag(aRouteTag);
    }

    // Directions

    public final Direction getDirectionFromTag(final String aDirectionTag) {
        return theCommonDBHelper.getDirectionForTag(aDirectionTag);
    }

    public final ArrayList<Direction> getDirections() {
        return theCommonDBHelper.getDirections();
    }

    /**
     * Extracts the database from the APK to a flat file on the SD card
     * @param aListener A listener object so that we can display a progress dialog
     */
    public void copyDatabase(final CopyDBListener aListener) {
        theCommonDBHelper.copyDatabase(aListener);
    }
}
