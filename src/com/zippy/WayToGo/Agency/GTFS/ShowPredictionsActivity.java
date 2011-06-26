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
package com.zippy.WayToGo.Agency.GTFS;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.zippy.WayToGo.ListAdapter.PredictionAdapter;
import com.zippy.WayToGo.Agency.PredictionListener;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.Util.Direction;
import com.zippy.WayToGo.Util.Prediction;
import com.zippy.WayToGo.Util.PredictionGroup;
import com.zippy.WayToGo.Util.PredictionSummary;
import com.zippy.WayToGo.Util.Stop;
import java.util.ArrayList;

/**
 *
 * @author alex
 */
public class ShowPredictionsActivity extends GTFSActivity implements PredictionListener {

    private static final String LOG_NAME = ShowPredictionsActivity.class.getCanonicalName();
    private final ArrayList<Prediction> thePredictions = new ArrayList<Prediction>();
    private Stop theStop;
    private AsyncTask theFetcher;
    

    @Override
    public void onCreate(final Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        theListAdapter = new PredictionAdapter(this);
        theListView.setAdapter(theListAdapter);
        theIconView.setIconVisible(false);
        theOptionsMenuId = R.menu.generic_prediction_menu;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        final Intent intent = getIntent();
        theStop = theAgency().getStop(intent.getStringExtra("stopId"));
        theFetcher = theAgency().fetchPredictionsForStop(theStop, this);
        final String theText;
        if ((theStop.address() != null) && !theStop.address().equals("")) {
            theIconView.setText(theStop.name() + "\n" + theStop.address());
        } else {
            theIconView.setText(theStop.name());
        }

        thePredictions.clear();
    }

    @Override
    protected void onPause() {
        Log.d(LOG_NAME, "onPause");
        super.onPause();
        if (theFetcher != null) {
            theFetcher.cancel(true);
        }
    }

    public void populateListView() {
        theListAdapter.clear();
        final boolean includeLegit = true;
        final boolean wantAllRoutes = true;
        ArrayList<PredictionGroup> ourPredictionGroups = PredictionGroup.getPredictionGroups(thePredictions, includeLegit);

        if (wantAllRoutes && (ourPredictionGroups.size() > 1)) {
            final int theLogo = theAgency().getLogo();
            if (theLogo == 0) {
                theIconView.setBadgeText(theAgency().getShortName());
            } else {
                theIconView.setBadgeDrawable(theLogo);
            }
        }

        for (final PredictionGroup ourGroup : ourPredictionGroups) {
            final PredictionSummary ourSummary = new PredictionSummary(getDialogContext(), ourGroup);
            if ((ourPredictionGroups.size() == 1) && wantAllRoutes) {
                ourSummary.setFlags("legit");
                final Stop ourStop = theAgency().getStop(ourGroup.stopTag());
                final Direction ourDirection = theAgency().getDirectionFromTag(ourGroup.directionTag());
                theIconView.setText(ourStop.shortName() + "\n" + ourDirection.shortTitle());
                //theIconView.setText(theStop.first) + "\n" + theAgency.getTerseDirectionName(theDirection.first));
                theIconView.setBadgeText(ourGroup.routeTag());
                theIconView.invalidate();
            }

            theListAdapter.add(ourSummary);
        }
        if (theListAdapter.isEmpty()) {
            theListAdapter.add(new PredictionSummary(getDialogContext()));
        }
        theListAdapter.notifyDataSetChanged();
    }

    public synchronized void startPredictionFetch() {
        thePredictions.clear();
    }

    public synchronized void addPrediction(final Prediction aPrediction) {
        thePredictions.add(aPrediction);
    }

    public void finishedPullingPredictions(final boolean wasCancelled) {
        populateListView();
    }
}
