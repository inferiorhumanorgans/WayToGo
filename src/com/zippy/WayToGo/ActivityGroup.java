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

import java.util.ArrayList;

import android.app.Activity;
import android.app.LocalActivityManager;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import junit.framework.Assert;

/**
 * The purpose of this Activity is to manage the activities in a tab.
 * Note: Child Activities can handle Key Presses before they are seen here.
 * @author Eric Harlow
 */
public class ActivityGroup extends android.app.ActivityGroup {

    private static String LOG_NAME = ActivityGroup.class.getCanonicalName();
    final protected ArrayList<String> mIdList = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * This is called when a child activity of this one calls its finish method.
     * This implementation calls {@link LocalActivityManager#destroyActivity} on the child activity
     * and starts the previous activity.
     * If the last child activity just called finish(),this activity (the parent),
     * calls finish to finish the entire group.
     */
    @Override
    public synchronized void finishFromChild(Activity child) {
        //Log.d(LOG_NAME, "finishFromChild(" + child + ") w/ list size: " + mIdList.size());
        //Log.d(LOG_NAME, "list is: " + mIdList.toString());
        final LocalActivityManager manager = getLocalActivityManager();
        int index = mIdList.size() - 1;

        if (index < 1) {
            finish();
            return;
        }

        destroy(mIdList.get(index));
        mIdList.remove(index);
        index--;
        String lastId = mIdList.get(index);

        /* Something randomly disappeared on me, dunno what. */
        Assert.assertEquals(false, manager == null);
        Assert.assertEquals(false, manager.getActivity(lastId) == null);

        final Intent lastIntent = manager.getActivity(lastId).getIntent();
        final Window newWindow = manager.startActivity(lastId, lastIntent);
        setContentView(newWindow.getDecorView());
        newWindow.getDecorView().requestFocus();
    }

    /**
     * Starts an Activity as a child Activity to this.
     * @param Id Unique identifier of the activity to be started.
     * @param intent The Intent describing the activity to be started.
     * @throws android.content.ActivityNotFoundException.
     */
    public synchronized void startChildActivity(String Id, Intent intent) {
        Log.d(LOG_NAME, "startChildActivity");
        LocalActivityManager manager = getLocalActivityManager();

        Window window = manager.startActivity(Id, intent);
        if (window != null) {
            mIdList.add(Id);
            setContentView(window.getDecorView());
        }
    }

    // http://stackoverflow.com/questions/2928101/android-using-linear-gradient-as-background-looks-banded
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    /**
     * The primary purpose is to prevent systems before android.os.Build.VERSION_CODES.ECLAIR
     * from calling their default KeyEvent.KEYCODE_BACK during onKeyDown.
     */
    @Override
    public synchronized boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Overrides the default implementation for KeyEvent.KEYCODE_BACK
     * so that all systems call onBackPressed().
     */
    @Override
    public synchronized boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * If a Child Activity handles KeyEvent.KEYCODE_BACK.
     * Simply override and add this method.
     */
    @Override
    public synchronized void onBackPressed() {
        int length = mIdList.size();
        if (length > 1) {
            Activity current = getLocalActivityManager().getActivity(mIdList.get(length - 1));
            current.finish();
        }
    }

    //http://stackoverflow.com/questions/1912947/android-start-user-defined-activity-on-search-button-pressed-handset
    //http://stackoverflow.com/questions/5461240/searchdialog-cannot-be-shown-within-activitygroup
    @Override
    public synchronized boolean onSearchRequested() {
        return true;
    }

    // http://stackoverflow.com/questions/3157406/android-activitygroup-menu-problem
    // http://stackoverflow.com/questions/80692/java-logger-that-automatically-determines-callers-class-name
    // http://www.exampledepot.com/egs/java.lang.reflect/Methods.html
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        return getLocalActivityManager().getCurrentActivity().onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Method[] methods = getLocalActivityManager().getCurrentActivity().getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].toString().endsWith("onCreateOptionsMenu(android.view.menu)")) {
                return getLocalActivityManager().getCurrentActivity().onCreateOptionsMenu(menu);
            }
        }
        return getParent().onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return getLocalActivityManager().getCurrentActivity().onMenuItemSelected(featureId, item);
    }

    /**
     * Christ on a fucking stick, I can't believe Google won't fix this.
     * And people wonder why the iPhone is so popular...
     * Also how badly does Java need proper mixins or MI?  Sheesh.
     * 
     * http://code.google.com/p/android/issues/detail?id=12359
     * http://code.google.com/p/android/issues/detail?id=879
     * @param id
     * @return
     */
    private synchronized boolean destroy(String id) {
        final LocalActivityManager activityManager = getLocalActivityManager();
        if (activityManager != null) {
            activityManager.destroyActivity(id, false);
            // http://code.google.com/p/android/issues/detail?id=12359
            // http://www.netmite.com/android/mydroid/frameworks/base/core/java/android/app/LocalActivityManager.java
            try {
                final Field mActivitiesField = LocalActivityManager.class.getDeclaredField("mActivities");
                if (mActivitiesField != null) {
                    mActivitiesField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> mActivities = (Map<String, Object>) mActivitiesField.get(activityManager);
                    if (mActivities != null) {
                        mActivities.remove(id);
                    }
                    final Field mActivityArrayField = LocalActivityManager.class.getDeclaredField("mActivityArray");
                    if (mActivityArrayField != null) {
                        mActivityArrayField.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        final ArrayList<Object> mActivityArray = (ArrayList<Object>) mActivityArrayField.get(activityManager);
                        if (mActivityArray != null) {
                            for (Object record : mActivityArray) {
                                final Field idField = record.getClass().getDeclaredField("id");
                                if (idField != null) {
                                    idField.setAccessible(true);
                                    final String _id = (String) idField.get(record);
                                    if (id.equals(_id)) {
                                        mActivityArray.remove(record);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
}
