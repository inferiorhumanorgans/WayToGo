// Created by plusminus on 22:01:11 - 29.09.2008
// package org.osmdroid.views.overlay;

package com.zippy.WayToGo.MapView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import com.zippy.WayToGo.R;
import org.osmdroid.views.overlay.Overlay;

/**
 *
 * @author alex
 * @author Nicolas Gramlich
 *
 */
public final class MapMarker extends Overlay {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final Paint mPaint = new Paint();

	protected Bitmap ICON;
	/** Coordinates the feet of the person are located. */
	protected android.graphics.Point HOTSPOT = new android.graphics.Point(16, 32);

	protected GeoPoint mLocation;
	private final Point screenCoords = new Point();

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapMarker(final Context ctx) {
		super(ctx);
		this.ICON = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.red_marker);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void setLocation(final GeoPoint mp) {
		this.mLocation = mp;
	}

	public final GeoPoint getLocation() {
		return this.mLocation;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	public final void draw(final Canvas c, final MapView osmv, final boolean shadow) {
		if (!shadow && this.mLocation != null) {
			final Projection pj = osmv.getProjection();
			pj.toMapPixels(this.mLocation, screenCoords);

			c.drawBitmap(ICON, screenCoords.x - HOTSPOT.x, screenCoords.y
					- HOTSPOT.y, this.mPaint);
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}