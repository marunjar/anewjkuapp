/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2020 Paul "Marunjar" Pretsch
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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp.base;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.util.Calendar;

public class TimePickerDialogFragment extends PreferenceDialogFragmentCompat
        implements TimePickerDialog.OnTimeSetListener {

    private long mTime;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mTime = getTimePreference().getTime();

        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mTime);


        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getContext(), resolveDialogTheme(getContext()), this, c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE), DateFormat.is24HourFormat(getContext()));
    }

    private int resolveDialogTheme(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(androidx.appcompat.R.attr.alertDialogTheme, outValue, true);
        return outValue.resourceId;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        getTimePreference().setTime(mTime);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(0);
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);

        mTime = c.getTimeInMillis();
    }

    public static TimePickerDialogFragment newInstance(String key) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();
        Bundle b = new Bundle(1);
        b.putString("key", key);
        fragment.setArguments(b);
        return fragment;
    }

    protected TimePreference getTimePreference() {
        return (TimePreference) this.getPreference();
    }
}