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
package com.zippy.WayToGo.Widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.R;

/**
 *
 * @author alex
 */
public class IconTextView extends LinearLayout {

    private static final String LOG_NAME = IconTextView.class.getCanonicalName();
    final private TextView theTextView;
    final private BadgeView theSurface;
    final protected Context theContext;
    TextAppearanceSpan theSmallStyle;

    public IconTextView(Context aContext) {
        this(aContext, null);
    }

    public IconTextView(Context aContext, AttributeSet anAttributeSet) {
        this(aContext, anAttributeSet, R.layout.widget_icon_text_view);
    }

    public IconTextView(Context aContext, AttributeSet anAttributeSet, int aLayoutId) {
        super(aContext, anAttributeSet);
        theContext = aContext;
        setOrientation(HORIZONTAL);
        LayoutInflater inflater = (LayoutInflater) aContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(aLayoutId, this, true);
        theTextView = (TextView) findViewById(R.id.ictv_textview);
        theSurface = (BadgeView) findViewById(R.id.ictv_surface_view);
        //theBigStyle = new TextAppearanceSpan(theContext, android.R.style.TextAppearance_Medium);
        theSmallStyle = new TextAppearanceSpan(theContext, android.R.style.TextAppearance_Small);

    }

    public final void setTheAgency(BaseAgency theAgency) {
        theSurface.setTheAgency(theAgency);
    }

    public final void setBadgeText(String aBadge) {
        theSurface.setText(aBadge);
    }

    public final void setBadgeDrawable(Drawable aDrawable) {
        theSurface.setDrawable(aDrawable);
    }

    public final void setBadgeDrawable(int anId) {
        theSurface.setDrawable(anId);
    }

    public final void setText(String someText) {
        SpannableString str = new SpannableString(someText);

        int start = someText.indexOf("\n");

        if (start != -1) {
            str.setSpan(theSmallStyle, start, someText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        theTextView.setText(str, BufferType.SPANNABLE);

    }

    public final void setIconVisible(boolean isVisible) {
        theSurface.setVisibility(isVisible ? VISIBLE : GONE);
        theTextView.setGravity(
                isVisible ? Gravity.CENTER_VERTICAL : (Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL));
    }
}
