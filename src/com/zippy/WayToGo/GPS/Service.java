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
package com.zippy.WayToGo.GPS;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import com.zippy.WayToGo.TheApp;

/**
 * http://stackoverflow.com/questions/2021176/android-gps-status
 * Our runs in the background and polls the GPS provider service.
 * @author alex
 */
public final class Service extends android.app.Service {

    private final static String LOG_NAME = Service.class.getCanonicalName();
    private volatile static Intent theIntent = null;
    private HandlerThread theGPSThread;
    private static LocationManager lm;
    private GPSListener gpsListener = null;
    private final IBinder mBinder = new LocalBinder();
    /**
     * Minimum number of seconds to wait between GPS updates
     */
    private static final int GPS_MIN_POLL = 45;
    /*
     * Minimum number of meters required to trigger a new GPS event
     */
    private static final float GPS_MIN_DISTANCE = 0.0f;

    public final class LocalBinder extends Binder {

        public Service getService() {
            return Service.this;
        }
    }

    /**
     *
     * @return An intent which will launch this service into the stratosphere.
     */
    public static Intent getIntent() {
        if (theIntent == null) {
            theIntent = new Intent(TheApp.getContext(), Service.class);
        }
        return theIntent;
    }

    public final synchronized boolean isGPSFix() {
        if (gpsListener == null) {
            return false;
        }
        return gpsListener.isGPSFix;
    }

    public static synchronized boolean isProviderOn() {
        if (lm == null) {
            lm = (LocationManager) TheApp.getContext().getSystemService(LOCATION_SERVICE);
        }
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public final synchronized Location location() {
        return gpsListener.mLastLocation;
    }

    private synchronized void setupGPS() {
        if (theGPSThread != null) {
            theGPSThread.quit();
        }
        gpsListener = new GPSListener();
        if (lm == null) {
            lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
        lm.addGpsStatusListener(gpsListener);

        theGPSThread = new HandlerThread("GPSThread");
        theGPSThread.start();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_POLL * 1000, GPS_MIN_DISTANCE, gpsListener, theGPSThread.getLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Log.d(LOG_NAME, "We've been bound");
        return mBinder;
    }

    @Override
    public void onCreate() {
        //Log.d(LOG_NAME, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d(LOG_NAME, "Received start id " + startId + ": " + intent);
        setupGPS();
        return START_STICKY;
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        //Log.d(LOG_NAME, "We've been destroyed");
        if (gpsListener != null) {
            lm.removeGpsStatusListener(gpsListener);
            lm.removeUpdates(gpsListener);
        }

        if (theGPSThread != null) {
            theGPSThread.quit();
        }
    }
}
