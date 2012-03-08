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
package com.inferiorhumanorgans.WayToGo;

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
public class BaseActivityGroup extends android.app.ActivityGroup {

    private static String LOG_NAME = BaseActivityGroup.class.getCanonicalName();
    protected final ArrayList<String> mIdList = new ArrayList<String>();

    @Override
    public void onCreate(final Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
    }

    /**
     * This is called when a child activity of this one calls its finish method.
     * This implementation calls {@link LocalActivityManager#destroyActivity} on the child activity
     * and starts the previous activity.
     * If the last child activity just called finish(),this activity (the parent),
     * calls finish to finish the entire group.
     */
    @Override
    public synchronized void finishFromChild(final Activity aChild) {
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
        Assert.assertNotNull(manager);
        Assert.assertNotNull(manager.getActivity(lastId));

        final Intent lastIntent = manager.getActivity(lastId).getIntent();
        final Window newWindow = manager.startActivity(lastId, lastIntent);
        setContentView(newWindow.getDecorView());
        newWindow.getDecorView().requestFocus();
    }

    /**
     * Starts an Activity as a child Activity to this.
     * @param anId Unique identifier of the activity to be started.
     * @param anIntent The Intent describing the activity to be started.
     * @throws android.content.ActivityNotFoundException.
     */
    public synchronized void startChildActivity(final String anId, final Intent anIntent) {
        Log.d(LOG_NAME, "startChildActivity");
        LocalActivityManager manager = getLocalActivityManager();

        Window window = manager.startActivity(anId, anIntent);
        if (window != null) {
            mIdList.add(anId);
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
    public synchronized boolean onKeyDown(final int aKeyCode, final KeyEvent anEvent) {
        if (aKeyCode == KeyEvent.KEYCODE_BACK) {
            //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
            return true;
        }
        return super.onKeyDown(aKeyCode, anEvent);
    }

    /**
     * Overrides the default implementation for KeyEvent.KEYCODE_BACK
     * so that all systems call onBackPressed().
     */
    @Override
    public synchronized boolean onKeyUp(final int aKeyCode, final KeyEvent anEvent) {
        if (aKeyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyUp(aKeyCode, anEvent);
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
    public boolean onPrepareOptionsMenu(final Menu aMenu) {
        aMenu.clear();
        return getLocalActivityManager().getCurrentActivity().onCreateOptionsMenu(aMenu);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu aMenu) {
        Method[] methods = getLocalActivityManager().getCurrentActivity().getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].toString().endsWith("onCreateOptionsMenu(android.view.menu)")) {
                return getLocalActivityManager().getCurrentActivity().onCreateOptionsMenu(aMenu);
            }
        }
        return getParent().onCreateOptionsMenu(aMenu);
    }

    @Override
    public boolean onMenuItemSelected(final int aFeatureId, final MenuItem anItem) {
        return getLocalActivityManager().getCurrentActivity().onMenuItemSelected(aFeatureId, anItem);
    }

    /**
     * Christ on a fucking stick, I can't believe Google won't fix this.
     * And people wonder why the iPhone is so popular...
     * Also how badly does Java need proper mixins or MI?  Sheesh.
     * 
     * http://code.google.com/p/android/issues/detail?id=12359
     * http://code.google.com/p/android/issues/detail?id=879
     * @param anId
     * @return
     */
    private synchronized boolean destroy(final String anId) {
        final LocalActivityManager activityManager = getLocalActivityManager();
        if (activityManager != null) {
            activityManager.destroyActivity(anId, false);
            // http://code.google.com/p/android/issues/detail?id=12359
            // http://www.netmite.com/android/mydroid/frameworks/base/core/java/android/app/LocalActivityManager.java
            try {
                final Field mActivitiesField = LocalActivityManager.class.getDeclaredField("mActivities");
                if (mActivitiesField != null) {
                    mActivitiesField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> mActivities = (Map<String, Object>) mActivitiesField.get(activityManager);
                    if (mActivities != null) {
                        mActivities.remove(anId);
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
                                    if (anId.equals(_id)) {
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
