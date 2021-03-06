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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import com.inferiorhumanorgans.WayToGo.Agency.BART.DataHelper;
import com.inferiorhumanorgans.WayToGo.Agency.BART.Route.RouteFetchListener;
import com.inferiorhumanorgans.WayToGo.Agency.BART.Station.StationActivity;
import com.inferiorhumanorgans.WayToGo.Agency.BART.Route.RouteTask;
import com.inferiorhumanorgans.WayToGo.Agency.BART.Station.StationTask;
import com.inferiorhumanorgans.WayToGo.BaseActivityGroup;
import com.inferiorhumanorgans.WayToGo.TheApp;
import com.inferiorhumanorgans.WayToGo.Util.Direction;
import com.inferiorhumanorgans.WayToGo.Util.Stop;
import java.util.Collection;

/**
 *
 * @author alex
 */
public final class BARTAgency extends BaseAgency {

    private static final String LOG_NAME = BARTAgency.class.getCanonicalName();

    protected static final String theLongName = "Bay Area Rapid Transit";
    protected static final String theShortName = "BART";
    protected static final String theURL = "http://www.bart.gov/";

    protected DataHelper theDBHelper = null;
    private StationTask theStationFetcher = null;
    private RouteTask theRouteFetcher = null;
    public static final String API_KEY = "MW9S-E7SL-26DU-VV8V";
    protected StationActivity theStationView;
    private RouteFetchListener theRouteFetchListener;

    public BARTAgency() {
        super();
    }

    @Override
    public void init(final Context aContext) {
        super.init(aContext);

        /*
         * We look it up manually in case we're running a build without icons.
         * If that's the case we should use the agency's short name instead.
         */
        final Resources ourResources = TheApp.getContext().getResources();
        theLogoId = ourResources.getIdentifier("drawable/bart", null, "com.inferiorhumanorgans.WayToGo");

        if (theDBHelper == null) {
            theDBHelper = (DataHelper) setTheDBHelper(new DataHelper(theContext, this));
        }
    }

    @Override
    public void finish() {
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

        if (theStationFetcher != null) {
            Log.d(LOG_NAME, "Canceling station fetcher");
            theStationFetcher.cancel(true);
        }
        if (theRouteFetcher != null) {
            Log.d(LOG_NAME, "Canceling route fetcher");
            theRouteFetcher.cancel(true);
        }
        if (theDBHelper != null) {
            Log.d(LOG_NAME, "Cleaning up the database helper");
            theDBHelper.cleanUp();
        }
    }

    public final int getNumberOfStations() {
        return theDBHelper.getNumberOfStations();
    }

    public final void refreshStations() {
        if (theRouteFetchListener != null) {
            theRouteFetchListener.currentlyFetchingStationList();
        }
        theStationFetcher = new StationTask();
        theStationFetcher.execute(this);
    }

    public final void setRouteFetchListener(final RouteFetchListener aListener) {
        theRouteFetchListener = aListener;
    }

    public final synchronized void addStations(final Collection<ContentValues> someStations) {
        Log.d(LOG_NAME, "Batch adding stations.");
        theDBHelper.beginTransaction();
        for (final ContentValues ourStation : someStations) {
            Log.d(LOG_NAME, "Adding: " + ourStation);
            addStation(ourStation);
        }
        theDBHelper.endTransaction();
    }

    public final synchronized void addStation(final ContentValues aStation) {
        theDBHelper.addStation(aStation);
    }

    public final synchronized void addRoute(final ContentValues aRoute) {
        theDBHelper.addRoute(aRoute);
    }

    public final synchronized void finishedParsingStations() {
        theStationFetcher = null;
        if (theRouteFetchListener != null) {
            theRouteFetchListener.finishedProcessingStationList();
            theRouteFetchListener.currentlyFetchingRouteList();
        }
        theRouteFetcher = new RouteTask();
        theRouteFetcher.execute(this);
    }

    public final synchronized void finishedParsingRoutes() {
        if (theRouteFetchListener != null) {
            theRouteFetchListener.finishedProcessingRouteList();
        }
    }

    @Override
    public final Intent getPredictionIntentForStop(final Stop aStop, final Direction aDirection) {
        final Intent ourIntent = new Intent("w2g.action.BART.PREDICTIONS_FOR_STATION");
        ourIntent.putExtra("stationTitle", aStop.name());
        ourIntent.putExtra("stationTag", aStop.stopId());
        ourIntent.putExtra("AgencyClassName", this.getClass().getCanonicalName());
        return ourIntent;
    }

    public static class ActivityGroup extends BaseActivityGroup {

        @Override
        public void onResume() {
            super.onResume();
            if (mIdList.isEmpty()) {
                startChildActivity(StationActivity.class.getCanonicalName(), new Intent(getParent(), StationActivity.class));
            }
        }
    }

    public AsyncTask fetchPredictionsForStop(final Stop aStop, final PredictionListener aListener) {
        com.inferiorhumanorgans.WayToGo.Agency.BART.Prediction.XMLTask ourTask = new com.inferiorhumanorgans.WayToGo.Agency.BART.Prediction.XMLTask(aStop, aListener);
        return ourTask.execute(this);
    }
}
