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
package com.zippy.WayToGo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.Bookmarks.BookmarksDataHelper;
import com.zippy.WayToGo.Util.Direction;
import com.zippy.WayToGo.Util.Stop;
import com.zippy.WayToGo.Util.StringPairList.StringPair;
import junit.framework.Assert;

/**
 *
 * @author alex
 */
public abstract class BaseActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String LOG_NAME = BaseActivity.class.getCanonicalName();
    protected ListView theListView;
    protected ArrayAdapter theListAdapter;
    protected String theAgencyClassName = null;

    /**
     * This is set to main_menu so we don't have to manually set the options menu
     * in all of the top level activities.
     */
    protected int theOptionsMenuId = R.menu.main_menu;
    protected int  theContextMenuId = 0;
    protected String theContextMenuTitle = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onItemClick(AdapterView<?> aParent, View aView, int aPosition, long anId) {
        return;
    }

    public boolean onItemLongClick(AdapterView<?> aParent, View aView, int aPosition, long anId) {
        return false;
    }

    protected BaseAgency theAgency() {
        return TheApp.theAgencies.get(theAgencyClassName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu aMenu) {
        Log.d(LOG_NAME, "onCreateOptionsMenu called");
        if (theOptionsMenuId == 0) {
            Log.d(LOG_NAME, "No options menu set");
            return false;
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(theOptionsMenuId, aMenu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu aMenu, View aView, ContextMenuInfo someMenuInfo) {
        Log.d(LOG_NAME, "onCreateContextMenu called");
        if (theContextMenuId == 0) {
            Log.d(LOG_NAME, "No context menu set");
            return;
        }
        super.onCreateContextMenu(aMenu, aView, someMenuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(theContextMenuId, aMenu);
        if (theContextMenuTitle != null) {
            aMenu.setHeaderTitle(theContextMenuTitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem anItem) {
        switch (anItem.getItemId()) {
            case R.id.menu_settings:
                this.startActivity(TheApp.thePrefIntent);
                return true;
            case R.id.menu_agency_site:
                final Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(theAgency().getURL()));
                startActivity(browserIntent);
                return true;
            default:
                return super.onOptionsItemSelected(anItem);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem anItem) {
        final AdapterView.AdapterContextMenuInfo ourContextMenuInfo = (AdapterContextMenuInfo) anItem.getMenuInfo();
        final int position = ourContextMenuInfo.position;
        final StringPair ourStopPair;
        final Stop ourStop;
        final Object ourItem = theListAdapter.getItem(position);
        if (ourItem instanceof StringPair) {
            ourStopPair = (StringPair) ourItem;
            ourStop = theAgency().getStop(ourStopPair.second);
        } else if (ourItem instanceof Stop) {
            ourStop = (Stop) ourItem;
        } else {
            ourStop = null;
        }

        switch (anItem.getItemId()) {
            case R.id.context_add_bookmark:
                final BookmarksDataHelper theDB = new BookmarksDataHelper(this);
                final StringPair theBookmark = new StringPair(theAgency().getClass().getCanonicalName(), ourStop.stopId());
                theDB.addBookmark(theBookmark);
                theDB.cleanUp();
                return true;
            case R.id.context_view_stop_on_map:
                final Intent myIntent = new Intent(getParent(), com.zippy.WayToGo.MapView.OneStopActivity.class);
                myIntent.putExtra("theStop", ourStop);
                startActivity(myIntent);
                return true;
            case R.id.context_view_predictions_for_stop:
                showPredictionsForStop(ourStop);
                return true;
            case R.id.context_directions_to:
                Assert.assertNotNull(getDialogContext());
                Assert.assertNotNull(theAgency());
                Assert.assertNotNull(ourStop);
                Stop.launchWalkingDirectionsTo(getDialogContext(), ourStop);
                return true;
            default:
                return super.onContextItemSelected(anItem);
        }
    }

    protected void showPredictionsForStop(final Stop aStop) {
        launchIntent(theAgency().getPredictionIntentForStop(aStop));
    }

    protected void showPredictionsForStopAndDirection(final Stop aStop, final Direction aDirection) {
        launchIntent(theAgency().getPredictionIntentForStop(aStop, aDirection));
    }

    /**
     * Launches an intent within an activity group if we're in the tab view,
     * otherwise as a top-level intent
     * @param anIntent
     */
    protected final void launchIntent(Intent anIntent) {
        theAgency().finish();
        anIntent.putExtra("AgencyClassName", theAgencyClassName);

        BaseActivityGroup ag = (BaseActivityGroup) getParent();
        if (ag != null) {
            Log.d(LOG_NAME, "Launching in an activity group");
            ag.startChildActivity(anIntent.toURI(), anIntent);
        } else {
            Log.d(LOG_NAME, "Launching as a top level");
            this.startActivity(anIntent);
        }
    }

    /**
     * Get a context suitable for putting dialog boxes on.  If we're in an
     * activity group, this is our parent, not ourselves.
     * @return
     */
    protected final Activity getDialogContext() {
        Activity ourContext = getParent();
        if (ourContext == null) {
            return this;
        }
        return ourContext;
    }

    protected final void addBookmarkForStop(final Stop aStop) {
        final BookmarksDataHelper theDB = new BookmarksDataHelper(this);
        final StringPair theBookmark = new StringPair(aStop.agency().getClass().getCanonicalName(), aStop.stopId());
        theDB.addBookmark(theBookmark);
        theDB.cleanUp();
    }
}
