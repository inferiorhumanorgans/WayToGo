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
package com.zippy.WayToGo.Widget;

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
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.Route;

/**
 *
 * @author alex
 */
public class BadgeView extends View {

    private static final String LOG_NAME = BadgeView.class.getSimpleName();
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
            theSanitizedText = theRoute.getTheTag();
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

    private void drawTheCircle(Canvas aCanvas, Rect someBounds, int aColor) {
        final Paint thePaint = new Paint();
        thePaint.setColor(Color.LTGRAY);
        thePaint.setAntiAlias(true);

        if (!isValid) {
            theRadius = (Math.min(someBounds.width(), someBounds.height()) / 2) - 8;
            theInnerWidth = (int) (theRadius * 2) - 8;
        }

        // Draw the first circle
        thePaint.setStyle(Paint.Style.FILL);
        aCanvas.drawCircle(someBounds.centerX(), someBounds.centerY(), theRadius+2, thePaint);

        // Draw the semi-circle if needed
        if (aColor == Color.LTGRAY)
            return;
        aCanvas.save(Canvas.CLIP_SAVE_FLAG);
        aCanvas.clipRect(someBounds.left, someBounds.centerY()+10, someBounds.right, someBounds.bottom);

        thePaint.setColor(aColor);
        thePaint.setStyle(Paint.Style.FILL);
        aCanvas.drawCircle(someBounds.centerX(), someBounds.centerY(), theRadius, thePaint);


        aCanvas.restore();
    }

    @Override
    public void onDraw(Canvas canvas) {
        Rect theBounds = new Rect();
        getDrawingRect(theBounds);

        super.onDraw(canvas);

        final int theBgColor;
        if (theRoute != null) {
            theBgColor = theRoute.getTheColor();
        } else {
            theBgColor = Color.LTGRAY;
        }

        drawTheCircle(canvas, theBounds, theBgColor);

        final Paint thePaint = new Paint();
        thePaint.setAntiAlias(true);
        thePaint.setStyle(Paint.Style.FILL);

        if (theText != null) {
            thePaint.setColor(Color.BLACK);
            thePaint.setTextSize(TheApp.getRealPixels(fontSize));
            thePaint.setTextAlign(Paint.Align.CENTER);

            final Rect theTextBounds = new Rect();
            thePaint.setTypeface(theBoldFont);
            thePaint.getTextBounds(theSanitizedText, 0, theSanitizedText.length(), theTextBounds);

            if (!isValid) {
                while ((theTextBounds.width() >= theInnerWidth) && (fontSize > 8)) {
                    thePaint.setTextSize(TheApp.getRealPixels(--fontSize));
                    thePaint.getTextBounds(theSanitizedText, 0, theSanitizedText.length(), theTextBounds);
                }
            }

            canvas.drawText(theSanitizedText, theBounds.centerX()+1f, theBounds.centerY() + (theTextBounds.height() / 2), thePaint);
        } else if (theDrawable != null) {
            final Bitmap theBitmap = ((BitmapDrawable) theDrawable).getBitmap();
            canvas.drawBitmap(theBitmap, theBounds.centerX() - (theBitmap.getWidth() / 2), theBounds.centerY() - (theBitmap.getHeight() / 2), thePaint);
        }
    }
}
