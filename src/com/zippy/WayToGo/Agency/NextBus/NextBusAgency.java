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
import com.zippy.WayToGo.Agency.NextBus.RouteList.RouteListXMLTask;
import com.zippy.WayToGo.Agency.NextBus.RouteConfig.RouteConfigXMLTask;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.Agency.NextBus.Prediction.PredictionXMLTask;
import com.zippy.WayToGo.Agency.PredictionListener;
import com.zippy.WayToGo.Util.Stop;
import java.util.ArrayList;

/**
 *
 * @author alex
 */
public abstract class NextBusAgency extends BaseAgency {

    private static final String LOG_NAME = NextBusAgency.class.getCanonicalName();
    protected String theNBName = null;
    protected NextBusDataHelper theDBHelper = null;
    private RouteConfigXMLTask theRouteConfigFetcher = null;
    private RouteListXMLTask theRouteListFetcher = null;
    private int numberOfExpectedRoutes = 0;
    private NextBusRouteFetchListener theRouteFetchListener;

    public NextBusAgency() {
        super();
    }

    @Override
    public synchronized void init(final Context aContext) {
        super.init(aContext);
    }

    @Override
    public synchronized void finish() {
        Log.d(LOG_NAME, "finish()");

        if (TRACE_FINISH) {
            StackTraceElement[] theTrace = Thread.currentThread().getStackTrace();
            Log.d(LOG_NAME, "finish(): ");
            int i = 2;
            if (theTrace.length < 2) {
                i = 0;
            }
            for (; i < theTrace.length; i++) {
                Log.d(LOG_NAME, theTrace[i].toString());
            }
        }

        if (theRouteConfigFetcher != null) {
            theRouteConfigFetcher.cancel(true);
            theRouteConfigFetcher = null;
        }
        if (theRouteListFetcher != null) {
            theRouteListFetcher.cancel(true);
            theRouteListFetcher = null;
        }

        // Ideally this object will last the lifetime of the app so we should
        // never need to close the DB or destroy its helper.  Ideally.
        if ((theDBHelper != null)) {
            theDBHelper.cleanUp();
        }
    }

    @Override
    public final Intent getPredictionIntentForStop(final Stop aStop, final Direction aDirection) {
        final Intent theIntent = new Intent("w2g.action.NextBus.PREDICTIONS_ROUTE_SPECIFIC");
        theIntent.putExtra("stopName", aStop.getTheName());
        theIntent.putExtra("stopId", aStop.getTheId());
        if (aDirection != null) {
            theIntent.putExtra("directionTitle", aDirection.getTheTitle());
            theIntent.putExtra("directionTag", aDirection.getTheTag());
            theIntent.putExtra("allRoutes", false);
            theIntent.putExtra("currentRouteTag", aDirection.getTheRouteTag());
        } else {
            theIntent.putExtra("allRoutes", true);
        }
        theIntent.putExtra("AgencyClassName", getClass().getCanonicalName());
        return theIntent;

    }

    public AsyncTask fetchPredictionsForStop(final Stop aStop, final PredictionListener aListener) {
        PredictionXMLTask ourTask = new PredictionXMLTask(aStop, aListener);
        return ourTask.execute(this);
    }

    public synchronized final void setRouteFetchListener(final NextBusRouteFetchListener aListener) {
        theRouteFetchListener = aListener;
    }

    // Nextbus Specific
    public final String getNextBusName() {
        return theNBName;
    }

    public final int getNumberOfRoutes() {
        return theDBHelper.getNumberOfRoutes();
    }

    public final synchronized void refreshRoutes() {
        if (theRouteFetchListener != null) {
            theRouteFetchListener.currentlyFetchingRouteList();
        }
        theRouteListFetcher = new RouteListXMLTask();
        theRouteListFetcher.execute(this);
    }

    public final synchronized void finishedParsingRouteList() {
        if (theRouteFetchListener != null) {
            theRouteFetchListener.finishedProcessingRouteList();
        }
    }

    public final synchronized void fetchRouteConfig() {
        if (numberOfExpectedRoutes > 100) {
            // What we should be doing is warning the user and then fetching the
            // routes individually
            AlertDialog.Builder alertbox = new AlertDialog.Builder(theContext);
            alertbox.setTitle(getShortName());
            alertbox.setMessage("Unfortunately NextBus lists more than 100 routes for this agency, and their API does not support this.  Stay tuned for future updates.");
            alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface arg0, int arg1) {
                }
            });

            // show it
            alertbox.show();
            return;
        }

        theRouteFetchListener.currentlyFetchingRouteConfig(numberOfExpectedRoutes);
        theRouteConfigFetcher = new RouteConfigXMLTask();
        theRouteConfigFetcher.execute(this);
    }

    public final synchronized void finishedRoute() {
        Log.i(LOG_NAME, "We think we've finished a route");
        theDBHelper.endTransaction();
    }

    public final synchronized void bumpProgress() {
        if (theRouteFetchListener != null) {
            theRouteFetchListener.finishedProcessingOneRoute();
        }
    }

    public final synchronized void finishedParsingRouteConfig() {
        if (theRouteFetchListener != null) {
            theRouteFetchListener.finishedProcessingAllRoutes();
        }
    }

    public final ArrayList<Direction> getDirectionsForRouteTag(final String aRouteTag) {
        return theDBHelper.getDirectionsForRouteTag(aRouteTag);
    }

    public final ArrayList<Stop> getStopsForDirectionTag(final String aDirectionTag) {
        return theDBHelper.getStopsForDirectionTag(aDirectionTag);
    }

    public final synchronized void setNumberOfExpectedRoutes(final int anExpectedNumber) {
        numberOfExpectedRoutes = anExpectedNumber;
    }

    public final synchronized void addRoute(final ContentValues theValues) {
        theDBHelper.beginTransaction();
        theDBHelper.addRoute(theValues);
    }

    public final synchronized void addDirection(final ContentValues aDirection) {
        theDBHelper.addDirection(aDirection);
    }

    public final void addStop(final ContentValues someContentValues) {
        theDBHelper.addStop(someContentValues);
    }

    public final void addStopToRoute(final String aRouteTag, final String aStopTag) {
        theDBHelper.addStopToRoute(aRouteTag, aStopTag);
    }

    public final void addStopToDirection(final String aDirectionTag, final String aStopTag, final int aPosition) {
        theDBHelper.addStopToDirection(aDirectionTag, aStopTag, aPosition);
    }

    public final Stop getStopAtIndexOnDirection(final String aDirectionTag, final int anIndex) {
        return theDBHelper.getStopAtIndexOnDirection(aDirectionTag, anIndex);
    }
}
