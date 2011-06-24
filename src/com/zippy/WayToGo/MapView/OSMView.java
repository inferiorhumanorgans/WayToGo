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
package com.zippy.WayToGo.MapView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 *
 * @author alex
 */
public class OSMView extends MapView {

    private static final String LOG_NAME = OSMView.class.getSimpleName();
    private OSMReadyListener theReadyListener;

    public OSMView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!changed) {
            return;
        }
        //dumpBoundingInfo();
        if (theReadyListener != null) {
            theReadyListener.finishedWithLayout(true);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void setOnReadyListener(OSMReadyListener aReadyListener) {
        theReadyListener = aReadyListener;
    }

    private void dumpBoundingInfo() {
        GeoPoint theCenter = getMapCenter();
        int theLatSpan = getLatitudeSpan();
        int theLngSpan = getLongitudeSpan();
        int minLat, maxLat;
        int minLng, maxLng;
        minLat = theCenter.getLatitudeE6() - theLatSpan;
        maxLat = theCenter.getLatitudeE6() + theLatSpan;
        minLng = theCenter.getLongitudeE6() - theLatSpan;
        maxLng = theCenter.getLongitudeE6() + theLatSpan;
        Log.i(LOG_NAME, "Bounding BOX IS: " + getBoundingBox().toString());

        Log.i(LOG_NAME, "Lat Span: " + theLatSpan);
        Log.i(LOG_NAME, "Lng Span: " + theLngSpan);
        Log.i(LOG_NAME, "View height: " + getHeight() + " width: " + getWidth());
        Log.i(LOG_NAME, "Map view goes from (" + minLat + "," + minLng + ") to (" + maxLat + "," + maxLng + ")");
    }
}
