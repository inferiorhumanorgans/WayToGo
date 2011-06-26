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
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.TheApp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author alex
 */
public class ExpandableArrayAdapter<T> extends BaseExpandableListAdapter {

    protected static final String LOG_NAME = ExpandableArrayAdapter.class.getCanonicalName();
    protected ArrayList<String> theGroups = new ArrayList<String>();
    protected Context theContext = null;
    protected HashMap<String, ArrayList<T>> theChildren = new HashMap<String, ArrayList<T>>();
    protected final Drawable EXPANDED_DRAWABLE =
            TheApp.getResDrawable((android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO) ? R.drawable.expander_ic_expanded_gingerbread : R.drawable.expander_ic_expanded_old);
    protected final Drawable COLLAPSED_DRAWABLE =
            TheApp.getResDrawable((android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.FROYO) ? R.drawable.expander_ic_collapsed_gingerbread : R.drawable.expander_ic_collapsed_old);;

    public ExpandableArrayAdapter(Context aContext) {
        theContext = aContext;
    }

    public void clear() {
        theGroups.clear();
        theChildren.clear();
        notifyDataSetChanged();
    }

    public void addItemToGroup(final String aGroup, final T anItem) {
        if (!theGroups.contains(aGroup)) {
            theGroups.add(aGroup);
        }
        if (!theChildren.containsKey(aGroup)) {
            theChildren.put(aGroup, new ArrayList<T>());
        }

        theChildren.get(aGroup).add(anItem);
        notifyDataSetChanged();
    }

    public int getGroupCount() {
        return theGroups.size();
    }

    public int getChildrenCount(final int aPosition) {
        String theGroup = theGroups.get(aPosition);
        if (theChildren.containsKey(theGroup)) {
            return theChildren.get(theGroup).size();
        } else {
            return 0;
        }
    }

    public Object getGroup(final int aPosition) {
        return theGroups.get(aPosition);
    }

    public Object getChild(final int groupPosition, final int childPosition) {
        String theGroup = this.theGroups.get(groupPosition);
        return this.theChildren.get(theGroup).get(childPosition);
    }

    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    public long getChildId(final int groupPosition, final int childPosition) {
        return (groupPosition * 1000000) + childPosition;
    }

    public boolean hasStableIds() {
        return false;
    }

    public View getGroupView(final int aGroupPosition, final boolean isExpanded, View convertView, final ViewGroup parent) {
        LinearLayout ll = null;
        TextView theView = null;
        ImageView expander = null;
        final LayoutInflater inflater = (LayoutInflater) theContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Gingerbread is so very, very broken.  Sigh.
        // http://code.google.com/p/android/issues/detail?id=12977
        // Unfortunately so is Froyo.
        if (convertView == null) {
            ll = (LinearLayout) inflater.inflate(R.layout.expandable_group_item, null);
        } else {
            ll = (LinearLayout) convertView;
        }
        expander = (ImageView) ll.findViewById(R.id.exp_group_indicator);
        theView = (TextView) ll.findViewById(R.id.exp_list_view_item);

        // Gross hack for 2.3.x that has a buggy expandable list view. :(
        if (getChildrenCount(aGroupPosition) == 0) {
            expander.setImageDrawable(null);
            expander.setVisibility(View.GONE);
            theView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        } else {
            expander.setImageDrawable(isExpanded ? EXPANDED_DRAWABLE : COLLAPSED_DRAWABLE);
            expander.setVisibility(View.VISIBLE);
            theView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }
        theView.setText(this.theGroups.get(aGroupPosition));

        return ll;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TextView theView;
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) theContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            theView = (TextView) inflater.inflate(R.layout.list_item_text, null);

        } else {
            theView = (TextView) convertView;
        }
        final String theGroup = theGroups.get(groupPosition);
        theView.setText(theChildren.get(theGroup).get(childPosition).toString());
        return theView;

    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
