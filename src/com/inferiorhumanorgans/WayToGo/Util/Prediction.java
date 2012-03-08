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
package com.inferiorhumanorgans.WayToGo.Util;

import com.inferiorhumanorgans.WayToGo.Agency.BaseAgency;
import junit.framework.Assert;
import junit.framework.Assert.*;

/**
 *
 * @author alex
 */
public final class Prediction {

    private final BaseAgency theAgency;
    private final String theRouteTag, theStopTag, theSubStopTag, theDirectionTag;
    private final int theMinutes;
    private String theFlags;

    public Prediction(final BaseAgency anAgency, final String aRouteTag, final String aStopTag, final String aDirectionTag, final int aMinutes) {
        this(anAgency, aRouteTag, aStopTag, null, aDirectionTag, aMinutes);
    }

    public Prediction(final BaseAgency anAgency, final String aRouteTag, final String aStopTag, final String aSubStopTag, final String aDirectionTag, final int aMinutes) {
        theAgency = anAgency;
        theRouteTag = aRouteTag;
        theStopTag = aStopTag;
        theSubStopTag = aSubStopTag;
        theDirectionTag = aDirectionTag;
        theMinutes = aMinutes;
        theFlags = "";
    }

    /**
     * A.K.A. Platform.
     * @return
     */
    public String subStopTag() {
        return theSubStopTag;
    }

    public String flags() {
        return theFlags;
    }

    public void setFlags(final String someFlags) {
        if (someFlags == null) {
            theFlags = "";
        } else {
            this.theFlags = someFlags;
        }
    }

    public final BaseAgency agency() {
        return theAgency;
    }

    public final int minutes() {
        return theMinutes;
    }

    public final String routeTag() {
        return theRouteTag;
    }

    public final String stopTag() {
        return theStopTag;
    }

    public final String directionTag() {
        return theDirectionTag;
    }

    /**
     * Completely unique combo of stop, agency, and route/dir
     * @return
     */
    public final String getGUID() {
        Assert.assertNotNull(theAgency);
        Assert.assertNotNull(theStopTag);
        Assert.assertNotNull(theRouteTag);
        Assert.assertNotNull(theDirectionTag);

        return
                theAgency.getClass().getCanonicalName()
                + "/"
                + theStopTag
                + "/"
                + theRouteTag
                + "/"
                + theAgency.getDirectionFromTag(theDirectionTag).title();
    }

    /**
     * Just the stop/agency so we can get different directions if needed
     * @return
     */
    public final String getUID() {
        return theAgency.getClass().getCanonicalName() + "/" + theStopTag;
    }

    @Override
    public final String toString() {
        return theAgency.getShortName() + "/" + theRouteTag + "/" + theDirectionTag + "/" + theMinutes + ((theFlags == null || theFlags.equals("")) ? "" : "/" + theFlags);
    }
}
