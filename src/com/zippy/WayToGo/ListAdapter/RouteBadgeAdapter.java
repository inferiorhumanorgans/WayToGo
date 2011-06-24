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
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.Util.Route;
import com.zippy.WayToGo.Widget.IconTextView;
import java.util.ArrayList;

/**
 *
 * @author alex
 */
public class RouteBadgeAdapter extends ArrayAdapter<Route> {

    private ArrayList<Route> theItems;
    private Context theContext = null;
    private BaseAgency theAgency = null;

    public RouteBadgeAdapter(Context aContext) {
        this(aContext, new ArrayList<Route>());
    }

    public RouteBadgeAdapter(Context aContext, ArrayList<Route> someItems) {
        super(aContext, R.layout.list_item_route, someItems);
        theItems = someItems;
        theContext = aContext;
    }

    public void setTheAgency(BaseAgency theAgency) {
        this.theAgency = theAgency;
    }

    public ArrayList<Route> getArray() {
        return theItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IconTextView v;
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) theContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = (IconTextView) inflater.inflate(R.layout.list_item_route, null);
            v.setFocusable(false);
            v.setFocusableInTouchMode(false);
            v.setClickable(false);
            v.setTheAgency(theAgency);
        } else {
            v = (IconTextView) convertView;
        }
        Route ourRoute = theItems.get(position);

        v.setBadgeText(ourRoute.getTheRawTag());
        v.setText(ourRoute.getTheName());
        return v;
    }
}
