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
package com.inferiorhumanorgans.WayToGo.Comparator;

import com.inferiorhumanorgans.WayToGo.Util.Stop;

/**
 * A sorting comparator to sort strings numerically,
 * ie [1, 2, 10], as opposed to [1, 10, 2].
 */
public class StopComparator extends NaturalComparator<Stop> {

    public static final StopComparator STOP_ORDER = new StopComparator();

    @Override
    protected String getSortString(final Stop aStop) {
        return aStop.name();
    }
}
