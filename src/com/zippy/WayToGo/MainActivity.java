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
// http://stackoverflow.com/questions/2315445/how-to-quickly-determine-if-a-method-is-overridden-in-java
// http://jtoee.blogspot.com/2008/05/better-way-to-iterate-java-maps.html
package com.zippy.WayToGo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabWidget;
import com.zippy.WayToGo.Agency.BaseAgency;
import com.zippy.WayToGo.Bookmarks.BookmarksDataHelper;
import com.zippy.WayToGo.Util.CopyDBListener;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends TabActivity implements CopyDBListener, OnSharedPreferenceChangeListener {

    private static final String LOG_NAME = MainActivity.class.getCanonicalName();
    private final ArrayList<String> theTabTags = new ArrayList<String>(TheApp.NUMBER_OF_TABS);
    private final int MINIMUM_TAB_WIDTH = TheApp.getRealPixels(70);
    private TabHost mTabHost;
    /**
     * If set to true, we'll reset all of our tabs when the activity is resumed.
     */
    private boolean tabConfigChanged = false;
    private int theFirstFavoriteIndex = -1;
    private int theFirstAgencyIndex = -1;
    private final ArrayList<String> pendingAgencies = new ArrayList<String>(TheApp.theAgencies.size());
    private ProgressDialog theProgressDialog;

    @Override
    public void onCreate(Bundle aBundle) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        super.onCreate(aBundle);
        setContentView(R.layout.activity_main);
        mTabHost = getTabHost();
        TheApp.getPrefs().registerOnSharedPreferenceChangeListener(this);
    }

    public void resetTabs() {
        // We'll crash *occasionally* if we don't set the current tab to 0. LAME
        // http://code.google.com/p/android/issues/detail?id=2772
        mTabHost.setCurrentTab(0);
        mTabHost.clearAllTabs();
        theTabTags.clear();
        theFirstFavoriteIndex = -1;

        for (int i = 0; i < TheApp.NUMBER_OF_TABS; i++) {
            final int keyId = TheApp.getTabKeyId(i + 1);
            final int defaultId = TheApp.getTabDefaultId(i + 1);
            final String theTabType = TheApp.getPrefs().getString(TheApp.getResString(keyId), TheApp.getResString(defaultId));
            addTab(theTabType);
        }

        tabConfigChanged = false;

        TabWidget widget = mTabHost.getTabWidget();
        for (int i = 0; i < widget.getChildCount(); ++i) {
            View v = widget.getChildAt(i);
            v.setMinimumWidth(MINIMUM_TAB_WIDTH);
        }

        int theNumberOfBookmarks = 0;
        // Should probably make this properly configurable.
        if (theFirstFavoriteIndex != -1) {
            BookmarksDataHelper bookmarkDB = new BookmarksDataHelper(this);
            theNumberOfBookmarks = bookmarkDB.getNumberOfBookmarks();
            bookmarkDB.cleanUp();
        }

        // If we have a favorites tab and bookmarks, show that by default
        // Otherwise show the first agency tab
        // Otherwise just show the last tab
        if ((theFirstFavoriteIndex != -1) && (theNumberOfBookmarks > 0)) {
            mTabHost.setCurrentTab(theFirstFavoriteIndex);
        } else if (theFirstAgencyIndex != -1) {
            mTabHost.setCurrentTab(theFirstAgencyIndex);
        }
    }

    private void addTab(final String aTabType) {
        Log.d(LOG_NAME, "addTab(" + aTabType + ")");
        final TabHost.TabSpec spec;
        final Intent intent;
        boolean isNonAgency = false;

        if (aTabType.equals("None")) {
            return;
        }

        theTabTags.add(aTabType);
        if (TheApp.theAgencies.containsKey(aTabType)) {
            final BaseAgency ourAgency = TheApp.theAgencies.get(aTabType);
            Class intentClass = null;
            try {
                final Class ourAgencyClass = Class.forName(aTabType);
                Class[] nestedClasses = ourAgencyClass.getDeclaredClasses();
                for (int i = 0; i < nestedClasses.length; i++) {
                    if (nestedClasses[i].getSimpleName().equals("ActivityGroup")) {
                        intentClass = nestedClasses[i];
                        break;
                    }
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }

            if (intentClass == null) {
                Log.e(LOG_NAME, "Couldn't find an ActivityGroup for " + aTabType);
                return;
            }

            intent = new Intent(this, intentClass);
            spec = mTabHost.newTabSpec(aTabType);
            spec.setContent(intent);
            spec.setIndicator(ourAgency.getShortName(), TheApp.getResDrawable(ourAgency.getLogo()));
            mTabHost.addTab(spec);
        } else if (aTabType.equals("Map")) {
            addMapTab();
            isNonAgency = true;
        } else if (aTabType.equals("Favorites")) {
            if (theFirstFavoriteIndex == -1) {
                theFirstFavoriteIndex = theTabTags.size() - 1;
            }
            isNonAgency = true;
            addBookmarkTab();
        }
        if (!isNonAgency) {
            if (theFirstAgencyIndex == -1) {
                theFirstAgencyIndex = theTabTags.size() - 1;
            }
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        if (mTabHost != null && mTabHost.getTabContentView() != null) {
            mTabHost.getTabContentView().requestFocus();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mTabHost.setCurrentTabByTag(state.getString("currentTab"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentTab", mTabHost.getCurrentTabTag());
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Don't unregister the listener here because we will get paused when hte
        // user ends up in the prefs screen.  And we really do want to know what
        // they do there.  Come to think of it, maybe we should register the
        // listener in onPause and unregister on onResume, because we only care
        // about changes made behind our back?
        //TheApp.getPrefs().unregisterOnSharedPreferenceChangeListener(this);
        stopService(new Intent(this, com.zippy.WayToGo.GPS.TheGPSService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TheApp.getPrefs().unregisterOnSharedPreferenceChangeListener(this);
        stopService(new Intent(this, com.zippy.WayToGo.GPS.TheGPSService.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tabConfigChanged) {
            Log.d(LOG_NAME, "Resetting the tabs in the onresume event");
            resetTabs();
        }
        mTabHost.getTabContentView().requestFocus();
        startService(new Intent(this, com.zippy.WayToGo.GPS.TheGPSService.class));

        pendingAgencies.clear();
        for (final Entry<String, BaseAgency> anEntry : TheApp.theAgencies.entrySet()) {
            if (anEntry.getValue().getStatus() == BaseAgency.AGENCY_DB_MISSING) {
                pendingAgencies.add(anEntry.getKey());
            }
        }
        if (!pendingAgencies.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(TheApp.getResString(R.string.text_1st_run)).setCancelable(false).setPositiveButton(TheApp.getResString(R.string.text_okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    extractDatabases();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            resetTabs();
        }
    }

    private void extractDatabases() {
        if (pendingAgencies.isEmpty()) {
            return;
        }
        TheApp.theAgencies.get(pendingAgencies.get(0)).copyDatabase(this);
    }

    public void copyingStarted() {
        final BaseAgency ourAgency = TheApp.theAgencies.get(pendingAgencies.get(0));
        if (theProgressDialog == null) {
            theProgressDialog = new ProgressDialog(this);
            theProgressDialog.setCancelable(true);
            theProgressDialog.setIndeterminate(true);
        }

        theProgressDialog.setMessage(ourAgency.getShortName());
        theProgressDialog.setTitle(TheApp.getResString(R.string.loading_database));
        theProgressDialog.show();
    }

    public void copyingFinished() {
        pendingAgencies.remove(0);
        if (pendingAgencies.isEmpty()) {
            if (theProgressDialog != null) {
                theProgressDialog.dismiss();
                theProgressDialog = null;
            }
            resetTabs();
            return;
        }
        TheApp.theAgencies.get(pendingAgencies.get(0)).copyDatabase(this);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu aMenu) {
        Log.d(LOG_NAME, "onCreateOptionsMenu called");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, aMenu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                this.startActivity(TheApp.thePrefIntent);
                return true;
            default:
                return false;
        }
    }

    public Activity getActivityForTabTag(final String aTabTag) {
        mTabHost.setCurrentTabByTag(aTabTag);
        return getLocalActivityManager().getActivity(aTabTag);
    }

    /**
     * Creates a new bookmark/favorites tab
     */
    private void addBookmarkTab() {
        TabHost.TabSpec spec;
        Intent intent;
        String tabName = "Favorites";
        intent = new Intent().setClass(this, com.zippy.WayToGo.Bookmarks.BookmarkActivity.class);
        spec = mTabHost.newTabSpec(tabName);
        spec.setContent(intent);
        spec.setIndicator(tabName, TheApp.getResDrawable(R.drawable.bookmark));
        mTabHost.addTab(spec);
    }

    /**
     * Adds a new map view tab
     */
    private void addMapTab() {
        TabHost.TabSpec spec;
        Intent intent;
        String tabName = "Map";
        intent = new Intent().setClass(this, com.zippy.WayToGo.MapView.AllStopsActivity.class);
        spec = mTabHost.newTabSpec(tabName);
        spec.setContent(intent);
        spec.setIndicator(tabName, TheApp.getResDrawable(android.R.drawable.ic_dialog_map));
        mTabHost.addTab(spec);
    }

    /**
     * If the tab configuration changes, make sure we reset the view
     * @param somePrefs
     * @param aKey
     */
    public void onSharedPreferenceChanged(SharedPreferences somePrefs, String aKey) {
        if (aKey.matches(TheApp.TAB_PREF_KEY_REGEX)) {
            String theNumber = aKey.replaceFirst(TheApp.TAB_PREF_KEY_REGEX, "$1");
            int theIndex = Integer.parseInt(theNumber) - 1;
            final String theTabName;
            if (theIndex >= theTabTags.size()) {
                theTabName = "";
            } else {
                theTabName = theTabTags.get(theIndex);
            }
            String theCurrentPref = somePrefs.getString(aKey, "");
            if (!theTabName.equals(theCurrentPref)) {
                Log.d(LOG_NAME, "Pref changed(" + aKey + ") From: " + theTabName + " To: " + theCurrentPref);
                tabConfigChanged = true;
            }
        }
    }
}
