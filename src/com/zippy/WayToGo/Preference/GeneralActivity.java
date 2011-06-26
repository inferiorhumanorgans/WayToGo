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
import com.zippy.WayToGo.R;

/**
 *
 * @author alex
 */
public class GeneralActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private final static String LOG_NAME = GeneralActivity.class.getCanonicalName();
    private SharedPreferences thePrefs;

    @Override
    protected void onCreate(final Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        thePrefs = (SharedPreferences) PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.layout.activity_prefs);
    }

    @Override
    protected void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences someSharedPreferences, String aKey) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
