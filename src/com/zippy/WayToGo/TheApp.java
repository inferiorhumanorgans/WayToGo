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
package com.zippy.WayToGo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import com.zippy.WayToGo.Agency.BaseAgency;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acra.*;
import org.acra.annotation.*;

/**
 *
 * @author alex
 */
@ReportsCrashes(formKey = "dFk2MXZtbjlLTzhobDhXaktjWTh1V2c6MQ",
mode = ReportingInteractionMode.NOTIFICATION,
resNotifTickerText = R.string.acra_crash_notif_ticker_text,
resNotifTitle = R.string.acra_crash_notif_title,
resNotifText = R.string.acra_crash_notif_text,
resDialogText = R.string.acra_dialog_text,
resDialogCommentPrompt = R.string.acra_dialog_comment_prompt)
public final class TheApp extends Application {

    private static final String LOG_NAME = TheApp.class.getCanonicalName();
    private static TheApp instance;
    private static String theAppName = null;
    private static final double KM_FACTOR = 1 / 1000.00;
    private static final String KM_UNIT = "km";
    private static final double MI_FACTOR = 0.000621371192;
    private static final String MI_UNIT = "mi";
    private static final double FT_FACTOR = 3.2808399;
    private static final String FT_UNIT = "ft";
    private static final double M_FACTOR = 1;
    private static final String M_UNIT = "m";
    private static final int INT_PRECISION = 0;
    private static final int FLOAT_PRECISION = 2;
    private static Resources res;
    public static final Intent thePrefIntent = new Intent("w2g.action.PREFERENCES");
    /**
     * Change this to change the number of tabs allowed in the main screen.
     */
    public static final int NUMBER_OF_TABS = 7;
    /**
     * A string representing a regular expression that will match the preferences
     * key for a tab's contents.
     */
    public static final String TAB_PREF_KEY_REGEX = String.format("tab([1-%d])Contents", TheApp.NUMBER_OF_TABS);
    /**
     * A hash representing all of the agencies we know about.
     */
    public static final HashMap<String, BaseAgency> theAgencies = new HashMap<String, BaseAgency>(NUMBER_OF_TABS);
    /**
     * How long do we wait for the GPS to find a fix before giving up.  In seconds.
     */
    public final static int MAX_GPS_WAIT = 10;

    public static class StringMap extends HashMap<String, String> {
    }
    private static SharedPreferences prefs;

    @Override
    public void onCreate() {
        ACRA.init(this);
        super.onCreate();
        TheApp.res = getResources();
        instance = this;
        prefs = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(this);
        //Debug.startMethodTracing("WayToGo");


        // Load all of the agencies specified in our preferences into the global
        // hash.
        for (int i = 0; i < NUMBER_OF_TABS; i++) {
            final String tabValue = getPrefs().getString(TheApp.getResString(getTabKeyId(i + 1)), TheApp.getResString(getTabDefaultId(i + 1)));
            addAgency(tabValue);
        }
    }

    public static int getTabKeyId(final int aPosition) {
        if (aPosition > NUMBER_OF_TABS || aPosition < 1) {
            final String fmt = "%d tabs configured, but you requested tab %d";
            throw new UnsupportedOperationException(String.format(fmt, NUMBER_OF_TABS, aPosition));
        }
        final String keyFormat = "string/pref_tab%d_key";
        final String keyIdentifier = String.format(keyFormat, aPosition);
        return res.getIdentifier(keyIdentifier, null, "com.zippy.WayToGo");
    }

    public static int getTabDefaultId(final int aPosition) {
        if (aPosition > NUMBER_OF_TABS || aPosition < 1) {
            final String fmt = "%d tabs configured, but you requested tab %d";
            throw new UnsupportedOperationException(String.format(fmt, NUMBER_OF_TABS, aPosition));
        }
        final String keyFormat = "string/pref_tab%d_default";
        final String keyIdentifier = String.format(keyFormat, aPosition);
        return res.getIdentifier(keyIdentifier, null, "com.zippy.WayToGo");
    }

