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
package com.zippy.WayToGo.ListAdapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.Util.PredictionSummary;
import java.util.ArrayList;

/**
 *
 * @author alex
 */
public class PredictionAdapter extends ArrayAdapter<PredictionSummary> {

    private final ArrayList<PredictionSummary> theItems;
    private final Context theContext;

    public PredictionAdapter(Context aContext) {
        this(aContext, new ArrayList<PredictionSummary>());
    }

    public PredictionAdapter(Context aContext, ArrayList<PredictionSummary> someItems) {
        super(aContext, R.layout.list_item_text, someItems);
        theItems = someItems;
        theContext = aContext;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView v;
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) theContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = (TextView) inflater.inflate(R.layout.list_item_text, null);
            v.setFocusable(false);
            v.setFocusableInTouchMode(false);
            v.setClickable(false);
            v.setLinksClickable(false);
        } else {
            v = (TextView) convertView;
        }
        final PredictionSummary theSummary = theItems.get(position);

        if (theSummary.isEmpty() || theSummary.getTheFlags().equals("legit")) {
            v.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        } else {
            v.setGravity(Gravity.CENTER_VERTICAL);
        }
        v.setText(theSummary.toText());
        return v;
    }
}
