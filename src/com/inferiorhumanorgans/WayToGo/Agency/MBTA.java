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
// http://groups.google.com/group/ne.transportation/browse_thread/thread/4c8fdc249c6c5713/5950cc665631c8b8?hl=en&ie=UTF-8&oe=UTF-8&q=mbta+34E&pli=1
// http://stackoverflow.com/questions/507602/how-to-initialise-a-static-map-in-java
package com.inferiorhumanorgans.WayToGo.Agency;

import com.inferiorhumanorgans.WayToGo.Agency.NextBus.NextBusAgency;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import com.inferiorhumanorgans.WayToGo.Agency.NextBus.Activity.SelectRouteActivity;
import com.inferiorhumanorgans.WayToGo.Agency.NextBus.NextBusDataHelper;
import com.inferiorhumanorgans.WayToGo.BaseActivityGroup;
import com.inferiorhumanorgans.WayToGo.ListAdapter.RouteBadgeAdapter;
import com.inferiorhumanorgans.WayToGo.Comparator.MBTARouteComparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author alex
 */
public class MBTA extends NextBusAgency {

    private static final String LOG_NAME = MBTA.class.getCanonicalName();

    protected static final String theLongName = "Massachusetts Bay Transportation Authority";
    protected static final String theShortName = "MBTA";
    protected static final String theURL = "http://www.mbta.com/";

    public MBTA() {
        super();
        theNBName = "mbta";
    }

    @Override
    public void init(final Context aContext) {
        super.init(aContext);
        if (theDBHelper == null) {
            theDBHelper = (NextBusDataHelper) setTheDBHelper(new MBTADataHelper(theContext, this));
        }
    }

    public static class ActivityGroup extends BaseActivityGroup {

        @Override
        public void onResume() {
            super.onResume();
            if (mIdList.isEmpty()) {
                final Intent ourIntent = new Intent(getParent(), RootActivity.class);
                ourIntent.putExtra("AgencyClassName", MBTA.class.getCanonicalName());
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
            theAdapter.sort(MBTARouteComparator.NUMERICAL_ORDER);
        }
    }

    private static final class MBTADataHelper extends NextBusDataHelper {

        public MBTADataHelper(final Context aContext, final NextBusAgency anAgency) {
            super(aContext, anAgency);
        }
        public static final Map<String, String> routeTagMap;

        static {
            Map<String, String> aMap = new HashMap<String, String>(21);
            aMap.put("116117", "116/117");
            aMap.put("214216", "214/216");
            aMap.put("2427", "24/27");
            aMap.put("3233", "32/33");
            aMap.put("3738", "37/38");
            aMap.put("4050", "40/50");
            aMap.put("8993", "89/93");
            aMap.put("426439", "426/439");
            aMap.put("426455", "426/455");
            aMap.put("441442", "441/442");
            aMap.put("7275", "72/75");
            aMap.put("741", "SL1");
            aMap.put("742", "SL2");
            aMap.put("746", "SL");
            aMap.put("749", "SL5");
            aMap.put("751", "SL4");
            aMap.put("747", "CT2");
            aMap.put("708", "CT3");
            aMap.put("701", "CT1");
            aMap.put("627", "62/76");
            aMap.put("725", "72/75");

            routeTagMap = Collections.unmodifiableMap(aMap);
        }
        public static final Map<String, String> routeTitleMap;

