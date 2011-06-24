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
package com.zippy.WayToGo.Bookmarks;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.Agency.PredictionListener;
import com.zippy.WayToGo.Comparator.PredictionGroupComparator;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.TheApp;
import com.zippy.WayToGo.Util.Prediction;
import com.zippy.WayToGo.Util.PredictionGroup;
import com.zippy.WayToGo.Util.PredictionSummary;
import com.zippy.WayToGo.Widget.IconTextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.osmdroid.util.GeoPoint;

/**
 *
 * @author alex
 */
public class BookmarkAdapter extends ArrayAdapter<Bookmark> implements PredictionListener {

    private ArrayList<Bookmark> theItems;
    private Context theContext = null;
    private static final String LOG_NAME = BookmarkAdapter.class.getCanonicalName();
    private Location theLocation;
    private ArrayList<Prediction> thePredictions = new ArrayList<Prediction>();
    private HashMap<String, ArrayList<PredictionGroup>> thePredictionGroups = new HashMap<String, ArrayList<PredictionGroup>>();
    private int pendingPredictions = 0;
    private final Object lock1 = new Object();

    public BookmarkAdapter(Context aContext) {
        this(aContext, new ArrayList<Bookmark>());
    }

    public BookmarkAdapter(Context aContext, ArrayList<Bookmark> someItems) {
        super(aContext, R.layout.list_item_route, someItems);
        theItems = someItems;
        theContext = aContext;
    }

    public Location getTheLocation() {
        return theLocation;
    }

    public void setTheLocation(Location theLocation) {
        this.theLocation = theLocation;
        notifyDataSetChanged();
    }

    public ArrayList<Bookmark> getArray() {
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
            v.setTheAgency(null);
        } else {
            v = (IconTextView) convertView;
        }
        Bookmark theBookmark = theItems.get(position);

        BaseAgency ourAgency = theBookmark.getTheStop().getTheAgency();

        int ourDrawable = -1;
        if (ourAgency != null) {
            ourDrawable = ourAgency.getLogo();
        }

        if (ourDrawable != -1) {
            v.setBadgeDrawable(ourDrawable);
        } else {
            v.setBadgeText(ourAgency.getShortName());
        }

        String theDistanceString = "";
        if (theLocation != null) {
            GeoPoint thePoint = theBookmark.getTheStop().getThePoint();
            double lat = thePoint.getLatitudeE6() / 1E6;
            double lng = thePoint.getLongitudeE6() / 1E6;
            float[] results = new float[1];
            Location.distanceBetween(lat, lng, theLocation.getLatitude(), theLocation.getLongitude(), results);
            theDistanceString = TheApp.formatDistance(results[0]);
        }

        final StringBuilder thePredictionString = new StringBuilder();
        //Log.d(LOG_NAME, "Refreshing: " + theBookmark.getTheStop().getGUID());
        if (thePredictionGroups.containsKey(theBookmark.getTheStop().getGUID())) {
            //Log.d(LOG_NAME, "FOUND SOMETHING");
            synchronized (lock1) {
                final ArrayList<PredictionGroup> ourGroups = thePredictionGroups.get(theBookmark.getTheStop().getGUID());
                if (ourGroups.isEmpty()) {
                    return v;
                }
                final PredictionGroup aPredictionGroup = ourGroups.get(0);
                final PredictionSummary summary = new PredictionSummary(theContext, aPredictionGroup);
                thePredictionString.append(summary.toText().toString());
            }
        } else {
            //Log.d(LOG_NAME, "Found nothing");
        }

        final StringBuilder theText = new StringBuilder();
        theText.append(theBookmark.getTheStop().getTheName());
        if (thePredictionString.toString().length() != 0) {
            theText.append("\n");
            theText.append(thePredictionString.toString());
        }
        if (theDistanceString.length() != 0) {
            theText.append("\n");
            theText.append(theDistanceString);
        }

        v.setText(theText.toString());

        return v;
    }

    public synchronized void startPredictionFetch() {
        if (pendingPredictions == 0) {
            thePredictions.clear();
            for (final ArrayList<PredictionGroup> someGroups : thePredictionGroups.values()) {
                someGroups.clear();
            }
        }
        pendingPredictions++;
    }

    public synchronized void addPrediction(Prediction aPrediction) {
        thePredictions.add(aPrediction);
    }

    public synchronized void finishedPullingPredictions(boolean wasCancelled) {
        if (pendingPredictions > 0) {
            pendingPredictions--;
        }
        if (wasCancelled) {
            Log.d(LOG_NAME, "Was cancelled???");
            return;
        }

        if (pendingPredictions != 0) {
            return;
        }

        synchronized (lock1) {
            final ArrayList<PredictionGroup> rawGroups = PredictionGroup.getPredictionGroups(thePredictions);
            for (final PredictionGroup aGroup : rawGroups) {
                if (!thePredictionGroups.containsKey(aGroup.getTheUID())) {
                    thePredictionGroups.put(aGroup.getTheUID(), new ArrayList<PredictionGroup>());
                }
                thePredictionGroups.get(aGroup.getTheUID()).add(aGroup);
            }
            for (final ArrayList<PredictionGroup> somePredictionGroups : thePredictionGroups.values()) {
                Collections.sort(somePredictionGroups, PredictionGroupComparator.PREDICTION_GROUP_ORDER);
            }
            notifyDataSetChanged();
        }
        ((Activity)theContext).getParent().setProgressBarIndeterminateVisibility(false);
    }

    public synchronized void clearPredictions() {
        synchronized(lock1) {
            thePredictions.clear();
            pendingPredictions = 0;
        }
    }

    protected synchronized void rotate() {
        synchronized (lock1) {
            for (final ArrayList<PredictionGroup> aList : thePredictionGroups.values()) {
                Collections.rotate(aList, -1);
            }
            notifyDataSetChanged();
        }
    }
}
