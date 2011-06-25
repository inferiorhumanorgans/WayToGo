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

import com.zippy.WayToGo.Util.CopyDBListener;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.zippy.WayToGo.Agency.NextBus.NextBusRouteFetchListener;
import com.zippy.WayToGo.ListAdapter.RouteBadgeAdapter;
import com.zippy.WayToGo.Comparator.RouteComparator;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.Direction;
import com.zippy.WayToGo.Util.Route;
import java.util.ArrayList;

/**
 *
 * @author alex
 */
public class SelectRouteActivity extends BaseNextBusActivity implements CopyDBListener, NextBusRouteFetchListener {

    private static final String LOG_NAME = SelectRouteActivity.class.getSimpleName();
    private boolean needsRefresh = true;

    @Override
    public void onCreate(Bundle aSavedInstanceState) {
        Log.d(LOG_NAME, "onCreate");
        super.onCreate(aSavedInstanceState);

        theListView = new ListView(this);
        theListView.setOnItemClickListener(this);
        setContentView(theListView);

        RouteBadgeAdapter theBadgeAdapter = new RouteBadgeAdapter(this);
        theListView.setAdapter(theBadgeAdapter);
    }

    @Override
    public void onResume() {
        Log.d(LOG_NAME, "onResume");
        super.onResume();
        // Insert loading thing? dunno
    }

    @Override
    public void onPostResume() {
        Log.d(LOG_NAME, "onPostResume");

        super.onPostResume();

        final RouteBadgeAdapter ourAdapter = (RouteBadgeAdapter) theListView.getAdapter();

        if (ourAdapter.isEmpty()) {
            theAgency().setRouteFetchListener(this);
            ourAdapter.setTheAgency(theAgency());

            theAgency().copyDatabase(this);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu aMenu) {
        super.onPrepareOptionsMenu(aMenu);
        final MenuItem theMenuItem = aMenu.findItem(R.id.menu_current_location);
        theMenuItem.setEnabled(false);
        theMenuItem.setVisible(false);
        return true;
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
        if (theAgency().getNumberOfRoutes() == 0) {
            theAgency().refreshRoutes();
        } else {
            populateRoutesListView();
        }
    }

    @Override
    public void onPause() {
        Log.d(LOG_NAME, "onPause");
        super.onPause();
        if (theProgressDialog != null) {
            theProgressDialog.dismiss();
        }
    }

    public void populateRoutesListView() {
        final RouteBadgeAdapter theAdapter = (RouteBadgeAdapter) theListView.getAdapter();
        theAdapter.clear();
        final ArrayList<Route> theRoutes = theAgency().getRoutes();
        for (Route aRoute : theRoutes) {
            theAdapter.add(aRoute);
        }
        theAdapter.sort(RouteComparator.ROUTE_ORDER);
        theAdapter.notifyDataSetChanged();
    }

    public void currentlyFetchingRouteList() {
        theProgressDialog = new ProgressDialog(getDialogContext());
        theProgressDialog.setCancelable(true);
        theProgressDialog.setMessage("Loading route list for " + theAgency().getShortName() + "...");
        theProgressDialog.setIndeterminate(true);
        theProgressDialog.show();
    }

    public void finishedProcessingRouteList() {
        theProgressDialog.dismiss();
    }

    public void currentlyFetchingRouteConfig(int forThisManyRoutes) {
        theProgressDialog = new ProgressDialog(getDialogContext());
        theProgressDialog.setCancelable(true);
        theProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        theProgressDialog.setTitle(theAgency().getShortName());
        theProgressDialog.setMessage("Loading detailed route info...");
        theProgressDialog.setIndeterminate(false);
        theProgressDialog.setMax(forThisManyRoutes);
        theProgressDialog.setProgress(0);
        theProgressDialog.show();
    }

    public void finishedProcessingOneRoute() {
        theProgressDialog.incrementProgressBy(1);
    }

    public void finishedProcessingAllRoutes() {
        theProgressDialog.dismiss();
        populateRoutesListView();
    }

    @Override
    public void onItemClick(AdapterView<?> aListView, View aRowView, int aPosition, long anId) {
        RouteBadgeAdapter theAdapter = (RouteBadgeAdapter) theListView.getAdapter();
        theRoute = theAdapter.getItem(aPosition);

        ArrayList<Direction> theDirections = theAgency().getDirectionsForRouteTag(theRoute.getTheRawTag());
        if (theDirections.size() <= 1) {

            final Direction theDirection = theDirections.get(0);
            final Intent myIntent = new Intent(getParent(), SelectStopActivity.class);
            myIntent.putExtra("currentRouteTag", theRoute.getTheRawTag());
            myIntent.putExtra("directionTag", theDirection.getTheTag());

            launchIntent(myIntent);
            return;
        }

        Intent myIntent = new Intent(this, SelectDirectionActivity.class);
        myIntent.putExtra("currentRouteTag", theRoute.getTheRawTag());
        launchIntent(myIntent);
    }
}
