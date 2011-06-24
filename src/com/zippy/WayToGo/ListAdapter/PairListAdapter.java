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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.zippy.WayToGo.Util.StringPairList;
import com.zippy.WayToGo.Util.StringPairList.StringPair;
import com.zippy.WayToGo.R;

/**
 *
 * @author alex
 */
public class PairListAdapter extends ArrayAdapter<StringPair> {

    private StringPairList theItems;
    private Context theContext = null;

    public PairListAdapter(Context aContext) {
        this(aContext, new StringPairList());
    }

    public PairListAdapter(Context aContext, StringPairList someItems) {
        super(aContext, R.layout.list_item_text, someItems);
        theItems = someItems;
        theContext = aContext;
    }

    public void addAll(StringPairList aList) {
        for (StringPair aPair : aList)
            this.add(aPair);
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
        StringPair thePair = theItems.get(position);
        v.setText(thePair.first);// + " / " + thePair.second);
        return v;
    }
}
