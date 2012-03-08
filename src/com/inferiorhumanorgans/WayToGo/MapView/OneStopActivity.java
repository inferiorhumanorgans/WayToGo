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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.inferiorhumanorgans.WayToGo.R;
import com.inferiorhumanorgans.WayToGo.TheApp;
import com.inferiorhumanorgans.WayToGo.Util.Stop;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapController;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayManager;

/**
 *
 * @author alex
 */
public class OneStopActivity extends Activity implements OSMReadyListener, ItemizedIconOverlay.OnItemGestureListener {

    private static final String LOG_NAME = OneStopActivity.class.getCanonicalName();
    private OSMView theMapView;
    private MapController theMapController;
    private boolean isReady = false;
    private MapMarker theMarker;
    private OverlayManager theManager;
    private Stop theStop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        final Intent theIntent = getIntent();
        theStop = theIntent.getParcelableExtra("theStop");

        this.setTitle(this.getTitle() + " - " + theStop.name());

        theMapView = (OSMView) findViewById(R.id.osm_map_view);
        theMapView.setTileSource(TileSourceFactory.MAPNIK);
        theMapView.setBuiltInZoomControls(true);
        theMapView.setOnReadyListener(this);
        theMapController = theMapView.getController();
        theMapController.setZoom(TheApp.getResInt(R.integer.default_map_zoom) - 1);
        theMapController.setCenter(theStop.point());

        theManager = theMapView.getOverlayManager();
        theMarker = new MapMarker(this);
        theMarker.setLocation(theStop.point());
        theManager.add(theMarker);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu aMenu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.osm_onestop_menu, aMenu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_return_to_stop:
                theMapController.setCenter(theStop.point());
                return true;
            case R.id.context_return_to_location:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void finishedWithLayout(boolean isFinished) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean onItemSingleTapUp(int index, Object item) {
        return false;
    }

    @Override
    public boolean onItemLongPress(int index, Object item) {
        return false;
    }
}
