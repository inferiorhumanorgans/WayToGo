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
package com.inferiorhumanorgans.WayToGo.Agency.NextBus.Activity;

import com.inferiorhumanorgans.WayToGo.ListAdapter.PairListAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import com.inferiorhumanorgans.WayToGo.Util.StringPairList.StringPair;
import com.inferiorhumanorgans.WayToGo.R;
import com.inferiorhumanorgans.WayToGo.TheApp;
import com.inferiorhumanorgans.WayToGo.Util.Direction;
import com.inferiorhumanorgans.WayToGo.Util.Stop;
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

        final ArrayList<Direction> ourDirections = theAgency().getDirectionsForRouteTag(theRoute.rawTag());

        final HashMap<String, Integer> ourDirectionTitles = new HashMap<String, Integer>(ourDirections.size());
        for (final Direction aDirection : ourDirections) {
            if (!ourDirectionTitles.containsKey(aDirection.title())) {
                ourDirectionTitles.put(aDirection.title(), Integer.valueOf(1));
            } else {
                Integer theIncrement = ourDirectionTitles.get(aDirection.title()).intValue() + 1;
                ourDirectionTitles.put(aDirection.title(), theIncrement);
            }
        }
        for (final Direction aDirection : ourDirections) {
            if (ourDirectionTitles.get(aDirection.title()) > 1) {
                Stop theFirstStop = theAgency().getStopAtIndexOnDirection(aDirection.tag(), 0);
                String theTitle = aDirection.title() + "\nvia " + theFirstStop.name();
                theListAdapter.add(new StringPair(theTitle, aDirection.tag()));
            } else {
                theListAdapter.add(new StringPair(aDirection.title(), aDirection.tag()));
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> aListView, View aRowView, int aPosition, long anId) {
        Log.d(LOG_NAME, "CLICK");
        final PairListAdapter ourAdapter = (PairListAdapter) aListView.getAdapter();
        final StringPair ourDirection = ourAdapter.getItem(aPosition);
        final Intent ourIntent = new Intent(this, SelectStopActivity.class);
        ourIntent.putExtra("currentRouteTag", theRoute.rawTag());
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
