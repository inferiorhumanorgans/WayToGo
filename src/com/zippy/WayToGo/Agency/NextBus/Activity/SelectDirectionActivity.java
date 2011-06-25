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

import com.zippy.WayToGo.ListAdapter.PairListAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import com.zippy.WayToGo.Util.StringPairList.StringPair;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.Direction;
import com.zippy.WayToGo.Util.Stop;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author alex
 */
public class SelectDirectionActivity extends BaseNextBusActivity {

    private final String LOG_NAME=SelectDirectionActivity.class.getCanonicalName();

    @Override
    public void onCreate(final Bundle aSavedInstanceState) {
        Log.d(LOG_NAME, "onCreate");
        super.onCreate(aSavedInstanceState);
        theOptionsMenuId = R.menu.nextbus_direction_menu;
    }

    @Override
    protected void onResume() {
        Log.d(LOG_NAME, "onResume");
        super.onResume();

        // Assume that the directions for a route won't change within the app's lifetime
        if (!theListAdapter.isEmpty()) {
            return;
        }

        final ArrayList<Direction> ourDirections = theAgency().getDirectionsForRouteTag(theRoute.getTheRawTag());

        final HashMap<String, Integer> ourDirectionTitles = new HashMap<String, Integer>(ourDirections.size());
        for (final Direction aDirection : ourDirections) {
            if (!ourDirectionTitles.containsKey(aDirection.getTheTitle())) {
                ourDirectionTitles.put(aDirection.getTheTitle(), Integer.valueOf(1));
            } else {
                Integer theIncrement = ourDirectionTitles.get(aDirection.getTheTitle()).intValue() + 1;
                ourDirectionTitles.put(aDirection.getTheTitle(), theIncrement);
            }
        }
        for (final Direction aDirection : ourDirections) {
            if (ourDirectionTitles.get(aDirection.getTheTitle()) > 1) {
                Stop theFirstStop = theAgency().getStopAtIndexOnDirection(aDirection.getTheTag(), 0);
                String theTitle = aDirection.getTheTitle() + "\nvia " + theFirstStop.getTheName();
                theListAdapter.add(new StringPair(theTitle, aDirection.getTheTag()));
            } else {
                theListAdapter.add(new StringPair(aDirection.getTheTitle(), aDirection.getTheTag()));
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> aListView, View aRowView, int aPosition, long anId) {
        Log.d(LOG_NAME, "CLICK");
        final PairListAdapter ourAdapter = (PairListAdapter) aListView.getAdapter();
        final StringPair ourDirection = ourAdapter.getItem(aPosition);
        final Intent ourIntent = new Intent(this, SelectStopActivity.class);
        ourIntent.putExtra("currentRouteTag", theRoute.getTheRawTag());
        ourIntent.putExtra("directionTag", ourDirection.second);
        Log.d(LOG_NAME, ourIntent.toURI());
        launchIntent(ourIntent);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem anItem) {
        switch (anItem.getItemId()) {
            case R.id.menu_settings:
                this.startActivity(TheApp.thePrefIntent);
                return true;
            default:
                return super.onOptionsItemSelected(anItem);
        }
    }
}
