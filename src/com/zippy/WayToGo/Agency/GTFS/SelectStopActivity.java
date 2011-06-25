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

import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.zippy.WayToGo.Comparator.StopComparator;
import com.zippy.WayToGo.GPS.LocationFinder;
import com.zippy.WayToGo.ListAdapter.StopAdapter;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.CopyDBListener;
import com.zippy.WayToGo.Util.Stop;
import java.util.ArrayList;

/**
 *
 * @author alex
 */
public class SelectStopActivity extends GTFSActivity implements CopyDBListener, LocationFinder.Listener {

    private static final String LOG_NAME = SelectStopActivity.class.getCanonicalName();
    private ProgressDialog theProgressDialog = null;
    private LocationFinder theFinder;
    private Location theLocation;
    private long lastLocationUpdate = 0;

    @Override
    public void onCreate(final Bundle aSavedInstanceState) {
        Log.d(LOG_NAME, "onCreate");
        super.onCreate(aSavedInstanceState);
        ((ViewGroup) (theListView.getParent())).removeView(theListView);
        setContentView(theListView);
        theListAdapter = new StopAdapter(this);
        theListView.setAdapter(theListAdapter);
        registerForContextMenu(theListView);
        theFinder = new LocationFinder(getDialogContext(), this);

        theContextMenuId = R.menu.generic_stop_context;
    }

    @Override
    public void onPostResume() {
        Log.d(LOG_NAME, "onPostResume");

        super.onPostResume();

        theFinder.init();
        if (theListAdapter.isEmpty()) {
            theAgency().copyDatabase(this);
        } else {
            startLocationSearch();
        }
    }

    @Override
    protected void onPause() {
        Log.d(LOG_NAME, "onPause");
        super.onPause();
        unregisterForContextMenu(theListView);
        theFinder.finish();
        if (theProgressDialog != null) {
            theProgressDialog.dismiss();
            theProgressDialog = null;
        }
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

    public void copyingStarted() {
        if (theProgressDialog != null) {
            theProgressDialog.dismiss();
            theProgressDialog = null;
        }
        theProgressDialog = new ProgressDialog(getDialogContext());
        theProgressDialog.setCancelable(true);
        theProgressDialog.setMessage(TheApp.getResString(R.string.loading_database));
        theProgressDialog.setIndeterminate(true);
        theProgressDialog.show();
    }

    public void copyingFinished() {
        if (theProgressDialog != null) {
            theProgressDialog.dismiss();
        }
        populateListView();
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
    public void onLocationFound(final Location aLocation) {
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
        lastLocationUpdate = System.currentTimeMillis();
        populateListView(true);
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
        final StopAdapter ourStopAdapter = (StopAdapter) theListAdapter;
        ourStopAdapter.setTheLocation(theLocation);
    }

    public void populateListView() {
        populateListView(false);
    }

    public void populateListView(final boolean useLocation) {
        final ArrayList<Stop> ourStops = theAgency().getStops(null);
        if (!useLocation) {
            theListAdapter.clear();

            for (Stop ourStop : ourStops) {
                theListAdapter.add(ourStop);
            }
            theListAdapter.sort(StopComparator.STOP_ORDER);
            theListAdapter.notifyDataSetChanged();
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

    @Override
    public void onItemClick(AdapterView<?> l, View v, int position, long id) {
        final StopAdapter theAdapter = (StopAdapter) l.getAdapter();
        showPredictionsForStop(theAdapter.getItem(position));
    }
}
