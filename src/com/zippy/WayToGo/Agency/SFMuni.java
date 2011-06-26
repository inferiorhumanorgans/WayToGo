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
import android.graphics.Color;
import android.util.Log;
import com.zippy.WayToGo.Comparator.MuniRouteComparator;
import com.zippy.WayToGo.Agency.NextBus.Activity.SelectRouteActivity;
import com.zippy.WayToGo.Agency.NextBus.NextBusDataHelper;
import com.zippy.WayToGo.BaseActivityGroup;
import com.zippy.WayToGo.ListAdapter.RouteBadgeAdapter;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.Util.LRUCache;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author alex
 */
public class SFMuni extends NextBusAgency {

    private static final String LOG_NAME = SFMuni.class.getCanonicalName();

    public SFMuni() {
        super();
        theNBName = "sf-muni";
        theURL = "http://www.sfmta.com";
        theShortName = "Muni";
        theLongName = "San Francisco Muni";
        theLogoId = R.drawable.sfmuni;
    }

    @Override
    public void init(final Context aContext) {
        super.init(aContext);
        if (theDBHelper == null) {
            theDBHelper = (NextBusDataHelper) setTheDBHelper(new SFMuniDataHelper(theContext, this));
        }
    }

    public static class ActivityGroup extends BaseActivityGroup {

        @Override
        public void onResume() {
            super.onResume();
            if (mIdList.isEmpty()) {
                final Intent ourIntent = new Intent(getParent(), RootActivity.class);
                ourIntent.putExtra("AgencyClassName", SFMuni.class.getCanonicalName());
                startChildActivity(RootActivity.class.getCanonicalName(), ourIntent);
            }
        }
    }

    protected static class RootActivity extends SelectRouteActivity {

        public RootActivity() {
            super();
        }

        @Override
        public void populateRoutesListView() {
            super.populateRoutesListView();
            RouteBadgeAdapter theAdapter = (RouteBadgeAdapter) theListView.getAdapter();
            theAdapter.sort(MuniRouteComparator.MUNI_ORDER);
        }
    }

    static final class SFMuniDataHelper extends NextBusDataHelper {
        private final LRUCache<String, String> theStopCache = new LRUCache<String, String>(75);

        public SFMuniDataHelper(final Context aContext, final NextBusAgency anAgency) {
            super(aContext, anAgency);
        }
        
