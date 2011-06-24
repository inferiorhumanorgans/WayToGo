/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.readystatesoftware.mapviewballoons;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.MainActivity;
import com.zippy.WayToGo.MapView.StopOverlayItem;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.Util.Stop;
import java.util.ArrayList;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * An abstract extension of ItemizedOverlay for displaying an information balloon
 * upon screen-tap of each marker overlay.
 * 
 * @author Jeff Gilfelt
 */
public class BalloonItemizedOverlay<Item extends OverlayItem> extends ItemizedOverlay<Item> {

    private static final String LOG_NAME = BalloonItemizedOverlay.class.getCanonicalName();
    private BalloonOverlayView<Item> balloonView;
    private View clickRegion;
    private int viewOffset;
    private Item currentFocussedItem;
    private int currentFocussedIndex;
    private MapView mapView;
    private MapController mc;
    protected final List<Item> mItemList;
    private static final int mDrawnItemsLimit = Integer.MAX_VALUE;
    final private Context theContext;

    /**
     * Create a new BalloonItemizedOverlay
     *
     * @param defaultMarker - A bounded Drawable to be drawn on the map for each item in the overlay.
     * @param mapView - The view upon which the overlay items are to be drawn.
     */
    public BalloonItemizedOverlay(List<Item> pList, Drawable defaultMarker, Context aContext, MapView aMapView) {
        super(defaultMarker, new DefaultResourceProxyImpl(aContext));
        theContext = aContext;
        mItemList = pList;
        viewOffset = 0;
        mapView = aMapView;
        mc = mapView.getController();
        populate();
    }

    public BalloonItemizedOverlay(Drawable defaultMarker, Context aContext, MapView aMapView) {
        this(new ArrayList<Item>(), defaultMarker, aContext, aMapView);
    }

    @Override
    protected Item createItem(int index) {
        return mItemList.get(index);
    }

    @Override
    public int size() {
        return Math.min(mItemList.size(), mDrawnItemsLimit);
    }

    public boolean addItem(Item item) {
        boolean result = mItemList.add(item);
        populate();
        return result;
    }

    public void addItem(int location, Item item) {
        mItemList.add(location, item);
    }

    public boolean addItems(List<Item> items) {
        boolean result = mItemList.addAll(items);
        populate();
        return result;
    }

    public void removeAllItems() {
        removeAllItems(true);
    }

    public void removeAllItems(boolean withPopulate) {
        mItemList.clear();
        if (withPopulate) {
            populate();
        }
    }

    public boolean removeItem(Item item) {
        boolean result = mItemList.remove(item);
        populate();
        return result;
    }

    public Item removeItem(int position) {
        Item result = mItemList.remove(position);
        populate();
        return result;
    }

    /**
     * Set the horizontal distance between the marker and the bottom of the information
     * balloon. The default is 0 which works well for center bounded markers. If your
     * marker is center-bottom bounded, call this before adding overlay items to ensure
     * the balloon hovers exactly above the marker.
     *
     * @param pixels - The padding between the center point and the bottom of the
     * information balloon.
     */
    public void setBalloonBottomOffset(int pixels) {
        viewOffset = pixels;
    }

    public int getBalloonBottomOffset() {
        return viewOffset;
    }

    /**
     * Override this method to handle a "tap" on a balloon. By default, does nothing
     * and returns false.
     *
     * @param index - The index of the item whose balloon is tapped.
     * @param item - The item whose balloon is tapped.
     * @return true if you handled the tap, otherwise false.
     */
    protected boolean onBalloonTap(int index, Item item) {
        if (!(item instanceof StopOverlayItem)) {
            return true;
        }

        final StopOverlayItem stopItem = (StopOverlayItem) item;
        final Stop ourStop = stopItem.getTheStop();
        final BaseAgency ourAgency = ourStop.getTheAgency();
        final Intent ourIntent = ourAgency.getPredictionIntentForStop(ourStop);

        if (theContext instanceof Activity) {
            final Activity ourActivity = (Activity) theContext;
            final MainActivity ourMain = (MainActivity) ourActivity.getParent();
            com.zippy.WayToGo.ActivityGroup ag = (com.zippy.WayToGo.ActivityGroup) ourMain.getActivityForTabTag(ourAgency.getClass().getCanonicalName());
            if (ag != null) {
                Log.d(LOG_NAME, "Creating on activity group: " + ag);
                ag.startChildActivity(ourIntent.toUri(0), ourIntent);
            } else {
                Log.d(LOG_NAME, "No activity group found??");
                theContext.startActivity(ourIntent);
            }
        }
        return true;
    }

