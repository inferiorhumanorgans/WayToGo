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
package com.zippy.WayToGo.Util;

import android.util.Log;
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.Comparator.PredictionGroupComparator;
import com.zippy.WayToGo.TheApp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author alex
 */
public final class PredictionGroup {

    final private static String LOG_NAME = PredictionGroup.class.getCanonicalName();
    final private Stop theStop;
    final private int[] theMinutes;
    final private String theGUID, theUID;
    private final String theRouteTag, theStopTag, theSubStopTag, theDirectionTag;

    PredictionGroup(final ArrayList<Prediction> somePredictions) {
        final Prediction theFirst = somePredictions.get(0);
        theGUID = theFirst.getGUID();
        theUID = theFirst.getUID();
        theRouteTag = theFirst.getRouteTag();
        theStopTag = theFirst.getStopTag();
        theSubStopTag = theFirst.getTheSubStopTag();
        theDirectionTag = theFirst.getDirectionTag();

        final BaseAgency ourAgency = theFirst.getTheAgency();
        theStop = ourAgency.getStop(theFirst.getStopTag());

        // Put all the minutes into an array for now.
        final int[] allTheMinutes = new int[somePredictions.size()];
        for (int i = 0; i < allTheMinutes.length; i++) {
            allTheMinutes[i] = somePredictions.get(i).getMinutes();
        }


        // Determine what the user selected for the max number of predictions to show
        final int hardLimit = TheApp.getMaxPredictionsPerStop();

        // Pick the smaller of the two
        final int numPredictions = Math.min(hardLimit, somePredictions.size());
        // Sort to be sure
        Arrays.sort(allTheMinutes);

        // Allocate new array and copy over at most the upper bound
        theMinutes = new int[numPredictions];
        System.arraycopy(allTheMinutes, 0, theMinutes, 0, numPredictions);
    }

    public final int[] getTheMinutes() {
        return theMinutes.clone();
    }

    public final Stop getTheStop() {
        return theStop;
    }

    public final String getTheGUID() {
        return theGUID;
    }

    public final String getTheUID() {
        return theUID;
    }

    public final String getTheDirectionTag() {
        return theDirectionTag;
    }

    public final String getTheRouteTag() {
        return theRouteTag;
    }

    public final String getTheStopTag() {
        return theStopTag;
    }

    public final String getTheSubStopTag() {
        return theSubStopTag;
    }

    public static ArrayList<PredictionGroup> getPredictionGroups(final ArrayList<Prediction> somePredictions) {
        return getPredictionGroups(somePredictions, true);
    }

    public static ArrayList<PredictionGroup> getPredictionGroups(final ArrayList<Prediction> somePredictions, final boolean includeLegit) {
        final ArrayList<PredictionGroup> ourPredictionGroups = new ArrayList<PredictionGroup>();

        // Put them into a hash first;
        final HashMap<String, ArrayList<Prediction>> predictionHash = new HashMap<String, ArrayList<Prediction>>();
        for (final Prediction ourPrediction : somePredictions) {
            if (ourPrediction.getTheFlags().equals("legit") && !includeLegit) {
                continue;
            }

            // Gross hack: group all so-called legit predictions together.
            final String ourKey;
            if (ourPrediction.getTheFlags().equals("legit")) {
                ourKey = "legit";
            } else {
                ourKey = ourPrediction.getGUID();
                //Log.d(LOG_NAME, "Our prediction is: " + ourPrediction.toString());
                //Log.d(LOG_NAME, "Our key is: " + ourKey);
            }

            if (!predictionHash.containsKey(ourKey)) {
                predictionHash.put(ourKey, new ArrayList<Prediction>());
            }
            predictionHash.get(ourKey).add(ourPrediction);
        }

        for (final ArrayList<Prediction> ourPredictions : predictionHash.values()) {
            ourPredictionGroups.add(new PredictionGroup(ourPredictions));
        }
        Collections.sort(ourPredictionGroups, PredictionGroupComparator.PREDICTION_GROUP_ORDER);
        return ourPredictionGroups;
    }
}
