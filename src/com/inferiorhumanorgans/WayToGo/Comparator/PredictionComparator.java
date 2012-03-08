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

import com.inferiorhumanorgans.WayToGo.Util.Prediction;
import java.util.Comparator;

/**
 *
 * @author alex
 */
public final class PredictionComparator implements Comparator<Prediction> {

    public static final PredictionComparator PREDICTION_ORDER = new PredictionComparator();

    @Override
    public int compare(final Prediction o1, final Prediction o2) {
        // BEFORE = -1;
        // EQUAL = 0;
        // AFTER = 1;

        final int i1 = o1.minutes();
        final int i2 = o2.minutes();
        if (i1 < i2) {
            return -1;
        }
        if (i1 == i2) {
            return 0;
        }
        return 1;
    }
}
