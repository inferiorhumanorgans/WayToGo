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
package com.inferiorhumanorgans.WayToGo.Agency;

import com.inferiorhumanorgans.WayToGo.Agency.NextBus.NextBusAgency;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import com.inferiorhumanorgans.WayToGo.Agency.NextBus.Activity.SelectRouteActivity;
import com.inferiorhumanorgans.WayToGo.Agency.NextBus.NextBusDataHelper;
import com.inferiorhumanorgans.WayToGo.BaseActivityGroup;
import com.inferiorhumanorgans.WayToGo.TheApp;

/**
 *
 * @author alex
 */
public class ACTransit extends NextBusAgency {

    private static final String LOG_NAME = ACTransit.class.getCanonicalName();

    protected static final String theLongName = "AC Transit";
    protected static final String theShortName = "AC Transit";
    protected static final String theURL = "http://www.actransit.org/";

    public ACTransit() {
        super();
        theNBName = "actransit";

        /*
         * We look it up manually in case we're running a build without icons.
         * If that's the case we should use the agency's short name instead.
         */
        final Resources ourResources = TheApp.getContext().getResources();
        theLogoId = ourResources.getIdentifier("drawable/actransit", null, "com.inferiorhumanorgans.WayToGo");
    }

    @Override
    public void init(final Context aContext) {
        super.init(aContext);
        if (theDBHelper == null) {
            theDBHelper = (NextBusDataHelper) setTheDBHelper(new ACTransitDataHelper(theContext, this));
        }
    }

    public static class ActivityGroup extends BaseActivityGroup {

        @Override
        public void onResume() {
            super.onResume();
            if (mIdList.isEmpty()) {
                final Intent ourIntent = new Intent(getParent(), RootActivity.class);
                ourIntent.putExtra("AgencyClassName", ACTransit.class.getCanonicalName());
                startChildActivity(RootActivity.class.getCanonicalName(), ourIntent);
            }
        }
    }

    protected static class RootActivity extends SelectRouteActivity {

        public RootActivity() {
            super();
        }
    }

    private static final class ACTransitDataHelper extends NextBusDataHelper {
        public ACTransitDataHelper(final Context aContext, final NextBusAgency anAgency) {
            super(aContext, anAgency);
        }

        @Override
        protected String getSanitizedStopName(final String aStopTitle) {
            return aStopTitle
                    .replaceAll("Bart(\\sStation|\\sStat$)?", "BART")
                    .replaceAll("Av(\\s|$)", "Ave$1")
                    .replaceFirst("Wesleyway$", "Wesley Way")
                    .replaceFirst("\\(([^\\)]*)(?!\\))$", "($1)")
                    .replaceFirst("(Sc|Sch|Scho|Schoo)\\)", "School)")
                    .replaceAll("\\'S", "'s")
                    .replaceAll("P\\s\\&\\sR", "Park and Ride")
                    .replaceAll("Jrway", "Jr Way")
                    .replaceAll("Nr\\s", "Near ")
                    .replaceAll("U\\.s\\.", "US")
                    .replaceAll("Cr(\\)|\\s|$)", "Crossing$1")
                    .replaceAll("Hgh SChool", "High School")
                    .replaceAll("Oakland Technical Hi\\)", "Oakland Technical High School)")
                    .replaceAll("In Middle Of Traffic Is\\)", "Middle of Traffic Island)")
                    .replaceAll("Simmons Middl\\)", "Simmons Middle School)")
                    .replaceAll("(?i)Mlk\\s", "Martin Luther King ")
                    .replaceAll("(Embarcadero\\sW)(\\s|$)", "$1est$2")
                    .replaceAll("(\\(.*BART\\))$", "");
        }

        @Override
        protected String getSanitizedDirection(String aDirectionTitle) {
            return aDirectionTitle
                    .replaceAll("(Blvd|Ct|St|Ave|Dr|Rd|Fwy|Pkwy|Terr)\\.", "$1")
                    .replaceAll("U.C. Campus", "UC Berkeley")
                    .replaceAll("Tenth", "10th");
        }

        @Override
        protected String getTerseDirectionName(final String aDirectionTitle) {
            return aDirectionTitle
                    .replaceAll("^[Tt]o\\s", "")
                    .replaceAll("(?i)Counterclockwise", "CCW")
                    .replaceAll("(?i)Clockwise", "CW")
                    .replaceAll("Jc Penney", "JC Penney");
        }
    }
}
