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

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import com.inferiorhumanorgans.WayToGo.Agency.NextBus.NextBusAgency;
import com.inferiorhumanorgans.WayToGo.BaseActivity;
import com.inferiorhumanorgans.WayToGo.ListAdapter.PairListAdapter;
import com.inferiorhumanorgans.WayToGo.R;
import com.inferiorhumanorgans.WayToGo.TheApp;
import com.inferiorhumanorgans.WayToGo.Util.Route;
import com.inferiorhumanorgans.WayToGo.Widget.IconTextView;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alex
 */
public abstract class BaseNextBusActivity extends BaseActivity {

    private final static String LOG_NAME = BaseNextBusActivity.class.getCanonicalName();
    protected Route theRoute = null;
    protected IconTextView theIconView;
    protected ProgressDialog theProgressDialog;

    @Override
    public void onCreate(final Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        Log.d(LOG_NAME, "onCreate");
        setContentView(R.layout.nextbus_new_layout);

        theListAdapter = new PairListAdapter(this);
        theListView = (ListView) findViewById(R.id.nb_direction_listview);
        theListView.setOnItemClickListener(this);
        theListView.setOnItemLongClickListener(this);
        theListView.setAdapter(theListAdapter);
        theIconView = (IconTextView) findViewById(R.id.nb_icon_view);
    }

    @Override
    protected NextBusAgency theAgency() {
        return (NextBusAgency) TheApp.theAgencies.get(theAgencyClassName);
    }

    @Override
    protected void onResume() {
        Log.d(LOG_NAME, "onResume");
        super.onResume();

        final Intent theIntent = getIntent();

        if (theAgencyClassName == null) {
            theAgencyClassName = theIntent.getStringExtra("AgencyClassName");


            // TODO: Check if theAgency() is null, if so pop up a dialog box
            // and go to another tab (if in a tab context) or return to previous
            // activity
            theAgency().init(getDialogContext());
            setTitle(TheApp.getAppTitle() + " - " + theAgency().getLongName());

            theRoute = theAgency().getRouteFromTag(theIntent.getStringExtra("currentRouteTag"));

            theIconView.setTheAgency(theAgency());


            if (theRoute != null) {
                theIconView.setBadgeText(theRoute.rawTag());
                theIconView.setText(theRoute.name());
            }
        } else {
            theAgency().init(getDialogContext());
        }
    }

    @Override
    protected void onPause() {
        Log.d(LOG_NAME, "onPause");
        super.onPause();
        if (theAgency() != null) {
            theAgency().finish();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_NAME, "onDestroy");
        super.onDestroy();
        if (theAgency() != null) {
            theAgency().finish();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem anItem) {
        switch (anItem.getItemId()) {
            case R.id.menu_help:
                final Intent ourIntent = new Intent("w2g.action.NextBus.DEBUG");
                launchIntent(ourIntent);
                return true;
            default:
                return super.onOptionsItemSelected(anItem);
        }
    }
}
