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
package com.inferiorhumanorgans.WayToGo.Widget;

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
import com.inferiorhumanorgans.WayToGo.Agency.BaseAgency;
import com.inferiorhumanorgans.WayToGo.R;

/**
 *
 * @author alex
 */
public class IconTextView extends LinearLayout {

    private static final String LOG_NAME = IconTextView.class.getCanonicalName();
    private final TextView theTextView;
    private final BadgeView theSurface;
    protected final Context theContext;
    TextAppearanceSpan theSmallStyle;

    public IconTextView(final Context aContext) {
        this(aContext, null);
    }

    public IconTextView(final Context aContext, final AttributeSet anAttributeSet) {
        this(aContext, anAttributeSet, R.layout.widget_icon_text_view);
    }

    public IconTextView(final Context aContext, final AttributeSet anAttributeSet, final int aLayoutId) {
        super(aContext, anAttributeSet);
        theContext = aContext;
        setOrientation(HORIZONTAL);
        final LayoutInflater ourInflater = (LayoutInflater) aContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ourInflater.inflate(aLayoutId, this, true);
        theTextView = (TextView) findViewById(R.id.ictv_textview);
        theSurface = (BadgeView) findViewById(R.id.ictv_surface_view);
        //theBigStyle = new TextAppearanceSpan(theContext, android.R.style.TextAppearance_Medium);
        theSmallStyle = new TextAppearanceSpan(theContext, android.R.style.TextAppearance_Small);

    }

    public final void setTheAgency(final BaseAgency anAgency) {
        theSurface.setTheAgency(anAgency);
    }

    public final void setBadgeText(final String aBadge) {
        theSurface.setText(aBadge);
    }

    public final void setBadgeDrawable(final Drawable aDrawable) {
        theSurface.setDrawable(aDrawable);
    }

    public final void setBadgeDrawable(final int anId) {
        theSurface.setDrawable(anId);
    }

    public final void setText(final String someText) {
        SpannableString str = new SpannableString(someText);

        final int start = someText.indexOf("\n");

        if (start != -1) {
            str.setSpan(theSmallStyle, start, someText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        theTextView.setText(str, BufferType.SPANNABLE);
    }

    public final void setIconVisible(final boolean isVisible) {
        theSurface.setVisibility(isVisible ? VISIBLE : GONE);
        theTextView.setGravity(
                isVisible ? Gravity.CENTER_VERTICAL : (Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL));
    }
}