        static final Pattern[] theStopPatterns = {
            // Yes, some stops say OB, some say Outbound
            Pattern.compile("OB"),
            Pattern.compile("Arr$"),
            Pattern.compile("Av$"),
            Pattern.compile("Drive"),
            Pattern.compile("Wy"),
            Pattern.compile("(Ave|St)\\."),
            Pattern.compile("Boulevard"),
            Pattern.compile("St\\."),
            Pattern.compile("-Sfsu$"),
            Pattern.compile("Us($|\\s)"),
            Pattern.compile("C. Chavez"),
            Pattern.compile(" /Ds"),
            Pattern.compile("Market Bet\\."),
            Pattern.compile("Bet\\."),
            Pattern.compile("(?:b/t)(\\d)"),
            Pattern.compile("([01456789])th\\s&\\s(\\d*)th\\sSt"),
            Pattern.compile("Macy'S"),
            Pattern.compile("-Stonestown$"),
            Pattern.compile("(?i)Bowllin0"),
            Pattern.compile("^Townsend & 4th"),
            Pattern.compile("Hts"),
            Pattern.compile("Portola$"),
            Pattern.compile("Duncan & Amber"),
            Pattern.compile("Diamond Heights Blvd Gold Mine"),
            Pattern.compile("Gnvaterm"),
            Pattern.compile("(?:Metro Terminal|Green Division Yard)"),
            Pattern.compile("Bay Shore"),
            Pattern.compile("Bayshore &"),
            Pattern.compile("Embarcadero Folsom St"),
            Pattern.compile("Glen Park Station"),
            Pattern.compile("^Embarcadero\\s&"),
            Pattern.compile("6th & 7th"),
            Pattern.compile("5th St & 4th St"),
            Pattern.compile("4th & 3rd St"),
            Pattern.compile("Bealemst"),
            Pattern.compile("Us101 Offramp"),
            Pattern.compile("Ggnra"),
            Pattern.compile("Sf\\s"),
            Pattern.compile("Van Ness\\s&"),
            Pattern.compile("North Point$"),
            Pattern.compile("Brotherhd"),
            Pattern.compile("Bart Station"),
            Pattern.compile("Bart"),
            Pattern.compile("Br\\."),
            Pattern.compile("^(Market|Clement)\\s&"),
            Pattern.compile("([01456789])\\s(St|Ave)")
        };
        private static final String[] theStopReplacements = {
            "Outbound",
            "Inbound",
            "Ave",
            "Drive",
            "Way",
            "$1",
            "Blvd",
            "St",
            " (SFSU)",
            "US",
            "Cesar Chavez",
            "",
            "Market St Bet.",
            "b/t",
            "b/t $2",
            "$1th St & $2th St",
            "Macy's",
            " (Stonestown)",
            "Bowley St & Lincoln Blvd",
            "Townsend St & 4th St",
            "Heights",
            "Portola Dr",
            "Duncan St & Amber Dr",
            "Diamond Heights Blvd & Gold Mine",
            "San Jose Ave & Geneva Ave",
            "Balboa Park BART (Mezzanine Level)",
            "Bayshore",
            "Bayshore Blvd &",
            "The Embarcadero & Folsom St",
            "Glen Park BART",
            "The Embarcadero &",
            "6th St & 7th St",
            "4th St & 5th St",
            "3rd St & 4th St",
            "Beale St",
            "US Hwy 101 &",
            "GGNRA",
            "SF ",
            "Van Ness Ave &",
            "North Point St",
            "Brotherhood Way",
            "BART",
            "BART",
            "Bridge",
            "$1 St &",
            "$1th $2"
        };
        private static final boolean[] theStopReplaceAll = {
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            false,
            false,
            true,
            true,
            false,
            false,
            false,
            false,
            true,
            true,
            false,
            false,
            false,
            false,
            false,
            false,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            false,
            true
        };

        @Override
        protected String getSanitizedRouteName(final String aRouteTitle) {
            return aRouteTitle
                    .replaceFirst("(\\d|\\w*)-(\\d|\\w*)\\s?-\\s?(.*)", "$1-$2/$3")
                    .replaceFirst("(\\d|\\w*)-(\\d|\\w*)", "$1 - $2")
                    .replaceAll("(.)/(.)", "$1 / $2")
                    .replaceAll("B.A.R.T.", "BART")
                    .replaceAll("Exp$", "Express");
        }

        @Override
        protected String getSanitizedDirection(final String aDirectionTitle) {
            return aDirectionTitle
                    .replaceAll("V A", "VA")
                    .replaceAll("(?i)St\\.", "St")
                    .replaceAll("(Ave|St|Blvd)\\.", "$1")
                    .replaceAll("Street", "St")
                    .replaceAll("Avenue", "Ave")
                    .replaceAll("Boulevard", "Blvd")
                    .replaceAll("\\sAv\\.", " Ave")
                    .replaceAll("\\sAv\\s", " Ave ")
                    .replaceAll("([01456789])\\s(St|Ave)", "$1th $2")
                    .replaceAll("(1)\\s(St|Ave)", "$1st $2")
                    .replaceAll("(2)\\s(St|Ave)", "$1nd $2")
                    .replaceAll("(3)\\s(St|Ave)", "$1rd $2")
                    .replaceAll("(?i)([0-9])(\\s)(nd|Th|Rd|St)", "$1$3")
                    .replaceAll("(?i)Geray", "Geary")
                    .replaceAll("PLaya", "Playa")
                    .replaceAll("Phean", "Phelan")
                    .replaceAll("S F", "SF")
                    .replaceAll("Pa Lobos", "Pt Lobos")
                    .replaceFirst("Visitacion Valley via Downtown", "Sunnydale")
                    .replaceFirst("SF STATE", "SF State University")
                    .replaceAll("St\\.", "St")
                    .replaceAll("Bart", "BART")
                    .replaceAll("Balboa (BART|Park) Station", "Balboa Park BART")
                    .replaceFirst("to Chavez", "to Cesar Chavez St")
                    .replaceAll("Steuart Terminal", "Steuart St & Mission St")
                    .replaceFirst("Mission &", "Mission St &")
                    .replaceFirst("Van Ness Av\\s&", "Van Ness Ave")
                    .replaceAll("Vanness", "Van Ness Ave")
                    .replaceAll("Lagrande", "La Grande")
                    .replaceAll("Acevdo", "Acevedo")
                    .replaceAll("\\s(Arballo)(?!\\s?+Dr)(\\s&|$)", " $1 Dr$2")
                    .replaceAll("\\s(Presidio|San Jose|Potrero|Van\\sNess|Pt\\sLobos|Masonic|La\\sGrande|Palou|Geneva|Acevedo)(?!\\s?+Ave)(\\s&|$)", " $1 Ave$2")
                    .replaceAll("\\s(Geary|Sloat|Park\\sPresidio|Bayshore)(?!\\s?+Blvd)(\\s&|$)", " $1 Blvd$2")
                    .replaceAll("\\s(Mission|Main|Market|Judah|Steuart|Powell|Turk|Lyon|Quintara|Kearny|Fillmore|Jackson|Church|Union|Duboce|Fulton|Vicente|Wawona|Bay|Ortega|Munich|La\\sPlaya|Divisidero|California|Cabrillo|Bryant|Polk)(?!\\s?+St)(\\s\\&|$)", " $1 St$2")
                    .replaceFirst("\\s&(Van Ness)$", " & $1 Ave")
                    .replaceAll("Park Presidio Ave", "Park Presidio Blvd");
        }

