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
package com.inferiorhumanorgans.WayToGo.GPS;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

/**
 * http://stackoverflow.com/questions/2021176/android-gps-status
 * @author Alex
 */
class GPSListener implements GpsStatus.Listener, LocationListener {

    public boolean isGPSFix = false;
    protected long mLastLocationMillis = 0;
    public Location mLastLocation = null;
    private static final String LOG_NAME=GPSListener.class.getCanonicalName();

    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                //Log.d(LOG_NAME, "SAT STATUS UPDATE, last location is: " +mLastLocation);
                //Log.d(LOG_NAME, "TIme difference is: " + (SystemClock.elapsedRealtime() - mLastLocationMillis));
                if (mLastLocation != null) {
                    isGPSFix = (SystemClock.elapsedRealtime() - mLastLocationMillis) < 3000;
                }

                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                //Log.d(LOG_NAME, "GPS FIRST FIX");
                isGPSFix = true;

                break;
        }
        if (!isGPSFix) {
            mLastLocation = null;
        }
    }

    public void onLocationChanged(Location location) {
        //Log.d(LOG_NAME, "Location changed");
        if (location == null) {
            return;
        }

        mLastLocationMillis = SystemClock.elapsedRealtime();

        // Do something.

        mLastLocation = location;
        //isGPSFix = true;
    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onProviderEnabled(String arg0) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onProviderDisabled(String arg0) {
        //Log.d(LOG_NAME, "GPS PROVIDER DISABLED");
        isGPSFix = false;
        mLastLocation = null;
    }
}
