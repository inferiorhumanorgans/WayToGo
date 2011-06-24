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
package com.zippy.WayToGo.Util;

import com.zippy.WayToGo.Agency.BaseAgency;
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

    public String getTheSubStopTag() {
        return theSubStopTag;
    }

    public String getTheFlags() {
        return theFlags;
    }

    public void setTheFlags(String someFlags) {
        if (someFlags == null) {
            theFlags = "";
        } else {
            this.theFlags = someFlags;
        }
    }

    public final BaseAgency getTheAgency() {
        return theAgency;
    }

    public final int getMinutes() {
        return theMinutes;
    }

    public final String getRouteTag() {
        return theRouteTag;
    }

    public final String getStopTag() {
        return theStopTag;
    }

    public final String getDirectionTag() {
        return theDirectionTag;
    }

    /**
     * Completely unique combo of stop, agency, and route/dir
     * @return
     */
    public final String getGUID() {
        Assert.assertEquals(false, theAgency == null);
        Assert.assertEquals(false, theStopTag == null);
        Assert.assertEquals(false, theRouteTag == null);
        Assert.assertEquals(false, theDirectionTag == null);

        return
                theAgency.getClass().getCanonicalName()
                + "/"
                + theStopTag
                + "/"
                + theRouteTag
                + "/"
                + theAgency.getDirectionFromTag(theDirectionTag).getTheTitle();
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
