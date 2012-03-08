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
package com.inferiorhumanorgans.WayToGo.Preference;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import com.inferiorhumanorgans.WayToGo.R;
import com.inferiorhumanorgans.WayToGo.TheApp;

/**
 * This is the preferences screen where you can configure which agencies or
 * other goodies go on which tabs.
 *
 * @author alex
 */
public class TabsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private final static String LOG_NAME = TabsActivity.class.getCanonicalName();
    private PreferenceScreen theScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.activity_prefs_tabs);
    }

    @Override
    protected void onPause() {
        super.onPause();

        TheApp.getPrefs().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (theScreen == null) {
            theScreen = getPreferenceScreen();
        }

        for (int i=1; i <=7; i++) {
            Log.d(LOG_NAME, "Updating " + i);
            updateTabSummary(String.valueOf(i));
        }
        // Set up a listener whenever a key changes
        TheApp.getPrefs().registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences someSharedPreferences, String aKey) {
        if (aKey.matches(TheApp.TAB_PREF_KEY_REGEX)) {
            final String theNumber = aKey.replaceFirst(TheApp.TAB_PREF_KEY_REGEX, "$1");
            updateTabSummary(theNumber);
        }
    }

    private void updateTabSummary(final String aNumber) {
        final String theKey = "tab" + aNumber + "Contents";
        final Resources theResources = getResources();
        final int summaryId = theResources.getIdentifier("string/pref_tab" + aNumber + "_summary", null, "com.inferiorhumanorgans.WayToGo");
        final int defaultId = theResources.getIdentifier("string/pref_tab" + aNumber + "_default", null, "com.inferiorhumanorgans.WayToGo");
        final String theDefault = TheApp.getResString(defaultId);
        final String theType = TheApp.getPrefs().getString(theKey, theDefault);
        final String theSummary = TheApp.getResString(summaryId) + "\nCurrently: " + theType;
        theScreen.findPreference(theKey).setSummary(theSummary);
    }
}
