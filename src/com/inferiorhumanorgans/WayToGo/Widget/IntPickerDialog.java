/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inferiorhumanorgans.WayToGo.Widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.quietlycoding.android.picker.NumberPicker;
import com.quietlycoding.android.picker.NumberPicker.OnChangedListener;
import com.inferiorhumanorgans.WayToGo.R;
import com.inferiorhumanorgans.WayToGo.TheApp;

/**
 * A dialog that prompts the user for the time of day using a {@link TimePicker}.
 *
 * <p>See the <a href="{@docRoot}resources/tutorials/views/hello-timepicker.html">Time Picker
 * tutorial</a>.</p>
 */
public class IntPickerDialog extends AlertDialog implements OnClickListener, OnChangedListener {

    private static final String VALUE = "value";
    private static final String MIN_VALUE = "min_value";
    private static final String MAX_VALUE = "max_value";


    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    public interface OnNumberSetListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The hour that was set.
         * @param minute The minute that was set.
         */
        void onNumberSet(NumberPicker aView, int aValue);
    }
    private final NumberPicker mNumPicker;
    private final OnNumberSetListener mCallback;

    /**
     * @param context Parent.
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    public IntPickerDialog(Context context, OnNumberSetListener callBack, int aMin, int aMax, int aCurrent) {
        this(context, R.style.AlertDialog, callBack, aMin, aMax, aCurrent);
    }

    /**
     * @param context Parent.
     * @param theme the theme to apply to this dialog
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    public IntPickerDialog(Context context, int theme, OnNumberSetListener callBack, int aMin, int aMax, int aCurrent) {
        super(context, theme);
        mCallback = callBack;

        setTitle(TheApp.getResString(R.string.widget_title_intpicker));

        setButton(context.getText(R.string.text_set), this);
        setButton2(context.getText(android.R.string.cancel), (OnClickListener) null);
        setIcon(R.drawable.ic_menu_refresh);

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.widget_integer_picker_dialog, null);
        setView(view);
        mNumPicker = (NumberPicker) view.findViewById(R.id.num_picker);

        // initialize state
        mNumPicker.setRange(aMin, aMax);
        mNumPicker.setCurrent(aCurrent);
        mNumPicker.setOnChangeListener(this);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (mCallback != null) {
            mNumPicker.clearFocus();
            mCallback.onNumberSet(mNumPicker, mNumPicker.getCurrent());
        }
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(VALUE, mNumPicker.getCurrent());
        int[] theRange = mNumPicker.getRange();
        state.putInt(MIN_VALUE, theRange[0]);
        state.putInt(MAX_VALUE, theRange[1]);
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int theMin = savedInstanceState.getInt(MIN_VALUE);
        final int theMax = savedInstanceState.getInt(MAX_VALUE);
        final int theCurrent = savedInstanceState.getInt(VALUE);
        mNumPicker.setCurrent(theCurrent);
        mNumPicker.setRange(theMin, theMax);
        mNumPicker.setOnChangeListener(this);
    }

    public void onChanged(NumberPicker picker, int oldVal, int newVal) {
        //
    }

}