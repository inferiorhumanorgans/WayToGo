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
package com.zippy.WayToGo.Agency.NextBus.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import com.zippy.WayToGo.Util.Direction;
import com.zippy.WayToGo.ListAdapter.PredictionAdapter;
import com.zippy.WayToGo.Agency.NextBus.Prediction.PredictionXMLTask;
import com.zippy.WayToGo.Agency.PredictionListener;
import com.zippy.WayToGo.Util.Prediction;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.PredictionGroup;
import com.zippy.WayToGo.Util.PredictionSummary;
import com.zippy.WayToGo.Util.Stop;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;

/**
 *
 * @author alex
 */
public class ShowPredictionsForStopActivity extends BaseNextBusActivity implements PredictionListener {

    private Direction theDirection = null;
    private Stop theStop = null;
    private static final String LOG_NAME = ShowPredictionsForStopActivity.class.getCanonicalName();
    private PredictionXMLTask thePredictionFetcher = null;
    protected Timer theRefreshTimer = null;
    protected int timerDuration;
    private boolean wantAllRoutes = false;
    private final ArrayList<Prediction> thePredictions = new ArrayList<Prediction>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_NAME, "onCreate");
        super.onCreate(savedInstanceState);

        theListAdapter = new PredictionAdapter(this);
        theListView.setAdapter(theListAdapter);
        theOptionsMenuId = R.menu.generic_prediction_menu;
    }

    @Override
    protected void onResume() {
        Log.d(LOG_NAME, "onResume");
        super.onResume();

        Intent intent = getIntent();
        wantAllRoutes = intent.getBooleanExtra("allRoutes", false);
        theStop = theAgency().getStop(intent.getStringExtra("stopId"));
        if (wantAllRoutes) {
            theRoute = null;
        } else {
            theDirection = theAgency().getDirectionFromTag(intent.getStringExtra("directionTag"));
        }


        killTimer();
        setupTimer();
        if (theListAdapter.isEmpty()) {
            setup();
        }
    }

    @Override
    public void onPause() {
        Log.d(LOG_NAME, "onPause()");
        super.onPause();
        killTimer();
        if (thePredictionFetcher != null) {
            thePredictionFetcher.cancel(true);
        }
    }

    protected void killTimer() {
        if (theRefreshTimer != null) {
            theRefreshTimer.cancel();
            theRefreshTimer.purge();
        }
    }

    protected void setupTimer() {
        timerDuration = TheApp.getPredictionRefreshDelay();
        boolean wantRefresh = TheApp.getPrefs().getBoolean(TheApp.getResString(R.string.pref_auto_refresh_key), TheApp.getResBool(R.bool.pref_auto_refresh_default));
        if (wantRefresh) {
            Log.i(LOG_NAME, "We're gonna set up a timer");

            theRefreshTimer = new Timer();
            theRefreshTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    refresh();
                }
            }, timerDuration, timerDuration);
        }

    }

    protected void refresh() {
        //We call the method that will work with the UI
        //through the runOnUiThread method.
        runOnUiThread(new Runnable() {

            public void run() {
                Log.i(LOG_NAME, "Refresh timer called");
                setup();
            }
        });
    }

    protected void setup() {
        theListAdapter.clear();

        if (wantAllRoutes) {
            theIconView.setText(theStop.getTheShortName());
        } else {
            theIconView.setText(theStop.getTheShortName() + "\nTo " + theDirection.getTheShortTitle());
        }

        theProgressDialog = new ProgressDialog(getDialogContext());
        theProgressDialog.setCancelable(true);
        theProgressDialog.setMessage(TheApp.getResString(R.string.loading_predictions));
        theProgressDialog.setIndeterminate(true);
        theProgressDialog.show();
        thePredictionFetcher = new PredictionXMLTask(theStop, this);
        thePredictionFetcher.execute(theAgency());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_bookmark_stop:
                addBookmarkForStop(theStop);
                return true;
            case R.id.menu_reload_predictions:
                killTimer();
                setupTimer();
                setup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public synchronized void addPrediction(final Prediction aPrediction) {
        Prediction thePrediction = new Prediction(theAgency(), aPrediction.getRouteTag(), theStop.getTheId(), aPrediction.getDirectionTag(), aPrediction.getMinutes());

        final Direction otherDirection = theAgency().getDirectionFromTag(thePrediction.getDirectionTag());

        if (!wantAllRoutes) {
            Assert.assertEquals(false, theRoute == null);
            Assert.assertEquals(false, thePrediction == null);
            if (!theRoute.getTheRawTag().equals(thePrediction.getRouteTag())) {
                thePrediction.setTheFlags("differentRoute");
            } else if ((theDirection != null) && !theDirection.equals(otherDirection)) {
                thePrediction.setTheFlags("differentDir");
            } else {
                thePrediction.setTheFlags("legit");
            }
        }
        //Log.i(LOG_NAME, "Got a prediction for: " + thePrediction.toString());
        thePredictions.add(thePrediction);
    }

    public void startPredictionFetch() {
    }

    public synchronized void finishedPullingPredictions(final boolean wasCancelled) {
        try {
            theProgressDialog.dismiss();
        } catch (IllegalArgumentException theEx) {
        }

        if (wasCancelled || thePredictionFetcher.isCancelled()) {
            Log.d(LOG_NAME, "We were cancelled, so... let's figure out something better to do");
            return;
        }

        theListAdapter.clear();
        if (wantAllRoutes) {
            displayAllPredictions(true);
        } else {
            final ArrayList<Prediction> theLegit = new ArrayList<Prediction>(thePredictions.size());

            for (Prediction aPrediction : thePredictions) {
                if (aPrediction.getTheFlags().equals("legit")) {
                    theLegit.add(aPrediction);
                }
            }
            if (!theLegit.isEmpty()) {
                //BaseAgency anAgency, String aRouteTag, String aStopTag, String aDirectionTag, final ArrayList<Integer> aMinutes
                ArrayList<PredictionGroup> foo = PredictionGroup.getPredictionGroups(theLegit, true);
                final PredictionSummary theSummary = new PredictionSummary(getDialogContext(), foo.get(0));
                theSummary.setTheFlags("legit");
                theListAdapter.add(theSummary);
            }
        }

        if (theListAdapter.isEmpty()) {
            theListAdapter.add(new PredictionSummary(getDialogContext()));
        }
        if (!wantAllRoutes) {
            displayAllPredictions(false);
        }
        thePredictions.clear();
    }

    private void displayAllPredictions(boolean includeLegit) {
        ArrayList<PredictionGroup> thePredictionGroups = PredictionGroup.getPredictionGroups(thePredictions, includeLegit);

        if (wantAllRoutes && (thePredictionGroups.size() != 1)) {
            // Assume we're actually in a tab
            // What we should do is check if we're tabbed and only hide the icon
            // in that case.
            theIconView.setIconVisible(false);
            final int theLogo = theAgency().getLogo();
            if (theLogo == -1) {
                theIconView.setBadgeText(theAgency().getShortName());
            } else {
                theIconView.setBadgeDrawable(theLogo);
            }
        }

        for (final PredictionGroup aGroup : thePredictionGroups) {
            final PredictionSummary theSummary = new PredictionSummary(getDialogContext(), aGroup);
            if ((thePredictionGroups.size() == 1) && wantAllRoutes) {
                theSummary.setTheFlags("legit");
                final Stop ourStop = theAgency().getStop(aGroup.getTheStopTag());
                final Direction ourDirection = theAgency().getDirectionFromTag(aGroup.getTheDirectionTag());
                theIconView.setText(ourStop.getTheShortName() + "\nTo " + ourDirection.getTheShortTitle());
                //theIconView.setText(theStop.first) + "\n" + theAgency.getTerseDirectionName(theDirection.first));
                theIconView.setBadgeText(aGroup.getTheRouteTag());
                theIconView.invalidate();
            }

            theListAdapter.add(theSummary);
        }

    }
}