        static {
            Map<String, String> aMap = new HashMap<String, String>(173);
            aMap.put("CT1", "CT1 - Central Square, Cambridge B.U. Medical Center/Boston Medical Center via M.I.T.");
            aMap.put("CT2", "CT2 - Sullivan Sta. Ruggles Sta. via Kendall/MIT");
            aMap.put("CT3", "CT3 - Beth Israel Deaconess Medical Center Andrew Sta. via B.U. Medical Center");
            aMap.put("SL1", "SL1 - Logan Airport South Sta. via Waterfront");
            aMap.put("SL2", "SL2 - Design Center South Sta. via Waterfront");
            aMap.put("SL4", "SL4 - Dudley Sta. South Sta. at Essex Street via Washington St");
            aMap.put("SL5", "SL5 - Dudley Sta. Downtown Crossing at Temple Place via Washington St");
            aMap.put("1", "1 - Harvard/Holyoke Gate Dudley Staton via Mass. Ave.");
            aMap.put("4", "4 - North Sta. World Trade Center via Federal Courthouse & South Station");
            aMap.put("5", "5 - City Point McCormack Housing via Andrew Station");
            aMap.put("7", "7 - City Point Otis & Summer Streets via Summer Street & South Station");
            aMap.put("8", "8 - Harbor Point/UMass Kenmore Sta. via B.U. Medical Center & Dudley Station");
            aMap.put("9", "9 - City Point Copley Square via Broadway Station");
            aMap.put("10", "10 - City Point Copley Square via Andrew Sta. & B.U. Medical Center");
            aMap.put("11", "11 - City Point Downtown BayView Route");
            aMap.put("14", "14 - Roslindale Sq. Heath Street via Dudley Sta., Grove Hall & Jackson Square Station");
            aMap.put("15", "15 - Kane Sq. or Fields Corner Sta. Ruggles Sta. via Uphams Corner");
            aMap.put("16", "16 - Forest Hills Sta. - Andrew Sta. or UMass via Columbia Road");
            aMap.put("17", "17 - Fields Corner Sta. - Andrew Sta. via Uphams Corner & Edward Everett Square");
            aMap.put("18", "18 - Ashmont Sta. - Andrew Sta. via Fields Corner Station");
            aMap.put("19", "19 - Fields Corner Sta. - Kenmore or Ruggles Sta. via Grove Hall & Dudley Station");
            aMap.put("21", "21 - Ashmont Sta. - Forest Hills Sta. via Morton St.");
            aMap.put("22", "22 - Ashmont Sta. - Ruggles Sta. via Talbot Ave. & Jackson Sq.");
            aMap.put("23", "23 - Ashmont Sta. - Ruggles Sta. via Washington St.");
            aMap.put("24", "24 - Wakefield Ave. & Truman Highway Mattapan or Ashmont Sta. via River Street");
            aMap.put("26", "26 - Ashmont Sta. - Norfolk & Morton Belt Line");
            aMap.put("27", "27 - Mattapan Sta. - Ashmont Sta. via River St.");
            aMap.put("28", "28 - Mattapan Sta. - Ruggles Sta. via Dudley Sta.");
            aMap.put("29", "29 - Mattapan Sta. - Jackson Sq. Sta. via Seaver St. & Columbus Ave.");
            aMap.put("30", "30 - Mattapan Sta. - Forest Hills Sta. via Cummins Highway & Roslindale Sq.");
            aMap.put("31", "31 - Mattapan Sta. - Forest Hills Sta. via Morton St.");
            aMap.put("32", "32 - Wolcott Sq. or Cleary Sq. - Forest Hills Sta. via Hyde Park Ave.");
            aMap.put("33", "33 - Dedham Line - Mattapan Sta. via River St.");
            aMap.put("34", "34 - Dedham Line - Forest Hills Sta. via Washington St.");
            aMap.put("34E", "34E - Walpole Center - Forest Hills Sta. via Washington St.");
            aMap.put("35", "35 - Dedham Mall/Stimson St. - Forest Hills Sta. via Belgrade Ave. & Centre St.");
            aMap.put("36", "36 - Charles River Loop or V.A. Hospital - Forest Hills Sta. via Belgrade Ave. & Centre St.");
            aMap.put("37", "37 - Baker & Vermont Sts. - Forest Hills Sta. via Belgrade Ave. & Centre St.");
            aMap.put("38", "38 - Wren St. - Forest Hills Sta. via Centre & South Streets");
            aMap.put("39", "39 - Forest Hills Sta. - Back Bay Sta. via Huntington Ave.");
            aMap.put("40", "40 - Georgetowne - Forest Hills Sta. via Washington St. & West Boundary Rd.");
            aMap.put("41", "41 - Centre & Eliot Sts. - JFK/UMass Sta. via Dudley Sta., Centre St. & Jackson Sq. Sta.");
            aMap.put("42", "42 - Forest Hills Sta. - Dudley or Ruggles Sta. via Washington St.");
            aMap.put("43", "43 - Ruggles Sta. - Park & Tremont Sts. via Tremont St.");
            aMap.put("44", "44 - Jackson Sq. Sta. - Ruggles Sta. via Seaver St. & Humboldt Ave.");
            aMap.put("45", "45 - Franklin Park Zoo - Ruggles Sta. via Blue Hill Ave.");
            aMap.put("47", "47 - Central Sq., Cambridge Broadway Sta. via B.U. Medical Center, Dudley Sta. & Longwood Medical Area");
            aMap.put("48", "48 - Jamaica Plain Loop Monument - Jackson Square Sta. via Green Street & Stony Brook Stations");
            aMap.put("50", "50 - Cleary Square - Forest Hills Sta. via Roslindale Square");
            aMap.put("51", "51 - Cleveland Circle - Forest Hills Sta. via Hancock Village");
            aMap.put("52", "52 - Dedham Mall or Charles River Loop - Watertown Yard via Oak Hill & Newton Center");
            aMap.put("55", "55 - Jersey & Queensberry - Copley Sq. or Park & Tremont Sts. via Ipswich St.");
            aMap.put("57", "57 - Watertown Yard - Kenmore Sta. via Newton Corner & Brighton Center");
            aMap.put("59", "59 - Needham Junction - Watertown Sq. via Newtonville");
            aMap.put("60", "60 - Chestnut Hill - Kenmore Sta. via Brookline Village & Cypress St.");
            aMap.put("62", "62 - Bedford V.A. Hospital - Alewife Sta. via Lexington Center & Arlington Heights");
            aMap.put("64", "64 - Oak Square University Park, Cambridge or Kendall/MIT via North Beacon St.");
            aMap.put("65", "65 - Brighton Center Kenmore Sta. via Washington St., Brookline Village & Brookline Ave.");
            aMap.put("66", "66 - Harvard Square Dudley Sta. via Allston & Brookline Village");
            aMap.put("67", "67 - Turkey Hill Alewife Sta. via Arlington Center");
            aMap.put("68", "68 - Harvard/Holyoke Gate - Kendall/M.I.T. via Broadway");
            aMap.put("69", "69 - Harvard/Holyoke Gate - Lechmere Sta. via Cambridge St.");
            aMap.put("70", "70 - Cedarwood or Watertown Sq. - University Park via Central Sq., Cambridge, Arsenal St. & Western Ave.");
            aMap.put("70A", "70A - No. Waltham or Watertown Sq. - University Park via Central Sq., Cambridge, Arsenal St. & Western Ave.");
            aMap.put("71", "71 - Watertown Square - Harvard Sta. via Mt. Auburn St.");
            aMap.put("72", "72 - Huron Ave. - Harvard Sta. via Concord Ave.");
            aMap.put("73", "73 - Waverley Sq. - Harvard Sta. via Trapelo Road");
            aMap.put("74", "74 - Belmont Center - Harvard Sta. via Concord Ave.");
            aMap.put("75", "75 - Belmont Center - Harvard Sta. via Concord Ave.");
            aMap.put("76", "76 - Hanscom/Lincoln Labs - Alewife Sta. via Lexington Center & Civil Air Terminal");
            aMap.put("77", "77 - Arlington Heights - Harvard Sta. via Massachusetts Ave.");
            aMap.put("78", "78 - Arlmont Village - Harvard Sta. via Park Circle");
            aMap.put("79", "79 - Arlington Heights - Alewife Sta. via Massachusetts Ave.");
            aMap.put("80", "80 - Arlington Center - Lechmere Sta. via Medford Hillside");
            aMap.put("83", "83 - Rindge Ave. Central Sq., Cambridge via Porter Square Station");
            aMap.put("84", "84 - Arlmont Village - Alewife Sta.");
            aMap.put("85", "85 - Spring Hill - Kendall/M.I.T. Sta. via Summer St. & Union Sq.");
            aMap.put("86", "86 - Sullivan Sq. Sta. - Reservoir (Cleveland Circle) via Harvard/Johnston Gate");
            aMap.put("87", "87 - Arlington Center or Clarendon Hill - Lechmere Sta. via Somerville Ave.");
            aMap.put("88", "88 - Clarendon Hill - Lechmere Sta. via Highland Avenue");
            aMap.put("89", "89 - Clarendon Hill or Davis Square - Sullivan Square Sta. via Broadway");
            aMap.put("90", "90 - Davis Square - Wellington Sta. via Sullivan Square Sta. & Assembly Mall");
            aMap.put("91", "91 - Sullivan Sq. Sta. - Central Sq., Camb. via Washington St.");
            aMap.put("92", "92 - Assembly Sq. Mall - Downtown via Sullivan Sq. Sta., Main St. & Haymarket Sta.");
            aMap.put("93", "93 - Sullivan Sq. Sta. - Downtown via Bunker Hill St. & Haymarket Sta.");
            aMap.put("94", "94 - Medford Square - Davis Sq. Sta. via W. Medford & Medford Hillside");
            aMap.put("95", "95 - West Medford - Sullivan Sq. Sta. via Mystic Ave.");
            aMap.put("96", "96 - Medford Sq. - Harvard Sta. via George St. & Davis Sq. Sta.");
            aMap.put("97", "97 - Malden Center Sta. - Wellington Sta. via Commercial & Hancock Sts.");
            aMap.put("99", "99 - Boston Regional Medical Center - Wellington Sta. via Main St. & Malden Center Station");
            aMap.put("100", "100 - Elm St. - Wellington Sta. via Fellsway");
            aMap.put("101", "101 - Malden Center Sta. - Sullivan Square Sta. via Salem St., Main St. & Broadway");
            aMap.put("104", "104 - Malden Center Sta. - Sullivan Square Sta. via Ferry St. & Broadway");
            aMap.put("105", "105 - Malden Center Sta. - Sullivan Square Sta. via Newland St. Housing");
            aMap.put("106", "106 - Lebanon St., Malden or Franklin Sq. Wellington Sta. via Main St.");
            aMap.put("108", "108 - Linden Square - Wellington Sta. via Malden Center Sta. & Highland Ave.");
            aMap.put("109", "109 - Linden Square - Sullivan Square Sta. via Glendale Square");
            aMap.put("110", "110 - Wonderland or Broadway & Park Ave. - Wellington Sta. via Park Ave., & Woodlawn");
            aMap.put("111", "111 - Woodlawn or Broadway & Park Ave. - Haymarket Sta. via Mystic River/Tobin Bridge");
            aMap.put("112", "112 - Wellington Sta. - Wood Island Sta. via Central Ave., Mystic Mall & Admiral's Hill");
            aMap.put("114", "114 - Bellingham Square - Maverick Station");
            aMap.put("116", "116 - Wonderland Sta. - Maverick Sta. via Revere Street");
            aMap.put("117", "117 - Wonderland Sta. - Maverick Sta. via Beach St.");
            aMap.put("119", "119 - Northgate - Beachmont Sta. via Revere Center & Cooledge Housing");
            aMap.put("120", "120 - Orient Heights Sta. - Maverick Sta. via Bennington St., Jeffries Point & Waldemar Loop");
            aMap.put("121", "121 - Wood Island Sta. Maverick Sta. via Lexington Street");
            aMap.put("131", "131 - Melrose Highlands - Malden Center Sta. via Oak Grove Station");
            aMap.put("132", "132 - Redstone Shopping Center - Malden Station");
            aMap.put("134", "134 - North Woburn - Wellington Sta. via Woburn Sq., Winchester Ctr., Winthrop St., Medford Sq., Riverside Ave. & Meadow...");
            aMap.put("136", "136 - Reading Depot - Malden Sta. via Wakefield, Melrose & Oak Grove Station");
            aMap.put("137", "137 - Reading Depot - Malden Sta. via North Ave, Wakefield, Melrose & Oak Grove Station");
            aMap.put("170", "170 - Central Sq., Waltham - Dudley Square");
            aMap.put("171", "171 - Dudley Sta. - Logan Airport via Andrew Station");
            aMap.put("201", "201 - Fields Corner or No. Quincy Sta. - Fields Corner via Neponset Ave. to Adams St.");
            aMap.put("202", "202 - Fields Corner or No. Quincy Sta. - Fields Corner via Adams St. to Neponset Ave.");
            aMap.put("210", "210 - Quincy Center Sta. - No. Quincy Sta. or Fields Corner Sta. via Hancock St. & Neponset Ave.");
            aMap.put("211", "211 - Quincy Center Sta. - Squantum via Montclair & No. Quincy Sta.");
            aMap.put("212", "212 - Quincy Center Sta. - North Quincy Sta. via Billings Road");
            aMap.put("214", "214 - Quincy Center Sta. - Germantown via Sea St. & O'Brien Towers");
            aMap.put("215", "215 - Quincy Center Sta. - Ashmont Sta. via W. Quincy & E. Milton Sq.");
            aMap.put("216", "216 - Quincy Center Sta. - Houghs Neck via Sea Street");
            aMap.put("217", "217 - Quincy Center Sta. - Ashmont Sta. via Beale St., Wollaston, & E. Milton Sq.");
            aMap.put("220", "220 - Quincy Center Sta. - Hingham Center via Fore River Bridge");
            aMap.put("221", "221 - Quincy Center Sta. - Fort Point via Bicknell Square");
            aMap.put("222", "222 - Quincy Center Sta. - East Weymouth via Bicknell Square");
            aMap.put("225", "225 - Quincy Center Weymouth Landing via Quincy Ave. & Shaw St. or Desmoines Road");
            aMap.put("230", "230 - Quincy Center Sta. - Montello Commuter Rail Sta. via Holbrook & Braintree Station");
            aMap.put("236", "236 - Quincy Center Sta. - South Shore Plaza via E. Braintree & Braintree Station");
            aMap.put("238", "238 - Quincy Center Sta. - Holbrook/Randolph Commuter Rail Sta.");
            aMap.put("240", "240 - Avon Square or Holbrook/Randolph Commuter Rail Sta. Ashmont Sta. via Crawford Sq., Randolph");
            aMap.put("245", "245 - Quincy Center Sta. - Mattapan Sta. via Quincy Hospital & Pleasant St.");
            aMap.put("325", "325 - Elm St., Medford - Haymarket Sta. via Fellsway West, Salem St. & I93");
            aMap.put("326", "326 - West Medford - Haymarket Sta. via Playstead Rd., High St., Medford & I93");
            aMap.put("350", "350 - North Burlington - Alewife Sta. via Burlington Mall");
            aMap.put("351", "351 - Oak Park/Bedford Woods - Alewife Sta. via Middlesex Turnpike");
            aMap.put("352", "352 - Burlington Express - Boston via Rte. 128 & I93");
            aMap.put("354", "354 - Woburn Express - Boston via Woburn Sq. & I93");
            aMap.put("355", "355 - Mishawum Sta. - Boston via W. Cummings Park & I93");
            aMap.put("411", "411 - Malden Center Sta. - Revere/Jack Satter House via Granada Highlands and Northgate");
            aMap.put("424", "424 - Eastern Ave. & Essex St. - Haymarket Sta. or Wonderland Salem Depot Haymarket Sta. or Wonderland Salem Depot Central ...");
            aMap.put("424W", "424W - Eastern Ave. & Essex St. - Haymarket Sta. or Wonderland Salem Depot Haymarket Sta. or Wonderland Salem Depot Central...");
            aMap.put("426", "426 - Central Sq., Lynn - Haymarket Sta. via Cliftondale");
            aMap.put("426W", "426W - Central Sq., Lynn - Wonderland Sta. via Cliftondale Square");
            aMap.put("428", "428 - Oaklandvale - Haymarket Sta. via Granada Highlands");
            aMap.put("429", "429 - Northgate Shopping Center - Central Sq., Lynn via Linden Square & Square One Mall");
            aMap.put("430", "430 - Saugus Center - Malden Center Sta. via Square One Mall");
            aMap.put("431", "431 - Neptune Towers - Central Sq., Lynn via Summer St.");
            aMap.put("434", "434 - Peabody - Haymarket EXPRESS via Goodwins Circle");
            aMap.put("435", "435 - Liberty Tree Mall - Central Sq., Lynn via Peabody Sq.");
            aMap.put("436", "436 - Liberty Tree Mall - Central Sq., Lynn via Goodwins Circle");
            aMap.put("439", "439 - Bass Point, Nahant - Central Sq., Lynn");
            aMap.put("441", "441 - Marblehead - Haymarket, Downtown Crossing or Wonderland via Central Square, Lynn & Lynnway");
            aMap.put("442", "442 - Marblehead - Haymarket, Downtown Crossing or Wonderland via Central Square, Lynn & Lynnway");
            aMap.put("448", "448 - Marblehead - Downtown Crossing via Paradise Rd.or Humphrey St., Lynnway, & Airport");
            aMap.put("449", "449 - Marblehead - Downtown Crossing via Paradise Rd. or Humphrey St., Lynnway, & Airport");
            aMap.put("450", "450 - Eastern Ave. & Essex St. Haymarket Sta. or Wonderland Salem Depot Haymarket Sta. or Wonderland Salem Depot Central ...");
            aMap.put("451", "451 - North Beverly - Salem Depot via Cabot St. or Tozer Rd.");
            aMap.put("455", "455 - Salem Depot - Wonderland via Central Sq., Lynn");
            aMap.put("456", "456 - Eastern Ave. & Essex St. Haymarket Sta. or Wonderland Salem Depot Haymarket Sta. or Wonderland Salem Depot Central ...");
            aMap.put("459", "459 - Salem Depot - Downtown Crossing via Logan Airport & Central Square, Lynn");
            aMap.put("465", "465 - Salem Depot - Liberty Tree Mall via Peabody & Danvers");
            aMap.put("468", "468 - Salem Depot - Danvers Sq. via Peabody");
            aMap.put("500", "500 - EXPRESS BUS Riverside Downtown via Mass. Turnpike");
            aMap.put("501", "501 - EXPRESS BUS Brighton Center Downtown via Oak Sq., & Mass. Turnpike");
            aMap.put("502", "502 - EXPRESS BUS Watertown Yard Copley Sq. (St. James Ave. at Dartmouth St.) via Newton Corner & Mass. Turnpike");
            aMap.put("503", "503 - EXPRESS BUS Brighton Center Copley Sq. via Oak Sq. & Mass. Turnpike");
            aMap.put("504", "504 - EXPRESS BUS Watertown/Newton Corner Downtown via Mass. Turnpike");
            aMap.put("505", "505 - EXPRESS BUS Central Sq., Waltham Downtown via Moody St. & Mass. Turnpike");
            aMap.put("553", "553 - Roberts Downtown Boston via Newton Corner & Central Sq., Waltham");
            aMap.put("554", "554 - Roberts Downtown Boston via Newton Corner & Central Sq., WalthamWaverley Sq. Downtown Boston via Newton Corner & C...");
            aMap.put("555", "555 - EXPRESS BUS Riverside - Downtown Boston via Newton, Mass. Pike & Copley Sq.");
            aMap.put("556", "556 - Waltham Highlands Downtown Boston via Newton Corner & Central Sq., Waltham & Newtonville");
            aMap.put("558", "558 - Riverside Downtown Boston via Newton Corner & Turnpike");
            // Best Guesses
            aMap.put("9109", "9109 - 89/92/93 Supplement");
            aMap.put("9111", "9111 - 89/83 Supplement");
            aMap.put("9501", "9501 - 211/215/217 Supplement");
            aMap.put("9507", "9507 - 211 Supplement");
            aMap.put("9701", "9701 - 39/66/501/503 Supplement");
            aMap.put("9702", "9702 - 39/66/501/503 Supplement");
            aMap.put("9703", "9703 - 39/66/501/503 Supplement");
            aMap.put("57A", "57 - Oak Square - Kenmore Sta.");

            routeTitleMap = Collections.unmodifiableMap(aMap);
        }

