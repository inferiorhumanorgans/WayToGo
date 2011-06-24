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
package com.zippy.WayToGo.MapView;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.Stop;
import java.util.ArrayList;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.overlay.OverlayItem;

/**
 *
 * @author alex
 */
public class OSMGetStopsTask extends AsyncTask<Void, Void, Void> {

    private static final String LOG_NAME = OSMGetStopsTask.class.getCanonicalName();
    private final AllStopsActivity theActivity;
    private final ArrayList<OverlayItem> theOverlayItems = new ArrayList<OverlayItem>();
    private final BoundingBoxE6 theBoundingBox;
    private Thread theBGThread = null;
    private volatile boolean shouldDie = false;

    public OSMGetStopsTask(final AllStopsActivity anActivity, final BoundingBoxE6 aBoundingBox) {
        super();
        Log.d(LOG_NAME, "CTOR");
        theActivity = anActivity;
        theBoundingBox = aBoundingBox;
    }

    @Override
    protected void onCancelled() {
        Log.d(LOG_NAME, "onCancelled()");
        super.onCancelled();
        shouldDie = true;

        if (theBGThread != null) {
            Log.d(LOG_NAME, "BG Thread still running, trying to interrupt");
            theBGThread.interrupt();
            for (BaseAgency anAgency : TheApp.theAgencies.values()) {
                anAgency.finish();
            }
            theActivity.addStops(null);
        } else {
            Log.d(LOG_NAME, "No background thread");
        }
    }

    /**
     * @note If we've been told to cancel, presume someone else has taken over
     * and don't clean up the agency/db
     * @param args
     * @return
     */
    @Override
    protected Void doInBackground(Void... args) {
        theBGThread = Thread.currentThread();
        //Log.d(LOG_NAME, "START FETCHING STOPS");
        if (isCancelled()) {
            return null;
        }

        for (BaseAgency anAgency : TheApp.theAgencies.values()) {
            anAgency.init(theActivity);
            final ArrayList<Stop> theStops = anAgency.getStops(theBoundingBox);
            if ((theStops == null) || (theStops.isEmpty())) {
                continue;
            }
            Log.i(LOG_NAME, "Got this many stops (" + anAgency.getShortName() + "): " + theStops.size());
            Log.i(LOG_NAME, "Bounding box was: " + 
                    theBoundingBox.getLatSouthE6() +
                    "/" +
                    theBoundingBox.getLatNorthE6() +
                    " " +
                    theBoundingBox.getLonWestE6() +
                    "/" +
                    theBoundingBox.getLonEastE6()
            );

            //Log.d(LOG_NAME, "END FETCHING STOPS");
            //Log.d(LOG_NAME, "START MAKING AND ADDING MARKERS");


            if (isCancelled()) {
                return null;
            }

            final Context theContext = theActivity;
            for (final Stop aStop : theStops) {
                if (isCancelled()) {
                    return null;
                }
                if (aStop.getThePoint() == null) {
                    continue;
                }
                final StopOverlayItem theOverlayItem = new StopOverlayItem(aStop);
                theOverlayItems.add(theOverlayItem);
            }
            //Log.d(LOG_NAME, "END MAKING AND ADDING MARKERS");

            //Log.d(LOG_NAME, "PAUSING MUNI");
            if (isCancelled()) {
                return null;
            }

            anAgency.finish();
            //Log.d(LOG_NAME, "DONE PAUSING MUNI");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (!isCancelled() && !shouldDie) {
            Log.i(LOG_NAME, "Calling addStops!");
            theActivity.addStops(theOverlayItems);
        } else {
            Log.i(LOG_NAME, "Not calling addStops(NULL)!");
            //theActivity.addStops(null);
        }
    }
}
