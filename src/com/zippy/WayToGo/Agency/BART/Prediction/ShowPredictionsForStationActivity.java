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
//http://stackoverflow.com/questions/3334048/android-layout-replacing-a-view-with-another-view-on-run-time
//http://stackoverflow.com/questions/4396221/how-to-show-alert-inside-an-activity-group
package com.zippy.WayToGo.Agency.BART.Prediction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;
import com.zippy.WayToGo.Agency.BART.BaseBARTActivity;
import com.zippy.WayToGo.Agency.PredictionListener;
import com.zippy.WayToGo.Comparator.PredictionComparator;
import com.zippy.WayToGo.Util.Prediction;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.Stop;
import com.zippy.WayToGo.Widget.IconTextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author alex
 */
public class ShowPredictionsForStationActivity extends BaseBARTActivity implements PredictionListener {

    private static final String LOG_NAME = ShowPredictionsForStationActivity.class.getCanonicalName();
    private Stop theStation;
    private Timer theRefreshTimer = null;
    private int timerDuration;
    private XMLTask thePredictionFetcher = null;
    private HashMap<String, ArrayList<Prediction>> thePredictionGroups;
    private ExpandableListView theExpandableListView;
    private ShowPredictionsAdapter theExpandableListAdapter;
    private IconTextView theIconView;

    private void switchToExpandableView() {
        setContentView(R.layout.nextbus_new_layout);

        theListView = (ListView) findViewById(R.id.nb_direction_listview);
        ViewGroup parent = (ViewGroup) theListView.getParent();
        int index = parent.indexOfChild(theListView);
        parent.removeView(theListView);
        theListView = null;
        theListAdapter = null;
        theExpandableListView = (ExpandableListView) new ExpandableListView(this);
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO) {
            theExpandableListView.setGroupIndicator(null);
        }

