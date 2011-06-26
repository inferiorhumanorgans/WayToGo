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
package com.zippy.WayToGo.Bookmarks;

import com.zippy.WayToGo.BaseActivityGroup;
import com.zippy.WayToGo.GPS.LocationFinder;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.Comparator.BookmarkByDistanceComparator;
import com.zippy.WayToGo.MainActivity;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.Stop;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author alex
 */
public class BookmarkActivity extends ListActivity implements LocationFinder.Listener {

    private static final String LOG_NAME = BookmarkActivity.class.getCanonicalName();
    private BookmarksDataHelper theDB;
    private final HashMap<Bookmark, Intent> theIntents = new HashMap<Bookmark, Intent>();
    private BookmarkAdapter theAdapter = null;
    private LocationFinder theFinder;
    private ProgressDialog theProgressDialog = null;
    private Timer theRotateTimer = null;
    private Timer theRefreshLocationTimer = null;
    private Timer theRefreshPredictionTimer = null;
    /**
     * How long to display each prediction summary on a stop.
     */
    private static final int CYCLE_PREDICTIONS_DURATION = 1000 * 4;
    private final ArrayList<AsyncTask> pendingTasks = new ArrayList<AsyncTask>(TheApp.theAgencies.size());
    private long lastLocationUpdate = 0;
    private Location theLocation = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_NAME, "onCreate");
        super.onCreate(savedInstanceState);
        registerForContextMenu(getListView());
        theFinder = new LocationFinder(this, this);
    }

    @Override
    public void onResume() {
        Log.d(LOG_NAME, "onResume");
        super.onResume();
        theFinder.init();

        if (theAdapter == null) {
            theAdapter = new BookmarkAdapter(this);
            this.setListAdapter(theAdapter);
        }

        for (final BaseAgency anAgency : TheApp.theAgencies.values()) {
            anAgency.init(this);
        }

        theAdapter.clear();

        theDB = new BookmarksDataHelper(this);
        ArrayList<Bookmark> theBookmarks = theDB.getAllBookmarks();
        for (Bookmark aBookmark : theBookmarks) {
            theIntents.put(aBookmark, aBookmark.getTheIntent());
            theAdapter.add(aBookmark);
        }

        startLocationSearch();
        killRotateTimer();
        killRefreshPredictionTimer();


        setupRotateTimer();
        setupRefreshPredictionTimer();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem anItem) {
        switch (anItem.getItemId()) {
            case R.id.menu_current_location:
                startLocationSearch();
                return true;
            default:
                return super.onOptionsItemSelected(anItem);
        }
    }

    protected void startLocationSearch() {
        if (lastLocationUpdate < (System.currentTimeMillis() - (TheApp.getPredictionRefreshDelay() / 2))) {
            if (theFinder.startLocationSearch()) {
                if (theProgressDialog != null) {
                    theProgressDialog.dismiss();
                }
                theProgressDialog = new ProgressDialog(this);
                theProgressDialog.setCancelable(true);
                theProgressDialog.setMessage(TheApp.getResString(R.string.loading_location));
                theProgressDialog.setIndeterminate(true);
                theProgressDialog.show();
            }
        } else {
            Collections.sort(theAdapter.getArray(), new BookmarkByDistanceComparator(theLocation));
            theAdapter.setTheLocation(theLocation);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (theDB != null) {
            theDB.cleanUp();
            theDB = null;
        }
        if (theProgressDialog != null) {
            theProgressDialog.dismiss();
            theProgressDialog = null;
        }
        theFinder.finish();
        killPendingTasks();
        killRotateTimer();
        killRefreshPredictionTimer();
        getParent().setProgressBarIndeterminateVisibility(false);

        for (final BaseAgency ourAgency : TheApp.theAgencies.values()) {
            ourAgency.finish();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu aMenu) {
        super.onPrepareOptionsMenu(aMenu);
        final MenuItem theMenuItem = aMenu.findItem(R.id.menu_agency_site);
        theMenuItem.setEnabled(false);
        theMenuItem.setVisible(false);
        return true;
    }

    @Override
    public void onCreateContextMenu(final ContextMenu aMenu, final View aView, final ContextMenuInfo someMenuInfo) {
        super.onCreateContextMenu(aMenu, aView, someMenuInfo);
        final MenuInflater ourInflater = getMenuInflater();
        ourInflater.inflate(R.menu.bookmark_context, aMenu);
        aMenu.setHeaderTitle(TheApp.getResString(R.string.context_title_generic));
    }

    @Override
    public boolean onContextItemSelected(final MenuItem anItem) {
        AdapterView.AdapterContextMenuInfo theInfo = (AdapterContextMenuInfo) anItem.getMenuInfo();
        final int position = theInfo.position;
        final Bookmark theBookmark = theAdapter.getItem(position);
        switch (anItem.getItemId()) {
            case R.id.context_view_stop_on_map:
                Intent myIntent = new Intent(getParent(), com.zippy.WayToGo.MapView.OneStopActivity.class);
                myIntent.putExtra("theStop", (Parcelable) theBookmark.getTheStop());
                this.startActivity(myIntent);
                return true;
            case R.id.context_directions_to:
                Stop.launchWalkingDirectionsTo(this, theBookmark.getTheStop());
                return true;
            case R.id.context_view_bookmark_prediction:
                showPredictionsForItem(position);
                return true;
            case R.id.context_view_bookmark_delete:
                theAdapter.remove(theBookmark);
                theDB.deleteBookmark(theBookmark);
                return true;
            default:
                return super.onContextItemSelected(anItem);
        }
    }

    @Override
    protected void onListItemClick(final ListView aListView, final View aView, final int position, final long anId) {
        showPredictionsForItem(position);
    }

    private void showPredictionsForItem(final int aPosition) {
        final Bookmark theBookmark = theAdapter.getItem(aPosition);

        /*
         * Since we use the class name as the tab ID now, we will
         * just use that to find the right activity group, and launch on that
         * otherwise just launch on the top level.
         */
        final MainActivity ourMainActivity = (MainActivity) getParent();
        Log.d(LOG_NAME, "Bookmark class is: " + theBookmark.getTheAgencyClass());
        BaseActivityGroup ag = (BaseActivityGroup) ourMainActivity.getActivityForTabTag(theBookmark.getTheAgencyClass());
        if (ag != null) {
            Log.d(LOG_NAME, "Creating on activity group: " + ag);
            ag.startChildActivity(theIntents.get(theBookmark).toUri(0), theIntents.get(theBookmark));
        } else {
            Log.d(LOG_NAME, "No activity group found??");
            startActivity(theIntents.get(theBookmark));
        }

    }

    @Override
    public void onLocationFound(final Location aLocation) {
        if (theProgressDialog != null) {
            theProgressDialog.dismiss();
            theProgressDialog = null;
        }

        theLocation = aLocation;

        Collections.sort(theAdapter.getArray(), new BookmarkByDistanceComparator(theLocation));
        theAdapter.setTheLocation(theLocation);
        lastLocationUpdate = System.currentTimeMillis();
    }

    @Override
    public void onLocationNotFound() {
        if (theProgressDialog != null) {
            theProgressDialog.dismiss();
            theProgressDialog = null;
        }
    }

    private void setupRotateTimer() {
        final boolean wantRefresh = TheApp.getPrefs().getBoolean(TheApp.getResString(R.string.pref_auto_refresh_key), TheApp.getResBool(R.bool.pref_auto_refresh_default));
        if (wantRefresh) {
            Log.i(LOG_NAME, "We're gonna set up a timer");
            theRotateTimer = new Timer();
            theRotateTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    runRotateTimer();
                }
            }, CYCLE_PREDICTIONS_DURATION, CYCLE_PREDICTIONS_DURATION);
        }

    }

    private void runRotateTimer() {
        //We call the method that will work with the UI
        //through the runOnUiThread method.
        runOnUiThread(new Runnable() {

            public void run() {
                theAdapter.rotate();
            }
        });
    }

    private void killRotateTimer() {
        if (theRotateTimer != null) {
            theRotateTimer.cancel();
            theRotateTimer.purge();
            theRotateTimer = null;
        }

    }

    private void setupRefreshLocationTimer() {
        final boolean wantRefresh = TheApp.getPrefs().getBoolean(TheApp.getResString(R.string.pref_auto_refresh_key), TheApp.getResBool(R.bool.pref_auto_refresh_default));
        if (wantRefresh) {
            Log.i(LOG_NAME, "Setting up refresh location timer");
            theRefreshLocationTimer = new Timer();
            theRefreshLocationTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    runRefreshLocationTimer();
                }
            }, TheApp.getPredictionRefreshDelay() / 2, TheApp.getPredictionRefreshDelay() / 2);
        }

    }

    private void runRefreshLocationTimer() {
        runOnUiThread(new Runnable() {

            public void run() {
                //theAdapter.rotate();
            }
        });
    }

    private void killRefreshLocationTimer() {
        if (theRefreshLocationTimer != null) {
            theRefreshLocationTimer.cancel();
            theRefreshLocationTimer.purge();
            theRefreshLocationTimer = null;
        }

    }

    private void setupRefreshPredictionTimer() {
        final boolean wantRefresh = TheApp.getPrefs().getBoolean(TheApp.getResString(R.string.pref_auto_refresh_key), TheApp.getResBool(R.bool.pref_auto_refresh_default));
        if (wantRefresh) {
            Log.i(LOG_NAME, "Setting up refresh predictions timer");
            theRefreshPredictionTimer = new Timer();
            theRefreshPredictionTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    runRefreshPredictionTimer();
                }
            }, 250, TheApp.getPredictionRefreshDelay());
        }

    }

    private void runRefreshPredictionTimer() {
        runOnUiThread(new Runnable() {

            public void run() {
                killPendingTasks();
                if (getParent() != null) {
                    getParent().setProgressBarIndeterminateVisibility(true);
                }
                for (final Bookmark ourBookmark : theAdapter.getArray()) {
                    final Stop ourStop = ourBookmark.getTheStop();
                    Log.d(LOG_NAME, "Adding task");
                    pendingTasks.add(ourStop.agency().fetchPredictionsForStop(ourStop, theAdapter));
                }

                // If we've no tasks, turn off the progress indicator
                // because we otherwise rely on the adapter to do this
                // but no bookmarks = that whole code path is skipped
                if (pendingTasks.isEmpty()) {
                    getParent().setProgressBarIndeterminateVisibility(false);
                }
            }
        });
    }

    private void killRefreshPredictionTimer() {
        if (theRefreshPredictionTimer != null) {
            theRefreshPredictionTimer.cancel();
            theRefreshPredictionTimer.purge();
            theRefreshPredictionTimer = null;
        }
    }

    private void killPendingTasks() {
        for (final AsyncTask aTask : pendingTasks) {
            if (aTask == null) {
                continue;
            }
            if (aTask.getStatus() != AsyncTask.Status.FINISHED) {
                aTask.cancel(true);
            }
        }
        pendingTasks.clear();
        theAdapter.clearPredictions();
    }
}
