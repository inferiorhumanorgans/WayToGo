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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.inferiorhumanorgans.WayToGo.TheApp;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author alex
 */
public class LocationFinder {

    private ServiceConnection theGPSServiceConnection = null;
    private boolean mGPSIsBound = false;
    private com.inferiorhumanorgans.WayToGo.GPS.TheGPSService theGPSService = null;
    private Timer theLocationTimer = null;
    private int theLocationTimerCount = 0;
    private final Context theContext;
    private final String LOG_NAME = LocationFinder.class.getCanonicalName();
    private final LocationFinder.Listener theListener;
    private final Handler mHandler = new Handler();

    public static interface Listener {
        public void onLocationFound(Location aLocation);
        public void onLocationNotFound();
    }

    public LocationFinder(Context aContext, LocationFinder.Listener aListener) {
        theContext = aContext;
        theListener = aListener;
        connectToGPSService();
    }

    public void init() {
        Log.d(LOG_NAME, "init");
        if (!mGPSIsBound) {
            Log.d(LOG_NAME, "Connecting to service");
            connectToGPSService();
        }
    }

    public void finish() {
        Log.d(LOG_NAME, "finish");
        if (theGPSServiceConnection != null) {
            Log.d(LOG_NAME, "We're unbinding the service now");
            TheApp.getContext().unbindService(theGPSServiceConnection);
        }
        mGPSIsBound = false;
        theGPSServiceConnection = null;
        if (theLocationTimer != null) {
            Log.d(LOG_NAME, "Timer is still active, so we're going to cancel, purge, and nullify it");
            theLocationTimer.cancel();
            theLocationTimer.purge();
        }
        theLocationTimerCount = 0;
    }

    public boolean startLocationSearch() {
        Log.d(LOG_NAME, "startLocationSearch");
        if (!com.inferiorhumanorgans.WayToGo.GPS.TheGPSService.isProviderOn()) {
            Log.d(LOG_NAME, "GPS IS OFF, ABORTING");
            return false;
        }
        Log.d(LOG_NAME, "GPS IS ON, GAME ON.");

        theLocationTimerCount = 0;
        if (theLocationTimer != null) {
            theLocationTimer.cancel();
            theLocationTimer.purge();
        }
        theLocationTimer = new Timer();
        theLocationTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                checkLocation();
            }
        }, 0, 250);
        return true;
    }

    protected void checkLocation() {
        ++theLocationTimerCount;

        if (theLocationTimerCount > (TheApp.MAX_GPS_WAIT * 4)) {
            Log.e(LOG_NAME, "Timed out, hoping for the best.");
            finishLocationSearch();
            return;
        }

        if (!mGPSIsBound || theGPSService == null) {
//            Log.e(LOG_NAME, "NO GPS SERVICE YET???");
            return;
        }
        if (theGPSService.isGPSFix()) {
//            Log.d(LOG_NAME, "We should have a GPS fix! " + theGPSService.location());
            finishLocationSearch();
            return;
        }
//        Log.d(LOG_NAME, "Checking location, no fix yet.");
    }

    protected void finishLocationSearch() {
        Log.d(LOG_NAME, "Finished with location search!!!");

        mHandler.post(new Runnable() {

            public void run() {
                if (theLocationTimer != null) {
                    theLocationTimer.cancel();
                    theLocationTimer.purge();
                }
                if (theLocationTimerCount > (TheApp.MAX_GPS_WAIT * 4)) {
                    Log.e(LOG_NAME, "Timed out, let's hope we don't do it again. Is fix is: " + theGPSService.isGPSFix());
                }
                if (theGPSService.isGPSFix()) {
                    theListener.onLocationFound(theGPSService.location());
                } else {
                    theListener.onLocationNotFound();
                }
            }
        });
    }

    private void connectToGPSService() {
        if (theGPSServiceConnection != null) {
            Log.d(LOG_NAME, "We've already got a connection???");
            return;
        }
        // Trying to boot up our gps service.
        theGPSServiceConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName className, IBinder service) {
                // This is called when the connection with the service has been
                // established, giving us the service object we can use to
                // interact with the service.  Because we have bound to a explicit
                // service that we know is running in our own process, we can
                // cast its IBinder to a concrete class and directly access it.
                Log.d(LOG_NAME, "onServiceConnected");
                theGPSService = ((com.inferiorhumanorgans.WayToGo.GPS.TheGPSService.LocalBinder) service).getService();
                // TheGPSService connected.
                mGPSIsBound = true;
            }

            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                // Because it is running in our same process, we should never
                // see this happen.
                Log.d(LOG_NAME, "onServiceDisconnected");
                mGPSIsBound = false;
                theGPSService = null;
                theGPSServiceConnection = null;
            }
        };
        if (theGPSServiceConnection == null) {
            // Couldn't boot up service???
            return;
        }
        // Trying to bind to service?
        TheApp.getContext().bindService(new Intent(theContext, com.inferiorhumanorgans.WayToGo.GPS.TheGPSService.class), theGPSServiceConnection, Context.BIND_AUTO_CREATE);
        // Finished with boot up?
    }
}
