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
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import com.zippy.WayToGo.R;

/**
 *
 * @author alex
 */
public class SmallIconTextView extends IconTextView {
    public SmallIconTextView(Context aContext) {
        this(aContext, null);
    }

    public SmallIconTextView(Context aContext, AttributeSet anAttributeSet) {
        super(aContext, anAttributeSet, R.layout.widget_small_icon_text_view);
        this.findViewById(R.id.ictv_textview).setBackgroundResource(R.drawable.gradient_box);
        this.findViewById(R.id.ictv_surface_view).setBackgroundResource(R.drawable.gradient_box);
        this.findViewById(R.id.ictv_textview).getBackground().setDither(true);
        this.findViewById(R.id.ictv_surface_view).getBackground().setDither(true);
        theSmallStyle = new TextAppearanceSpan(theContext, android.R.style.TextAppearance_Small_Inverse);

    }
}
