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
//http://code.google.com/p/googletransitdatafeed/wiki/PublicFeeds
//http://www.caltrain.com/schedules/Mobile_Device_Schedules.html
package com.inferiorhumanorgans.WayToGo.Agency;

import com.inferiorhumanorgans.WayToGo.Agency.GTFS.GTFSAgency;
import android.content.Intent;
import android.content.res.Resources;
import com.inferiorhumanorgans.WayToGo.Agency.GTFS.SelectRouteActivity;
import com.inferiorhumanorgans.WayToGo.Agency.GTFS.SelectStopActivity;
import com.inferiorhumanorgans.WayToGo.BaseActivityGroup;
import com.inferiorhumanorgans.WayToGo.TheApp;

/**
 *
 * @author alex
 */
public class Caltrain extends GTFSAgency {

    private static final String LOG_NAME = Caltrain.class.getCanonicalName();

    protected static final String theLongName = "Caltrain";
    protected static final String theShortName = "Caltrain";
    protected static final String theURL = "http://www.caltrain.com";

    public Caltrain() {
        super();
        theGTFSName = "caltrain";

        /*
         * We look it up manually in case we're running a build without icons.
         * If that's the case we should use the agency's short name instead.
         */
        final Resources ourResources = TheApp.getContext().getResources();
        theLogoId = ourResources.getIdentifier("drawable/caltrain", null, "com.inferiorhumanorgans.WayToGo");
    }

    public static class ActivityGroup extends BaseActivityGroup {

        @Override
        public void onResume() {
            super.onResume();
            if (mIdList.isEmpty()) {
                final Intent ourIntent = new Intent(getParent(), RootActivity.class);
                ourIntent.putExtra("AgencyClassName", Caltrain.class.getCanonicalName());
                startChildActivity(RootActivity.class.getCanonicalName(), ourIntent);
            }
        }
    }

    protected static class RootActivity extends SelectStopActivity {

        public RootActivity() {
            super();
        }
    }
}
