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
package com.zippy.WayToGo.Agency;

import com.zippy.WayToGo.Agency.GTFS.GTFSAgency;
import android.content.Intent;
import com.zippy.WayToGo.Agency.GTFS.SelectRouteActivity;
import com.zippy.WayToGo.Agency.GTFS.SelectStopActivity;
import com.zippy.WayToGo.BaseActivityGroup;
import com.zippy.WayToGo.R;

/**
 *
 * @author alex
 */
public class Caltrain extends GTFSAgency {

    public Caltrain() {
        super();
        theGTFSName = "caltrain";
        theURL = "http://www.caltrain.com";
        theShortName = "Caltrain";
        theLongName = "Caltrain";
        theLogoId = R.drawable.caltrain;
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
