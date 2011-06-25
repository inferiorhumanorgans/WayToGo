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
// http://stackoverflow.com/questions/3880623/how-to-show-a-balloon-above-a-marker-in-a-mapactivity-isnt-there-a-widget
package com.zippy.WayToGo.MapView;

import android.app.Activity;
import android.location.Location;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.GPS.LocationFinder;
import java.util.ArrayList;
import org.osmdroid.events.MapListener;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;

/**
 *
 * @author alex
 */
public class AllStopsActivity extends Activity implements OSMReadyListener, MapListener, ItemizedIconOverlay.OnItemGestureListener, LocationFinder.Listener {

    private static final String LOG_NAME = AllStopsActivity.class.getCanonicalName();
    private OSMView theMapView;
    private MapController theMapController;
    private boolean isReady = false;
    private MapMarker theMarker;
    private OverlayManager theManager;
    private BalloonItemizedOverlay theItemizedOverlay;
    private final GeoPoint theDefaultCenter = new GeoPoint(TheApp.getResInt(R.integer.default_map_lat), TheApp.getResInt(R.integer.default_map_lng));
    private LocationFinder theFinder;
    private Location theLocation;
    private boolean isLookingForLocation = false;
    private boolean isAddingStops = false;
    private OSMGetStopsTask theStopTask;
    private boolean needMoreStops = false;
    private boolean isPaused = false;

    // http://stackoverflow.com/questions/3538546/custom-title-bar-with-progress-in-android
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        theMapView = (OSMView) findViewById(R.id.osm_map_view);
        theMapView.setTileSource(TileSourceFactory.MAPNIK);
        theMapView.setBuiltInZoomControls(true);
        theMapView.setOnReadyListener(this);
        theMapController = theMapView.getController();
        theMapController.setZoom(TheApp.getResInt(R.integer.default_map_zoom));
        theMapController.setCenter(theDefaultCenter);
        theFinder = new LocationFinder(getParent(), this);
    }

    @Override
    public void onResume() {
        super.onResume();
        theFinder.init();
        isReady = needMoreStops = isPaused = isAddingStops = isLookingForLocation = false;
        if (theStopTask != null) {
            Log.d(LOG_NAME, "There's a lingering stop task.");
            theStopTask.cancel(true);
            theStopTask = null;
        }
    }

    @Override
    public void onPause() {
        Log.d(LOG_NAME, "onPause");
        isPaused = true;
        super.onPause();
        if (theStopTask != null) {
            Log.d(LOG_NAME, "Canceling the stop task");
            theStopTask.cancel(true);
            theStopTask = null;
        } else {
            Log.d(LOG_NAME, "What stop task??");
        }
        theFinder.finish();
        getParent().setProgressBarIndeterminateVisibility(false);
    }

    /*    @Override
    public boolean onPrepareOptionsMenu(Menu aMenu) {
    super.onPrepareOptionsMenu(aMenu);
    MenuItem theMenuItem = aMenu.findItem(R.id.menu_agency_site);
    if (theMenuItem != null) {
    theMenuItem.setEnabled(false);
    theMenuItem.setVisible(false);
    }
    return true;
    }*/
    @Override
    public boolean onCreateOptionsMenu(Menu aMenu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.osm_allstop_menu, aMenu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_return_to_location:
                startLocationSearch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // http://www.google.com/intl/en_us/mapfiles/ms/micons/red.png
    public void finishedWithLayout(boolean isFinished) {
        if (isFinished && !isReady) {
            isReady = true;
            getParent().setProgressBarIndeterminateVisibility(true);
            theMapView.setMapListener(this);
            theMapController.animateTo(theDefaultCenter);
            startLocationSearch();
            theManager = theMapView.getOverlayManager();
            theItemizedOverlay = new BalloonItemizedOverlay(TheApp.getResDrawable(R.drawable.blue_marker), this, theMapView);
            theManager.add(theItemizedOverlay);
        }
    }

    public synchronized void addStops(ArrayList<OverlayItem> someStops) {
        Log.d(LOG_NAME, "addStops");
        if (isPaused == true || someStops == null) {
            Log.d(LOG_NAME, "We're paused or were passed a null arraylist, returning with no action");
            needMoreStops = false;
        } else {
            Log.d(LOG_NAME, "Actually adding stops.");
            theItemizedOverlay.removeAllItems();
            theItemizedOverlay.addItems(someStops);
            theMapView.invalidate();
            isAddingStops = false;

            if (needMoreStops == true) {
                needMoreStops = false;
                refreshMarkers();
            }
        }

        //theStopTask = null;

        if (!isLookingForLocation) {
            getParent().setProgressBarIndeterminateVisibility(false);
        }
    }

    protected void startLocationSearch() {
        if (theFinder.startLocationSearch()) {
            isLookingForLocation = true;
            getParent().setProgressBarIndeterminateVisibility(true);
        } else {
            isLookingForLocation = false;
        }
    }

    @Override
    public synchronized void onLocationFound(Location aLocation) {
        Log.d(LOG_NAME, "Probably have a GPS Fix");
        isLookingForLocation = false;
        theLocation = aLocation;
        if (theLocation != null) {
            if (theMarker != null) {
                theManager.remove(theMarker);
            }
            theMarker = new MapMarker(this);
            final GeoPoint thePoint = new GeoPoint(theLocation.getLatitude(), theLocation.getLongitude());
            theMarker.setLocation(thePoint);
            theManager.add(theMarker);
            theMapController.setCenter(thePoint);
        }

        if (!this.isAddingStops) {
            getParent().setProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    public synchronized void onLocationNotFound() {
        isLookingForLocation = false;
        theLocation = null;
        if (!this.isAddingStops) {
            getParent().setProgressBarIndeterminateVisibility(false);
        }
    }

    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onItemSingleTapUp(int anIndex, Object anItem) {
        if (!(anItem instanceof OverlayItem)) {
            return false;
        }
        final OverlayItem theItem = (OverlayItem) anItem;
        final GeoPoint thePoint = theItem.getPoint();
        Log.i(LOG_NAME, "Got TAP: " + anIndex + "/" + thePoint.toString());
        return true;
    }

    @Override
    public boolean onItemLongPress(int index, Object item) {
        return false;
    }

    private synchronized void refreshMarkers() {
        //Log.d(LOG_NAME, "refreshMarkers");
        if (isAddingStops) {
            //Log.d(LOG_NAME, "While adding stops!");
            needMoreStops = true;
            return;
        }
        if (isPaused == true) {
            Log.d(LOG_NAME, "REFRESH MARKERS WHILE PAUSED!!!");
            return;
        }
        if (theStopTask != null) {
            theStopTask.cancel(true);
            theStopTask = null;
        }
        theStopTask = new OSMGetStopsTask(this, theMapView.getBoundingBox());
        getParent().setProgressBarIndeterminateVisibility(true);
        theStopTask.execute();
        isAddingStops = true;
    }

    public boolean onScroll(ScrollEvent event) {
        if (isPaused == true) {
            Log.d(LOG_NAME, "onScroll (WHILE PAUSED)");
            return true;
        }
        //Log.d(LOG_NAME, "onScroll");
        refreshMarkers();
        return true;
    }

    public boolean onZoom(ZoomEvent event) {
        return false;
    }
}
