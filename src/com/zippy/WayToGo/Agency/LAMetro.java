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
package com.zippy.WayToGo.Agency;

import com.zippy.WayToGo.Agency.NextBus.NextBusAgency;
import android.content.Context;
import android.content.Intent;
import com.zippy.WayToGo.Agency.NextBus.Activity.SelectRouteActivity;
import com.zippy.WayToGo.Agency.NextBus.NextBusDataHelper;

/**
 *
 * @author alex
 */
public class LAMetro extends NextBusAgency {

    public LAMetro() {
        super();
        theNBName = "lametro";
        theURL = "http://www.metro.net/";
        theShortName = "LA Metro";
        theLongName = "Los Angeles County Metropolitan Transportation Authority";
    }

    @Override
    public void init(Context aContext) {
        super.init(aContext);
        if (theDBHelper == null) {
            theDBHelper = (NextBusDataHelper) setTheDBHelper(new LAMetroDataHelper(theContext, this));
        }
    }

    public static class ActivityGroup extends com.zippy.WayToGo.ActivityGroup {

        @Override
        public void onResume() {
            super.onResume();
            if (mIdList.isEmpty()) {
                final Intent ourIntent = new Intent(getParent(), RootActivity.class);
                ourIntent.putExtra("AgencyClassName", LAMetro.class.getCanonicalName());
                startChildActivity(RootActivity.class.getCanonicalName(), ourIntent);
            }
        }
    }

    protected static class RootActivity extends SelectRouteActivity {

        public RootActivity() {
            super();
        }
    }

    private static final class LAMetroDataHelper extends NextBusDataHelper {

        public LAMetroDataHelper(final Context aContext, final NextBusAgency anAgency) {
            super(aContext, anAgency);
        }

        @Override
        protected String getSanitizedRouteName(final String aRouteTitle) {
            final String correctedStr = aRouteTitle.replaceFirst("[0-9a-zA-Z]*\\s", "").replaceAll("\\s?-\\s?", "-").replaceAll("(Csu|Cal\\. State Univ\\.)", "CSU").replaceAll("(Dtwn|Dwntwn)", "Downtown").replaceAll("Hllywd", "Hollywood").replaceAll("Tran Ctr", "Transit Center").replaceAll("Ctr", "Center").replaceAll("Cty", "City").replaceAll("Hwy", "Highway").replaceAll("Pk", "Park").replaceAll("Rck", "Rock").replaceAll("Rd", "Road").replaceAll("Hawthrn", "Hawthorne").replaceAll("Av(\\s|$|-)", "Ave$1").replaceAll("Sta(\\s|$|-)", "Station$1").replaceAll("Jpl", "Jet Propulsion Laboratory").replaceAll("S Madre", "Sierra Madre").replaceAll("Sm Vlla", "Sierra Madre Villa").replaceAll("Lax", "LAX").replaceAll("Sbay Gallria", "S. Bay Galleria Transit Center").replaceAll("Latijera", "La Tijera").replaceAll("W Hollywood", "West Hollywood");
            final String mainRoutes = correctedStr.replaceFirst("(?im)(\\s)via.*", "").replaceAll("(?m)-", "\n").replaceAll("(?m)I\n([0-9]{1,3}) Fwy", "I-$1 Freeway");
            final String viaRoutes;
            if (correctedStr.contains("Via")) {
                viaRoutes = "\n" + correctedStr.replaceAll("\\s?-\\s?", " / ").replaceFirst("(?i)^.*\\svia", "Via");
            } else {
                viaRoutes = "";
            }
            return mainRoutes + viaRoutes;
        }
    }
}
