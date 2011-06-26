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
package com.zippy.WayToGo.Agency.NextBus.RouteConfig;

import android.content.ContentValues;

/**
 *
 * @author alex
 */
public interface RouteConfigNotification {
    public void addRoute(final ContentValues aRoute);
    public void addStopToRoute(final String aRouteTag, final String aStopTag);
    public void addDirection(final ContentValues aDirection);
    public void addStopToDirection(final String aDirectionTag, final String aStopTag, final int aPosition);
    public void addStop(final ContentValues aStop);
    public void addPath(final ContentValues aPath);
    public void finishedWithRoute();
    public boolean isCancelled();
}