        theExpandableListAdapter = new ShowPredictionsAdapter(this);
        theExpandableListView.setAdapter(theExpandableListAdapter);
        parent.addView(theExpandableListView, index);
        theIconView = (IconTextView) findViewById(R.id.nb_icon_view);
    }

    @Override
    public void onCreate(Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);

        Log.d(LOG_NAME, "onCreate()");

        switchToExpandableView();

        setTitle(TheApp.getAppTitle() + " - BART");
        Intent theIntent = getIntent();

        theAgency().init(this);

        theStation = theAgency().getStop(theIntent.getStringExtra("stationTag"));
        thePredictionGroups = new HashMap<String, ArrayList<Prediction>>();
        theOptionsMenuId = R.menu.generic_prediction_menu;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_NAME, "onResume()");

        theAgency().init(this);

        timerDuration = TheApp.getPredictionRefreshDelay();

        killTimer();
        setupTimer();
        setup();

    }

    @Override
    protected void onPause() {
        Log.d(LOG_NAME, "onPause()");
        super.onPause();

        killTimer();
        theAgency().finish();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_NAME, "onStop()");
        killTimer();
        theAgency().finish();
        super.onStop();
    }

    protected void setupTimer() {
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

    protected void killTimer() {
        if (theRefreshTimer != null) {
            theRefreshTimer.cancel();
            theRefreshTimer.purge();
            theRefreshTimer = null;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_bookmark_stop:
                addBookmarkForStop(theStation);
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

    /**
     * Load the predictions.
     */
    protected void setup() {
        thePredictionGroups.clear();

        theExpandableListAdapter.clear();

        theIconView.setIconVisible(false);
        theIconView.setText(theStation.getTheName() + "\n" + theStation.getTheAddress());

        theProgressDialog = new ProgressDialog(getDialogContext());
        theProgressDialog.setCancelable(true);
        theProgressDialog.setMessage(TheApp.getResString(R.string.loading_predictions));
        theProgressDialog.setIndeterminate(true);
        theProgressDialog.show();
        thePredictionFetcher = new XMLTask(theStation, this);
        thePredictionFetcher.execute(theAgency());
    }

    /**
     * Called from our timer thread
     */
    protected void refresh() {
        runOnUiThread(new Runnable() {

            public void run() {
                Log.i(LOG_NAME, "Refresh timer called");
                setup();
            }
        });
    }

    protected static String formatPrediction(final Prediction aPrediction, final boolean groupByPlatform) {
        final int theMinutes = aPrediction.getMinutes();
        final String theNumberOfCars = aPrediction.getTheFlags();
        final String thePlatform = aPrediction.getTheSubStopTag().toLowerCase();
        final String theDestination = aPrediction.getDirectionTag();
        String theText;
        if (!groupByPlatform) {
            switch (theMinutes) {
                case 0:
                    theText = "Currently at " + thePlatform + "\n" + theNumberOfCars + " cars";
                    break;
                case 1:
                    theText = "1 minute\n" + theNumberOfCars + " cars, " + thePlatform;
                    break;
                default:
                    theText = "" + theMinutes + " minutes\n" + theNumberOfCars + " cars, " + thePlatform;
                    break;
            }
        } else {
            switch (theMinutes) {
                case 0:
                    theText = theDestination + "\nAt platform - " + theNumberOfCars + " cars";
                    break;
                case 1:
                    theText = theDestination + "\n1 minute - " + theNumberOfCars + " cars";
                    break;
                default:
                    theText = theDestination + "\n" + theMinutes + " minutes - " + theNumberOfCars + " cars";
                    break;
            }
        }
        return theText;
    }

    protected void populateByDestination() {
        final String[] theDirections = thePredictionGroups.keySet().toArray(new String[thePredictionGroups.size()]);
        Arrays.sort(theDirections);
        final int maxPredictions = TheApp.getMaxPredictionsPerStop();

        for (String aDirection : theDirections) {
            int predictionCount = 0;
            for (Prediction ourPrediction : thePredictionGroups.get(aDirection)) {
                if ((predictionCount++ < maxPredictions) || (maxPredictions == -1)) {
                    addItem(aDirection, ourPrediction);
                }
            }
        }
    }

    protected void populateByPlatform() {
        final int maxPredictions = TheApp.getMaxPredictionsPerStop();
        HashMap<String, ArrayList<Prediction>> thePredictions = new HashMap<String, ArrayList<Prediction>>();
        for (String aDestination : thePredictionGroups.keySet()) {
            ArrayList<Prediction> somePredictions = thePredictionGroups.get(aDestination);
            for (Prediction aPrediction : somePredictions) {
                if (!thePredictions.containsKey(aPrediction.getTheSubStopTag())) {
                    thePredictions.put(aPrediction.getTheSubStopTag(), new ArrayList<Prediction>());
                }
                thePredictions.get(aPrediction.getTheSubStopTag()).add(aPrediction);
            }
        }

        final String[] thePlatforms = thePredictions.keySet().toArray(new String[thePredictions.size()]);
        Arrays.sort(thePlatforms);

        for (String aPlatform : thePlatforms) {
            ArrayList<Prediction> thePlatformPredictions = thePredictions.get(aPlatform);
            Collections.sort(thePlatformPredictions, PredictionComparator.PREDICTION_ORDER);

            int i = 0;
            for (Prediction ourPrediction : thePlatformPredictions) {
                if ((i >= maxPredictions) && (maxPredictions != -1)) {
                    break;
                }
                addItem(aPlatform, ourPrediction);
                i++;
            }
        }
    }

    public void startPredictionFetch() {
    }

    public void finishedPullingPredictions(boolean wasCancelled) {
        if (wasCancelled || thePredictionFetcher.isCancelled()) {
            return;
        }

        try {
            theProgressDialog.dismiss();
        } catch (IllegalArgumentException theEx) {
            //E/AndroidRuntime(13912): java.lang.IllegalArgumentException: View not attached to window manager
            // Try to shut down gracefully
            onPause();
            return;
        }


        boolean groupByPlatform = TheApp.getPrefs().getBoolean(TheApp.getResString(R.string.pref_bart_prediction_group_key), TheApp.getResBool(R.bool.pref_bart_prediction_group_default));

        if (thePredictionGroups.isEmpty()) {
            //addItem(TheApp.getResString(R.string.text_no_predictions));
            return;
        }

        if (groupByPlatform) {
            populateByPlatform();
        } else {
            populateByDestination();
        }
        for (int i = 0; i < theExpandableListAdapter.getGroupCount(); i++) {
            theExpandableListView.expandGroup(i);
        }
    }

    public synchronized void addPrediction(final Prediction aPrediction) {
        if (thePredictionFetcher.isCancelled()) {
            Log.i(LOG_NAME, "We shouldn't be adding a prediction from a zombie.");
            return;
        }

        //        Log.i(LOG_NAME, "ADDING PREDICTION for: " + aPrediction.getAsString("destination"));
        String theDestination = aPrediction.getDirectionTag();
        if (!thePredictionGroups.containsKey(theDestination)) {
            //Log.i(LOG_NAME, "Creating group of: " + theDestination);
            thePredictionGroups.put(theDestination, new ArrayList<Prediction>());
        }
        thePredictionGroups.get(theDestination).add(aPrediction);
    }

    private synchronized void addItem(String aGroup, Prediction anItem) {
        theExpandableListAdapter.addItemToGroup(aGroup, anItem);
    }
}