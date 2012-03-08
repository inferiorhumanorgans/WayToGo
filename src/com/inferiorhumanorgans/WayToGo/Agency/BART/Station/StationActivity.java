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
//http://code.google.com/p/android/issues/detail?id=2483
package com.inferiorhumanorgans.WayToGo.Agency.BART.Station;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.inferiorhumanorgans.WayToGo.Agency.BART.BaseBARTActivity;
import com.inferiorhumanorgans.WayToGo.Agency.BART.Route.RouteFetchListener;
import com.inferiorhumanorgans.WayToGo.R;
import com.inferiorhumanorgans.WayToGo.TheApp;
import com.inferiorhumanorgans.WayToGo.GPS.LocationFinder;
import com.inferiorhumanorgans.WayToGo.ListAdapter.StopAdapter;
import com.inferiorhumanorgans.WayToGo.Util.CopyDBListener;
import com.inferiorhumanorgans.WayToGo.Util.Stop;

/**
 *
 * @author alex
 */
public class StationActivity extends BaseBARTActivity implements CopyDBListener, RouteFetchListener, AdapterView.OnItemClickListener, LocationFinder.Listener {

    private static final String LOG_NAME = StationActivity.class.getCanonicalName();
    private LocationFinder theFinder;
    private Location theLocation;
    private long lastLocationUpdate = 0;

    public StationActivity() {
        super();
        this.theContextMenuId = R.menu.generic_stop_context;
    }

    @Override
    public void onCreate(final Bundle aSavedInstanceState) {
        Log.d(LOG_NAME, "onCreate");
        super.onCreate(aSavedInstanceState);

        theAgency().init(this);
        theFinder = new LocationFinder(getParent(), this);
        theListView = new ListView(this);
        theListAdapter = new StopAdapter(this);
        theListView.setAdapter(theListAdapter);
        theListView.setOnItemClickListener(this);
        setContentView(theListView);
        theAgency().setRouteFetchListener(this);
    }

    @Override
    public void onResume() {
        Log.d(LOG_NAME, "onResume");
        super.onResume();

        registerForContextMenu(theListView);

        theAgency().init(this);
        theFinder.init();

        StopAdapter theAdapter = (StopAdapter) theListView.getAdapter();
        theAdapter.clear();
        theAgency().copyDatabase(this);
    }

    @Override
    public void onPause() {
        Log.d(LOG_NAME, "onPause()");
        if (theProgressDialog != null) {
            theProgressDialog.dismiss();
            theProgressDialog = null;
        }
        theAgency().finish();
        theFinder.finish();
        unregisterForContextMenu(theListView);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem anItem) {
        switch (anItem.getItemId()) {
            case R.id.menu_agency_site:
                final Intent ourBrowserIntent = new Intent("android.intent.action.VIEW", Uri.parse(theAgency().getURL()));
                startActivity(ourBrowserIntent);
                return true;
            case R.id.menu_current_location:
                startLocationSearch();
                return true;
            default:
                return super.onOptionsItemSelected(anItem);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final StopAdapter theAdapter = (StopAdapter) theListView.getAdapter();
        final Stop theStation = theAdapter.getItem(position);
        launchIntent(theAgency().getPredictionIntentForStop(theStation));
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

        if (theAgency().getNumberOfStations() == 0) {
            theAgency().refreshStations();
        } else {
            // load from the db
            populateStationListView();
        }
    }

    public void currentlyFetchingRouteList() {
        if (theProgressDialog != null) {
            theProgressDialog.dismiss();
        }
        theProgressDialog = new ProgressDialog(getParent());
        theProgressDialog.setCancelable(true);
        theProgressDialog.setMessage(TheApp.getResString(R.string.loading_route_list));
        theProgressDialog.setIndeterminate(true);
        theProgressDialog.show();
    }

    public void finishedProcessingRouteList() {
        if (theProgressDialog != null) {
            theProgressDialog.dismiss();
        }
        populateStationListView();
    }

    public void currentlyFetchingStationList() {
        if (theProgressDialog != null) {
            theProgressDialog.dismiss();
        }
        theProgressDialog = new ProgressDialog(getParent());
        theProgressDialog.setCancelable(true);
        theProgressDialog.setMessage(TheApp.getResString(R.string.loading_station_list));
        theProgressDialog.setIndeterminate(true);
        theProgressDialog.show();
    }

    public void finishedProcessingStationList() {
        if (theProgressDialog != null) {
            theProgressDialog.dismiss();
        }
    }

    private void populateStationListView() {
        populateStationListView(false);
    }

    private void populateStationListView(final boolean useLocation) {
        StopAdapter theAdapter = (StopAdapter) theListView.getAdapter();
        if (!useLocation) {
            theAdapter.clear();
            for (final Stop aStop : theAgency().getStops(null)) {
                theAdapter.add(aStop);
            }
            startLocationSearch();
        } else {
            theAdapter.setTheLocation(theLocation);
            final int ourClosest = theAdapter.highlightClosest();
            if (ourClosest != Integer.MIN_VALUE) {
                theListView.setSelection(ourClosest);
            }
        }
    }

    protected void startLocationSearch() {
        if (lastLocationUpdate < (System.currentTimeMillis() - (TheApp.getPredictionRefreshDelay() / 2))) {
            if (theProgressDialog != null) {
                theProgressDialog.dismiss();
            }
            theProgressDialog = new ProgressDialog(getParent());
            theProgressDialog.setCancelable(true);
            theProgressDialog.setMessage(TheApp.getResString(R.string.loading_location));
            theProgressDialog.setIndeterminate(true);
            if (theFinder.startLocationSearch()) {
                theProgressDialog.show();
            } else {
                theProgressDialog = null;
            }
        } else {
            populateStationListView(true);
        }

    }

    @Override
    public void onLocationFound(final Location aLocation) {
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
        populateStationListView(true);
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
