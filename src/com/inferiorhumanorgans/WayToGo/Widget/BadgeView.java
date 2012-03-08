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
// http://stackoverflow.com/questions/3035692/android-how-to-convert-a-drawable-to-bitmap
package com.inferiorhumanorgans.WayToGo.Widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.inferiorhumanorgans.WayToGo.Agency.BaseAgency;
import com.inferiorhumanorgans.WayToGo.TheApp;
import com.inferiorhumanorgans.WayToGo.Util.Route;

/**
 *
 * @author alex
 */
public class BadgeView extends View {

    private static final String LOG_NAME = BadgeView.class.getCanonicalName();
    private String theText, theSanitizedText;
    private boolean isValid = false;
    private float theRadius;
    private int theInnerWidth;
    private int fontSize;
    private Drawable theDrawable;
    private Context theContext;
    private static Typeface theBaseFont = null;
    private static Typeface theBoldFont = null;
    private BaseAgency theAgency = null;
    private Route theRoute = null;

    public BadgeView(Context aContext) {
        this(aContext, (AttributeSet) null);
    }

    public BadgeView(Context aContext, AttributeSet anAttributeSet) {
        super(aContext, anAttributeSet);
        theContext = aContext;
        if (theBaseFont == null) {
            theBaseFont = Typeface.createFromAsset(theContext.getAssets(), "fonts/swanse.ttf");
        }
        if (theBoldFont == null) {
            theBoldFont = Typeface.createFromAsset(theContext.getAssets(), "fonts/swanse_bold.ttf");
        }
        theAgency = null;
    }

    public void setTheAgency(BaseAgency theAgency) {
        this.theAgency = theAgency;
    }

    public void setText(String aText) {
        theDrawable = null;
        theText = aText;
        if (theAgency == null) {
            theSanitizedText = theText;
        } else {
            theRoute = theAgency.getRouteFromTag(aText);
            theSanitizedText = theRoute.tag();
        }
        invalidate();
    }

    public void setDrawable(int anId) {
        setDrawable(TheApp.getResDrawable(anId));
    }

    public void setDrawable(Drawable aDrawable) {
        theText = null;
        theDrawable = aDrawable;
        invalidate();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        isValid = false;
        fontSize = 20;
    }

    private void drawTheCircle(final Canvas aCanvas, final Rect someBounds) {
        final Paint ourPaint = new Paint();
        ourPaint.setColor(Color.LTGRAY);
        ourPaint.setAntiAlias(true);

        if (!isValid) {
            theRadius = (Math.min(someBounds.width(), someBounds.height()) / 2) - 8;
            theInnerWidth = (int) (theRadius * 2) - 8;
        }

        // Draw the first circle
        ourPaint.setStyle(Paint.Style.FILL);
        aCanvas.drawCircle(someBounds.centerX(), someBounds.centerY(), theRadius + 2, ourPaint);
    }

    private void drawTheSplash(final Canvas aCanvas, final Rect someBounds, final int aColor, final int aTextHeight) {
        final Paint ourPaint = new Paint();
        ourPaint.setColor(aColor);
        ourPaint.setAntiAlias(true);

        aCanvas.save(Canvas.CLIP_SAVE_FLAG);
        aCanvas.clipRect(someBounds.left, someBounds.centerY() + ((aTextHeight/2)+2), someBounds.right, someBounds.bottom);

        ourPaint.setColor(aColor);
        ourPaint.setStyle(Paint.Style.FILL);
        aCanvas.drawCircle(someBounds.centerX(), someBounds.centerY(), theRadius, ourPaint);


        aCanvas.restore();
    }

    @Override
    public void onDraw(final Canvas aCanvas) {
        Rect ourBounds = new Rect();
        getDrawingRect(ourBounds);

        super.onDraw(aCanvas);

        final int ourBgColor;
        if (theRoute != null) {
            ourBgColor = theRoute.color();
        } else {
            ourBgColor = Color.LTGRAY;
        }

        drawTheCircle(aCanvas, ourBounds);

        final Paint ourPaint = new Paint();
        ourPaint.setAntiAlias(true);
        ourPaint.setStyle(Paint.Style.FILL);

        if (theText != null) {
            ourPaint.setColor(Color.BLACK);
            ourPaint.setTextSize(TheApp.getRealPixels(fontSize));
            ourPaint.setTextAlign(Paint.Align.CENTER);

            final Rect ourTextBounds = new Rect();
            ourPaint.setTypeface(theBoldFont);
            ourPaint.getTextBounds(theSanitizedText, 0, theSanitizedText.length(), ourTextBounds);

            if (!isValid) {
                while ((ourTextBounds.width() >= theInnerWidth) && (fontSize > 8)) {
                    ourPaint.setTextSize(TheApp.getRealPixels(--fontSize));
                    ourPaint.getTextBounds(theSanitizedText, 0, theSanitizedText.length(), ourTextBounds);
                }
            }

            aCanvas.drawText(theSanitizedText, ourBounds.centerX() + 1f, ourBounds.centerY() + (ourTextBounds.height() / 2), ourPaint);
            // Draw the semi-circle if needed
            if (ourBgColor != Color.LTGRAY) {
                ourPaint.getTextBounds(theSanitizedText, 0, theSanitizedText.length(), ourTextBounds);
                drawTheSplash(aCanvas, ourBounds, ourBgColor, ourTextBounds.height());
            }
        } else if (theDrawable != null) {
            final Bitmap ourBitmap = ((BitmapDrawable) theDrawable).getBitmap();
            aCanvas.drawBitmap(ourBitmap, ourBounds.centerX() - (ourBitmap.getWidth() / 2), ourBounds.centerY() - (ourBitmap.getHeight() / 2), ourPaint);
        }
    }
}