        @Override
        protected String getSanitizedStopName(final String aStopTitle) {
            final String ourCachedValue = theStopCache.get(aStopTitle);
            if (ourCachedValue != null) {
                return ourCachedValue;
            }
            String ret = aStopTitle;
            /*if ((theStopPatterns.length != theStopReplaceAll.length) && (theStopReplaceAll.length != theStopReplacements.length)) {
            Log.d(LOG_NAME, "Something's wrong with the arrays");
            }*/
            for (int i = 0; i < theStopPatterns.length; i++) {
                final Matcher ourMatcher = theStopPatterns[i].matcher(ret);
                if (theStopReplaceAll[i]) {
                    ret = ourMatcher.replaceAll(theStopReplacements[i]);
                } else {
                    ret = ourMatcher.replaceFirst(theStopReplacements[i]);
                }
            }
            theStopCache.put(aStopTitle, ret);
            return ret;
        }

        @Override
        protected String getTerseStopName(final String aStopTitle) {
            return getSanitizedStopName(aStopTitle).replaceAll("Mezzanine Level", "Mezzanine").replaceAll("Junipero Serra Blvd", "Junipero Serra").replaceAll("Van Ness Ave", "Van Ness").replaceAll("Brotherhood Way", "Brotherhood");
        }

        @Override
        protected String getTerseDirectionName(final String aDirectionTitle) {
            return aDirectionTitle.replaceAll("(?:In|Out)bound\\sto\\s", "").replaceAll("(Presidio|San Jose|Potrero|Van\\sNess|Pt\\sLobos|Masonic|La\\sGrande|Palou|Geneva|Acevedo) Ave", "$1").replaceAll("(Judah|Kirkham|Munich|Mission|Market|California) St", "$1").replaceAll("Park Presidio Blvd", "Park Presidio");
        }

        @Override
        protected String getSanitizedRouteTag(final String aRouteTag) {
            if (aRouteTag.equals("59")) {
                return "PM";
            } else if (aRouteTag.equals("60")) {
                return "PH";
            } else if (aRouteTag.equals("61")) {
                return "CA";
            } else {
                return aRouteTag.replaceFirst("(\\s|_)OWL$", "");
            }
        }

        @Override
        protected int getBadgeColorForRouteTag(final String aRouteTag) {
            if (aRouteTag.contains("X")) {
                // Our express color
                return 0xff990000;
            } else if (aRouteTag.contains("L") && !aRouteTag.contains("OWL") && !aRouteTag.equals("L")) {
                // Limited color
                return 0xff00aa00;
            } else if (aRouteTag.contains("OWL")) {
                // OWL splash color
                return 0xff0000cc;
            } else {
                //0xff444444
                //0xffcccccc
                return Color.LTGRAY;
            }
        }
    };
}
