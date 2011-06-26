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
package com.zippy.WayToGo.Agency.BART.Prediction;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.zippy.WayToGo.ListAdapter.ExpandableArrayAdapter;
import com.zippy.WayToGo.Util.Prediction;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Widget.IconTextView;
import java.util.HashMap;

/**
 *
 * @author alex
 */
public class ShowPredictionsAdapter extends ExpandableArrayAdapter<Prediction> {

    protected final HashMap<String, Drawable> theBadges;

    public ShowPredictionsAdapter(final Context aContext) {
        super(aContext);
        theBadges = new HashMap<String, Drawable>();
    }

    @Override
    public View getChildView(final int aGroupPosition, final int aChildPosition, final boolean isLastChild, final View aConvertView, final ViewGroup aParent) {
        final IconTextView v;
        if (aConvertView == null) {
            final LayoutInflater inflater = (LayoutInflater) theContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = (IconTextView) inflater.inflate(R.layout.list_item_route, null);
            v.setFocusable(false);
            v.setFocusableInTouchMode(false);
            v.setClickable(false);
        } else {
            v = (IconTextView) aConvertView;
        }

        final String ourGroup = theGroups.get(aGroupPosition).intern();
        final Prediction ourPrediction = theChildren.get(ourGroup).get(aChildPosition);
        final Resources ourResources = TheApp.getContext().getResources();
        final String ourText;
        if (ourGroup.startsWith("Platform ")) {
            ourText = ShowPredictionsForStationActivity.formatPrediction(ourPrediction, true);
        } else {
            ourText = ShowPredictionsForStationActivity.formatPrediction(ourPrediction, false);
        }

        Drawable ourBadge;
        final String ourKey = ourPrediction.routeTag().intern();
        if (theBadges.containsKey(ourKey)) {
            ourBadge = theBadges.get(ourKey);
        } else {
            int theBadgeId;
            Log.d(LOG_NAME, "Trying to find BART badge for color: " + ourPrediction.routeTag().substring(1));
            try {
                final String ourResourceName = "drawable/bart_" + ourPrediction.routeTag().substring(1);
                theBadgeId = ourResources.getIdentifier(ourResourceName, null, "com.zippy.WayToGo");
            } catch (android.content.res.Resources.NotFoundException theEx) {
                theBadgeId = ourResources.getIdentifier("drawable/bart", null, "com.zippy.WayToGo");
            }
            if (theBadgeId == 0) {
                theBadgeId = ourResources.getIdentifier("drawable/bart", null, "com.zippy.WayToGo");
            }
            ourBadge = TheApp.getResDrawable(theBadgeId);
            theBadges.put(ourKey, ourBadge);
        }

        v.setBadgeDrawable(ourBadge);
        v.setText(ourText);
        return v;
    }

    void addEmptyPredictionGroup() {
        Log.d(LOG_NAME, "Adding something to indicate we've got no predictions!");
        theGroups.add(TheApp.getResString(R.string.text_no_predictions));
        notifyDataSetChanged();
    }
}