    private void addAgency(final String anAgency) {
        BaseAgency ourAgency = null;
        if (!anAgency.startsWith("com.zippy.WayToGo.Agency.")) {
            return;
        }
        try {
            ourAgency = (BaseAgency) Class.forName(anAgency).newInstance();
        } catch (ClassNotFoundException ex) {
            Log.d(LOG_NAME, "Probably not an agency then");
            return;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(TheApp.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (InstantiationException ex) {
            Logger.getLogger(TheApp.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        if (!(ourAgency == null) && !theAgencies.containsKey(anAgency)) {
            ourAgency.init(this);
            theAgencies.put(anAgency, ourAgency);
        }
    }

    /**
     *
     * @return  A global shared prefs object
     */
    public static SharedPreferences getPrefs() {
        return prefs;
    }

    /**
     *
     * @return Global context object
     */
    public static Context getContext() {
        return instance;
    }

    /**
     *
     * @param id A resource id
     * @return A string value from that resource ID
     */
    public static String getResString(final int id) {
        return res.getText(id).toString();
    }

    /**
     *
     * @param id A resource id
     * @return
     */
    public static boolean getResBool(final int id) {
        return res.getBoolean(id);
    }

    /**
     *
     * @param id A resource id
     * @return
     */
    public static int getResInt(final int id) {
        return res.getInteger(id);
    }

    /**
     *
     * @param id A resource id
     * @return
     */
    public static Drawable getResDrawable(final int id) {
        return res.getDrawable(id);
    }

    /**
     *
     * @return The application title
     */
    public static String getAppTitle() {
        if (theAppName == null) {
            theAppName = getResString(R.string.app_name);
        }
        return theAppName;
    }

    /**
     *
     * This will use meters or feet for small distances, and kilometers/miles for longer ones.
     * @param someMeters Distance in meters
     * @return Distance in desire format (metric/std) as a string with a unit attached
     */
    public static String formatDistance(final double someMeters) {
        boolean isMetric = prefs.getBoolean(TheApp.getResString(R.string.pref_metric_units_key), TheApp.getResBool(R.bool.pref_metric_units_default));
        String theUnit;
        double theFactor;
        int thePrecision = FLOAT_PRECISION;
        if (isMetric) {
            theFactor = KM_FACTOR;
            theUnit = KM_UNIT;

            // Less than half a km, we use meters
            if ((someMeters * theFactor) < 0.5) {
                theFactor = M_FACTOR;
                theUnit = M_UNIT;
                thePrecision = INT_PRECISION;
            }
        } else {
            theFactor = MI_FACTOR;
            theUnit = MI_UNIT;

            // Less than ~1000ft we use feet
            if ((someMeters * theFactor) < 0.18) {
                theFactor = FT_FACTOR;
                theUnit = FT_UNIT;
                thePrecision = INT_PRECISION;
            }
        }
        String formatString = "%." + thePrecision + "f %s";
        return String.format(formatString, someMeters * theFactor, theUnit);

    }

    public static String formatStop(final String aStopName, final double someDistanceFromUs) {
        boolean useDistance = prefs.getBoolean(TheApp.getResString(R.string.pref_show_distance_key), TheApp.getResBool(R.bool.pref_show_distance_default));
        if (!useDistance) {
            return aStopName;
        }
        String formatString = "%s\n%s";
        return String.format(formatString, aStopName, formatDistance(someDistanceFromUs));
    }

    /**
     *
     * @return The maximum number of predictions to show per prediction summary.
     */
    public static int getMaxPredictionsPerStop() {
        final String maxPredictionsString = TheApp.getPrefs().getString(TheApp.getResString(R.string.pref_predictions_per_stop_key), TheApp.getResString(R.string.pref_predictions_per_stop_default));
        int maxPredictions;
        if (maxPredictionsString.equals("All")) {
            maxPredictions = Integer.MAX_VALUE;
        } else {
            maxPredictions = Integer.parseInt(maxPredictionsString);
        }
        return maxPredictions;
    }

    /**
     *
     * @return The milliseconds between reloading predictions that the user has specified.
     */
    public static int getPredictionRefreshDelay() {
        final String predictionDelayString = TheApp.getPrefs().getString(TheApp.getResString(R.string.pref_auto_refresh_delay_key), TheApp.getResString(R.string.pref_auto_refresh_delay_default));
        return Integer.parseInt(predictionDelayString) * 1000;
    }

    /**
     *
     * @param someDensityIndependentPixels Raw pixel value to be scaled
     * @return Actual pixels appropriately scaled.
     */
    public static int getRealPixels(final int someDensityIndependentPixels) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, someDensityIndependentPixels, TheApp.instance.getResources().getDisplayMetrics());
    }

    /**
     * Converts a floating point coordinate into a scaled integer coordinate
     * @param aDoubleValue The floating point version of the coordinate
     * @return The integer (E6) version of the coordinate
     */
    public static int toE6(double aDoubleValue) {
        return (int) (aDoubleValue * 1E6);
    }

    /**
     * This is used for CopyDBTask, without a FQ path it wouldn't know where to
     * write its output.
     *
     * @param aName
     * @return The fully qualified path to the db file we want.
     */
    public static String getQualifiedDatabasePathName(final String aName) {
        return instance.getDatabasePath(aName).getPath();
        //return instance.getExternalFilesDir("databases") + File.separator + name;
    }

    /**
     * This is for Android < 2.3, because SQLiteOpenHelper can only "handle"
     * a FQ path after 2.3 (2.3.3? ugh, I hope not) or so.
     *
     * @param aName
     * @return
     */
    public static String getDatabaseFileName(final String aName) {
        // Sadly pre-2.2 doesn't support absolute path names for the DB, meaning
        // we're restricted to internal memory only. More half-baked Google shit.
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO) {
            return aName;
        } else {
            return getQualifiedDatabasePathName(aName);
        }
    }
}
