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
package com.zippy.WayToGo.Preference;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import com.zippy.WayToGo.R;
import com.zippy.WayToGo.TheApp;

/**
 *
 * @author alex
 */
public class PredictionActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private final static String LOG_NAME = PreferenceActivity.class.getSimpleName();
    private SharedPreferences prefs;
    private PreferenceScreen theScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.layout.activity_prefs_predictions);
    }

    @Override
    protected void onPause() {
        super.onPause();

        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (theScreen == null) {
            theScreen = getPreferenceScreen();
        }
        updatePredictionsPerStop();
        updateReloadIntervalSummary();
        // Set up a listener whenever a key changes
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences someSharedPreferences, String aKey) {
        
        if (aKey.equals(TheApp.getResString(R.string.pref_predictions_per_stop_key))) {
            updatePredictionsPerStop();
        } else if (aKey.equals(TheApp.getResString(R.string.pref_auto_refresh_delay_key))) {
            updateReloadIntervalSummary();
        }
    }

    private void updatePredictionsPerStop() {
        final String theKey = TheApp.getResString(R.string.pref_predictions_per_stop_key);
        final String theDefault = TheApp.getResString(R.string.pref_predictions_per_stop_default);
        final String theCount = prefs.getString(theKey, theDefault);
        final String theSummary = TheApp.getResString(R.string.pref_predictions_per_stop_summary) + "  Currently: " + theCount;
        theScreen.findPreference(theKey).setSummary(theSummary);
    }

    private void updateReloadIntervalSummary() {
        final String theKey = TheApp.getResString(R.string.pref_auto_refresh_delay_key);
        final String theDefault = TheApp.getResString(R.string.pref_auto_refresh_delay_default);
        final String theCount = prefs.getString(theKey, theDefault);
        final String theSummary = TheApp.getResString(R.string.pref_auto_refresh_delay_summary) + "  Currently: " + theCount + " seconds";
        theScreen.findPreference(theKey).setSummary(theSummary);
    }
}
