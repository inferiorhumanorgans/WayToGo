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
package com.zippy.WayToGo.Comparator;

import com.zippy.WayToGo.Util.Route;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A sorting comparator to sort strings numerically,
 * ie [1, 2, 10], as opposed to [1, 10, 2].
 */
public final class MuniRouteComparator extends RouteComparator {

    public static final MuniRouteComparator MUNI_ORDER = new MuniRouteComparator();

    private static Set<String> theCableCarRoutes = null;
    private static final String[] theCableCarRoutesArray = {"59", "60", "61"};
    private static final String theOwlString = "OWL";
    private static final String OWL_PREFIX="zza";
    private static final String CABLE_CAR_PREFIX="zzz";

    @Override
    protected String getSortString(final Route aRoute) {
        if (theCableCarRoutes == null) {
            theCableCarRoutes = new HashSet<String>(Arrays.asList(theCableCarRoutesArray));
        }

        if (theCableCarRoutes.contains(aRoute.rawTag())) {
            return CABLE_CAR_PREFIX + aRoute.name();
        } else if (aRoute.rawTag().contains(theOwlString)) {
            return OWL_PREFIX + aRoute.name();
        }
        return aRoute.name();
    }

}
