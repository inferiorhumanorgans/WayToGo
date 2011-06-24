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
package com.zippy.WayToGo.Agency.NextBus;

import com.zippy.WayToGo.Agency.NextBus.Activity.BaseNextBusActivity;
import android.os.Bundle;
import android.widget.ExpandableListView;
import com.zippy.WayToGo.ListAdapter.ExpandableArrayAdapter;
import com.zippy.WayToGo.Comparator.RouteComparator;
import com.zippy.WayToGo.Comparator.StopComparator;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.Direction;
import com.zippy.WayToGo.Util.Route;
import com.zippy.WayToGo.Util.Stop;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author alex
 */
public class DebugNames extends BaseNextBusActivity {

    protected ExpandableListView theExListView;
    protected ExpandableArrayAdapter<String> theExListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(TheApp.getAppTitle());

        theExListAdapter = new ExpandableArrayAdapter<String>(this);
        theExListView = new ExpandableListView(this);
        //theExListView.setOnItemClickListener(this);
        //theExListView.setOnItemLongClickListener(this);
        theExListView.setAdapter(theExListAdapter);
        setContentView(theExListView);
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO) {
            theExListView.setGroupIndicator(null);
        }

        final ArrayList<Route> theRoutes = theAgency().getRoutes();
        Collections.sort(theRoutes, RouteComparator.ROUTE_ORDER);

        final ArrayList<Direction> theDirections = theAgency().getDirections();


        for (final Route aRoute : theRoutes) {
            theExListAdapter.addItemToGroup("Routes", aRoute.getTheName());
        }

        final ArrayList<Stop> theStops = theAgency().getStops(null);
        Collections.sort(theStops, StopComparator.STOP_ORDER);
        for (final Stop aStop : theStops) {
            theExListAdapter.addItemToGroup("Stops", aStop.getTheName());
        }

        for (final Direction aDirection : theDirections) {
            if (aDirection.getTheTitle().startsWith("Inbound")) {
                theExListAdapter.addItemToGroup("Directions Inbound", aDirection.getTheTitle());
            } else if (aDirection.getTheTitle().startsWith("Outbound")) {
                theExListAdapter.addItemToGroup("Directions Outbound", aDirection.getTheTitle());
            } else {
                theExListAdapter.addItemToGroup("Directions", aDirection.getTheTitle());
            }
        }
    }
}
