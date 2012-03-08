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
package com.inferiorhumanorgans.WayToGo.MapView;

import com.inferiorhumanorgans.WayToGo.Util.Stop;
import org.osmdroid.views.overlay.OverlayItem;

/**
 *
 * @author alex
 */
public class StopOverlayItem extends OverlayItem {
    private final Stop theStop;
    public StopOverlayItem (final Stop aStop) {
        super(null,null,aStop.point());
        theStop=aStop;
    }

    public Stop getTheStop() {
        return theStop;
    }

    @Override
    public String getTitle() {
        return theStop.agency().getShortName();
    }

    @Override
    public String getSnippet() {
        return theStop.name();
    }

}