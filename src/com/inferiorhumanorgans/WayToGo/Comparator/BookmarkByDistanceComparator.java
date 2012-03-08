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

import android.location.Location;
import com.inferiorhumanorgans.WayToGo.Bookmarks.Bookmark;
import java.util.Comparator;
import org.osmdroid.util.GeoPoint;

/**
 *
 * @author alex
 */
public final class BookmarkByDistanceComparator implements Comparator<Bookmark> {

    private final Location theLocation;

    public BookmarkByDistanceComparator(Location aLocation) {
        theLocation=aLocation;
    }

    @Override
    public int compare(Bookmark o1, Bookmark o2) {
        // BEFORE = -1;
        // EQUAL = 0;
        // AFTER = 1;

        // We shouldn't really be here because the only time this is called is when
        // we've gotten a fix and saved it.
        if (theLocation == null) {
            return 0;
        }

        final float[] theDistance = new float[1];
        final float i1, i2;
        final GeoPoint point1, point2;

        point1 = o1.getTheStop().point();
        point2 = o2.getTheStop().point();

        Location.distanceBetween(
                point1.getLatitudeE6()/1E6, point1.getLongitudeE6()/1E6,
                theLocation.getLatitude(), theLocation.getLongitude(), theDistance);
        i1 = theDistance[0];

        Location.distanceBetween(
                point2.getLatitudeE6()/1E6, point2.getLongitudeE6()/1E6,
                theLocation.getLatitude(), theLocation.getLongitude(), theDistance);
        i2 = theDistance[0];


        if (i1 < i2) {
            return -1;
        }
        if (i1 == i2) {
            return 0;
        }
        return 1;
    }
}
