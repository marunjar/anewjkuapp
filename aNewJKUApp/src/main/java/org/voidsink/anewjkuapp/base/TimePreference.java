/*
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;

import org.voidsink.anewjkuapp.R;

import java.util.Date;

public class TimePreference extends DialogPreference {

    private long mTime = 0;
    private final long DEFAULT_VALUE = 0;

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimePreference(Context context) {
        super(context);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

        if (restorePersistedValue) {
            if (defaultValue == null) {
                mTime = getPersistedLong(System.currentTimeMillis());
            } else {
                mTime = getPersistedLong(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        } else {
            if (defaultValue == null) {
                mTime = System.currentTimeMillis();
            } else {
                mTime = Long.parseLong((String) defaultValue);
            }
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        return DateFormat.getTimeFormat(getContext()).format(new Date(mTime));
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;

        setSummary(getSummary());
        if (callChangeListener(time)) {
            persistLong(time);
            notifyChanged();
        }
    }
} 