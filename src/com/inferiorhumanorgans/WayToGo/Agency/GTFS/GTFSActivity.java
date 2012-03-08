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
package com.inferiorhumanorgans.WayToGo.Agency.GTFS;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import com.inferiorhumanorgans.WayToGo.BaseActivity;
import com.inferiorhumanorgans.WayToGo.ListAdapter.PairListAdapter;
import com.inferiorhumanorgans.WayToGo.R;
import com.inferiorhumanorgans.WayToGo.TheApp;
import com.inferiorhumanorgans.WayToGo.Widget.IconTextView;

/**
 *
 * @author alex
 */
public class GTFSActivity extends BaseActivity {

    private final static String LOG_NAME = GTFSActivity.class.getCanonicalName();
    protected IconTextView theIconView;

    @Override
    protected GTFSAgency theAgency() {
        return (GTFSAgency) TheApp.theAgencies.get(theAgencyClassName);
    }

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
    protected void onResume() {
        Log.d(LOG_NAME, "onResume");
        super.onResume();

        final Intent ourIntent = getIntent();

        if (theAgencyClassName == null) {
            theAgencyClassName = ourIntent.getStringExtra("AgencyClassName");


            // TODO: Check if theAgency() is null, if so pop up a dialog box
            // and go to another tab (if in a tab context) or return to previous
            // activity
            theAgency().init(getDialogContext());
            setTitle(TheApp.getAppTitle() + " - " + theAgency().getLongName());
        } else {
            theAgency().init(getDialogContext());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (theAgency() != null) {
            theAgency().finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (theAgency() != null) {
            theAgency().finish();
        }
    }
}
