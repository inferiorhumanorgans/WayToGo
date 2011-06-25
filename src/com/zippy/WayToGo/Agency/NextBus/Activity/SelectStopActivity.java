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
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import com.zippy.WayToGo.GPS.LocationFinder;
import com.zippy.WayToGo.ListAdapter.StopAdapter;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.Direction;
import com.zippy.WayToGo.Util.Stop;
import java.util.ArrayList;
import junit.framework.Assert;

/**
 *
 * @author alex
 */
public class SelectStopActivity extends BaseNextBusActivity implements LocationFinder.Listener {

    /**
     * The name we use for logging statements.
     */
    private static final String LOG_NAME = SelectStopActivity.class.getCanonicalName();
    private Direction theDirection = null;
    private ArrayList<Stop> theStops = new ArrayList<Stop>();
    private LocationFinder theFinder;
    private Location theLocation;
    private long lastLocationUpdate = 0;

    @Override
    public void onCreate(Bundle aSavedInstanceState) {
        Log.d(LOG_NAME, "onCreate");
        super.onCreate(aSavedInstanceState);
        theFinder = new LocationFinder(getDialogContext(), this);

        theListAdapter = new StopAdapter(this);
        theListView.setAdapter(theListAdapter);
        theContextMenuId = R.menu.generic_stop_context;
        theOptionsMenuId = R.menu.nextbus_stop_menu;
    }

    @Override
    protected void onResume() {
        Log.d(LOG_NAME, "onResume");
        super.onResume();
        theFinder.init();

        final Intent intent = getIntent();
        theDirection = theAgency().getDirectionFromTag(intent.getStringExtra("directionTag"));

        registerForContextMenu(theListView);
        theIconView.setText(theDirection.getTheTitle());
        if (theStops.isEmpty()) {
            theStops = theAgency().getStopsForDirectionTag(theDirection.getTheTag());
            populateListView();
        } else {
            startLocationSearch();
        }
    }

    @Override
    protected void onPause() {
        Log.d(LOG_NAME, "onPause");
        super.onPause();
        cleanUp();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_NAME, "onDestroy");
        super.onDestroy();
        cleanUp();
    }

    private void cleanUp() {
        if (theProgressDialog != null) {
            theProgressDialog.dismiss();
            theProgressDialog = null;
        }
        unregisterForContextMenu(theListView);
        theAgency().finish();
        theFinder.finish();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem anItem) {
        switch (anItem.getItemId()) {
            case R.id.menu_current_location:
                startLocationSearch();
                return true;
            default:
                return super.onOptionsItemSelected(anItem);
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem anItem) {
        AdapterView.AdapterContextMenuInfo theInfo = (AdapterContextMenuInfo) anItem.getMenuInfo();
        final int ourListPosition = theInfo.position;
        final Stop ourStop = (Stop) theListAdapter.getItem(ourListPosition);
        switch (anItem.getItemId()) {
            case R.id.context_add_bookmark:
                addBookmarkForStop(ourStop);
                return true;
            case R.id.context_view_stop_on_map:
                final Intent ourIntent = new Intent(getParent(), com.zippy.WayToGo.MapView.OneStopActivity.class);
                ourIntent.putExtra("theStop", ourStop);
                startActivity(ourIntent);
                return true;
            case R.id.context_view_predictions_for_stop:
                showPredictionsForStop(ourStop);
                return true;
            case R.id.context_directions_to:
                Assert.assertEquals(false, getDialogContext() == null);
                Assert.assertEquals(false, theAgency() == null);
                Assert.assertEquals(false, ourStop == null);
                Stop.getWalkingDirectionsTo(getDialogContext(), ourStop);
                return true;
            default:
                return super.onContextItemSelected(anItem);
        }
    }

    private void populateListView() {
        populateListView(false);
    }

    /**
     * Call this with useLocation set to false for the initial list
     * If a list with distances from here is desired call it again with true.
     * @param useLocation Do we perform a location search nor not
     */
    private void populateListView(final boolean useLocation) {
        if (!useLocation) {
            for (final Stop aStop : theStops) {
                theListAdapter.add(aStop);
            }
            startLocationSearch();
        } else {
            final StopAdapter ourStopAdapter = (StopAdapter) theListAdapter;
            ourStopAdapter.setTheLocation(theLocation);
            final int ourClosest = ourStopAdapter.highlightClosest();

            if (ourClosest != Integer.MIN_VALUE) {
                theListView.setSelection(ourClosest);
            }
        }
    }

    protected void startLocationSearch() {
        if (lastLocationUpdate < (System.currentTimeMillis() - (TheApp.getPredictionRefreshDelay() / 2))) {
            theProgressDialog = new ProgressDialog(getDialogContext());
            theProgressDialog.setCancelable(true);
            theProgressDialog.setMessage(TheApp.getResString(R.string.loading_location));
            theProgressDialog.setIndeterminate(true);

            if (theFinder.startLocationSearch()) {
                theProgressDialog.show();
            } else {
                theProgressDialog = null;
            }
        } else {
            populateListView(true);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> aListView, View aRowView, int aPosition, long anId) {
        final StopAdapter theAdapter = (StopAdapter) aListView.getAdapter();
        final Stop theStop = theAdapter.getItem(aPosition);
        showPredictionsForStopAndDirection(theStop, theDirection);
    }

    @Override
    public void onLocationFound(Location aLocation) {
        Log.d(LOG_NAME, "Probably have a GPS Fix");
        if (theProgressDialog != null) {
            try {
                theProgressDialog.dismiss();
            } catch (java.lang.IllegalArgumentException theEx) {
                Log.e(LOG_NAME, "We really shouldn't be here: " + theEx.getMessage());
            } finally {
                theProgressDialog = null;
            }
        }
        theLocation = aLocation;
        populateListView(true);
        lastLocationUpdate = System.currentTimeMillis();
    }

    @Override
    public void onLocationNotFound() {
        if (theProgressDialog != null) {
            try {
                theProgressDialog.dismiss();
            } catch (java.lang.IllegalArgumentException theEx) {
                Log.e(LOG_NAME, "We really shouldn't be here: " + theEx.getMessage());
            } finally {
                theProgressDialog = null;
            }
        }
        theLocation = null;
    }
}
