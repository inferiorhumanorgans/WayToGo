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
package com.zippy.WayToGo.Bookmarks;

import android.content.Intent;
import com.zippy.WayToGo.Util.Stop;

/**
 *
 * @author alex
 */
public class Bookmark {

    private final int theId;
    private final String theAgencyClassName;
    private final Stop theStop;
    private Intent theIntent;

    public Bookmark(final int anId, final String anAgencyClass, final Stop aStop) {
        theId = anId;
        theAgencyClassName = anAgencyClass;
        theStop = aStop;
    }

    public final Intent getTheIntent() {
        return theIntent;
    }

    public final void setTheIntent(Intent aIntent) {
        theIntent = aIntent;
    }

    public final int getTheId() {
        return theId;
    }

    public final Stop getTheStop() {
        return theStop;
    }

    public final String getTheAgencyClass() {
        return theAgencyClassName;
    }
}