    @Override
    public boolean onSingleTapUp(final MotionEvent event, final MapView mapView) {
        final Projection pj = mapView.getProjection();
        final int eventX = (int) event.getX();
        final int eventY = (int) event.getY();
        final Point mItemPoint = new Point();
        final Point mTouchScreenPoint = new Point();

        /* These objects are created to avoid construct new ones every cycle. */
        pj.fromMapPixels(eventX, eventY, mTouchScreenPoint);
        int markerIndex = -1;

        for (int i = 0; i < this.mItemList.size(); ++i) {
            final Item item = getItem(i);
            final Drawable marker = (item.getMarker(0) == null) ? this.mDefaultMarker : item.getMarker(0);

            pj.toPixels(item.getPoint(), mItemPoint);

            if (hitTest(item, marker, mTouchScreenPoint.x - mItemPoint.x, mTouchScreenPoint.y
                    - mItemPoint.y)) {
                markerIndex = i;
                break;
            }
        }

        if (markerIndex == -1) {
            return false;
        }

        currentFocussedIndex = markerIndex;
        currentFocussedItem = createItem(markerIndex);

        boolean isRecycled;
        if (balloonView == null) {
            balloonView = createBalloonOverlayView();
            clickRegion = (View) balloonView.findViewById(R.id.balloon_inner_layout);
            clickRegion.setOnTouchListener(createBalloonTouchListener());
            isRecycled = false;
        } else {
            isRecycled = true;
        }

        balloonView.setVisibility(View.GONE);

        List<Overlay> mapOverlays = mapView.getOverlays();
        if (mapOverlays.size() > 1) {
            hideOtherBalloons(mapOverlays);
        }

        balloonView.setData(currentFocussedItem);

        GeoPoint point = currentFocussedItem.getPoint();
        MapView.LayoutParams params = new MapView.LayoutParams(
                MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, point,
                MapView.LayoutParams.BOTTOM_CENTER, 0, 0);
//		params.mode = MapView.LayoutParams.MODE_MAP;

        balloonView.setVisibility(View.VISIBLE);

        if (isRecycled) {
            balloonView.setLayoutParams(params);
        } else {
            mapView.addView(balloonView, params);
        }

        mc.animateTo(point);
        return true;
    }

    /**
     * Creates the balloon view. Override to create a sub-classed view that
     * can populate additional sub-views.
     */
    protected BalloonOverlayView<Item> createBalloonOverlayView() {
        return new BalloonOverlayView<Item>(getMapView().getContext(), getBalloonBottomOffset());
    }

    /**
     * Expose map view to subclasses.
     * Helps with creation of balloon views.
     */
    protected MapView getMapView() {
        return mapView;
    }

    /**
     * Sets the visibility of this overlay's balloon view to GONE.
     */
    protected void hideBalloon() {
        if (balloonView != null) {
            balloonView.setVisibility(View.GONE);
        }
    }

    /**
     * Hides the balloon view for any other BalloonItemizedOverlay instances
     * that might be present on the MapView.
     *
     * @param overlays - list of overlays (including this) on the MapView.
     */
    private void hideOtherBalloons(List<Overlay> overlays) {

        for (Overlay overlay : overlays) {
            if (overlay instanceof BalloonItemizedOverlay<?> && overlay != this) {
                ((BalloonItemizedOverlay<?>) overlay).hideBalloon();
            }
        }

    }

    /**
     * Sets the onTouchListener for the balloon being displayed, calling the
     * overridden {@link #onBalloonTap} method.
     */
    private OnTouchListener createBalloonTouchListener() {
        return new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                View l = ((View) v.getParent()).findViewById(R.id.balloon_main_layout);
                Drawable d = l.getBackground();

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int[] states = {android.R.attr.state_pressed};
                    if (d.setState(states)) {
                        d.invalidateSelf();
                    }
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    int newStates[] = {};
                    if (d.setState(newStates)) {
                        d.invalidateSelf();
                    }
                    // call overridden method
                    onBalloonTap(currentFocussedIndex, currentFocussedItem);
                    return true;
                } else {
                    return false;
                }

            }
        };
    }

    public boolean onSnapToItem(int x, int y, Point snapPoint, MapView mapView) {
        return false;
    }
}
