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
import android.util.Log;
import android.content.Intent;
import android.os.AsyncTask;
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.Agency.PredictionListener;
import com.zippy.WayToGo.Util.Direction;
import com.zippy.WayToGo.Util.Stop;

/**
 *
 * @author alex
 */
abstract public class GTFSAgency extends BaseAgency {

    private static final String LOG_NAME = GTFSAgency.class.getCanonicalName();
    protected String theGTFSName = null;
    protected GTFSDataHelper theDBHelper = null;

    @Override
    public synchronized void init(final Context aContext) {
        super.init(aContext);
        if (theDBHelper == null) {
            theDBHelper = (GTFSDataHelper) setTheDBHelper(new GTFSDataHelper(theContext, this));
        }
    }

    @Override
    public synchronized void finish() {
        Log.d(LOG_NAME, "finish()");

        if (TRACE_FINISH) {
            StackTraceElement[] ourTrace = Thread.currentThread().getStackTrace();
            Log.d(LOG_NAME, "finish(): ");
            int i = 2;
            if (ourTrace.length < 2) {
                i = 0;
            }
            for (; i < ourTrace.length; i++) {
                Log.d(LOG_NAME, ourTrace[i].toString());
            }
        }

        // Ideally this object will last the lifetime of the app so we should
        // never need to close the DB or destroy its helper.  Ideally.
        if (theDBHelper != null) {
            theDBHelper.cleanUp();
        }
    }

    public String getGTFSName() {
        return theGTFSName;
    }

    @Override
    public final Intent getPredictionIntentForStop(final Stop aStop, final Direction aDirection) {
        final Intent ourIntent = new Intent("w2g.action.GTFS.PREDICTIONS_STOP_SPECIFIC");
        ourIntent.putExtra("stopName", aStop.name());
        ourIntent.putExtra("stopId", aStop.stopId());
        if (aDirection != null) {
            ourIntent.putExtra("directionTitle", aDirection.title());
            ourIntent.putExtra("directionTag", aDirection.tag());
        }
        ourIntent.putExtra("allRoutes", true);
        ourIntent.putExtra("AgencyClassName", this.getClass().getCanonicalName());
        return ourIntent;

    }

    @Override
    public AsyncTask fetchPredictionsForStop(final Stop aStop, final PredictionListener aListener) {
        return new PredictionTask(this, aListener).execute(aStop);
    }
}