        @Override
        protected String getSanitizedStopName(final String aStopTitle) {
            return aStopTitle.replaceAll("St opp", "Stop");
        }

        @Override
        protected String getSanitizedRouteTag(final String aRouteTag) {
            if (routeTagMap.containsKey(aRouteTag)) {
                return routeTagMap.get(aRouteTag);
            }
            return aRouteTag;
        }

        @Override
        protected String getSanitizedRouteName(final String aRouteTitle) {
            if (routeTitleMap.containsKey(aRouteTitle)) {
                return routeTitleMap.get(aRouteTitle).replaceFirst("[0-9a-zA-Z]*\\s\\-\\s", "").replaceFirst("via.*", "").replaceFirst("\\s-\\s", "\n").replaceFirst("EXPRESS BUS\\s", "");
            }
            return aRouteTitle;
        }

        @Override
        protected int getBadgeColorForRouteTag(final String aRouteTag) {
            if (routeTitleMap.containsKey(aRouteTag)) {
                final String ourRouteTag = routeTitleMap.get(aRouteTag);
                if (ourRouteTag.contains("EXPRESS")) {
                    return 0xff990000;
                }
            }
            if (aRouteTag.endsWith("A")) {
                return 0xff00aa00;
            } else if (aRouteTag.endsWith("E")) {
                return 0xff0099cc;
            }
            return Color.LTGRAY;
        }
    }
}
