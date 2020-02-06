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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;

import org.voidsink.anewjkuapp.R;

import java.util.Calendar;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {

    private static final String DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG";

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (isAdded() && getParentFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) == null) {
            DialogFragment f = null;
            if (preference instanceof TimePreference) {
                f = TimePickerDialogFragment.newInstance(preference.getKey());
            } else if (preference instanceof TwoLinesListPreference) {
                f = TwoLinesListPreferenceDialogFragment.newInstance(preference.getKey());
            }

            if (f != null) {
                f.setTargetFragment(this, 0);
                f.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
                return;
            }
        }

        super.onDisplayPreferenceDialog(preference);
    }


    public static class TwoLinesListPreferenceDialogFragment extends PreferenceDialogFragmentCompat {
        private int mClickedDialogEntryIndex;

        public TwoLinesListPreferenceDialogFragment() {
        }

        public static TwoLinesListPreferenceDialogFragment newInstance(String key) {
            TwoLinesListPreferenceDialogFragment fragment = new TwoLinesListPreferenceDialogFragment();
            Bundle b = new Bundle(1);
            b.putString("key", key);
            fragment.setArguments(b);
            return fragment;
        }

        private TwoLinesListPreference getListPreference() {
            return (TwoLinesListPreference) this.getPreference();
        }

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            super.onPrepareDialogBuilder(builder);

            ListPreference preference = this.getListPreference();
            if (preference.getEntries() != null && preference.getEntryValues() != null) {
                // adapter
                String[] mEntriesString = (String[]) getListPreference().getEntries();

                ListAdapter adapter = new ArrayAdapter<String>(
                        getContext(), R.layout.custom_simple_list_item_2_single_choice, mEntriesString) {

                    ViewHolder holder;

                    class ViewHolder {
                        TextView title;
                        TextView subTitle;
                        RadioButton radio;
                    }

                    @NonNull
                    @SuppressLint("InflateParams")
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        if (convertView == null) {
                            convertView = inflater.inflate(R.layout.custom_simple_list_item_2_single_choice, null);

                            holder = new ViewHolder();
                            holder.title = convertView.findViewById(android.R.id.text1);
                            holder.subTitle = convertView.findViewById(android.R.id.text2);
                            holder.radio = convertView.findViewById(R.id.radio);

                            convertView.setTag(holder);
                        } else {
                            // view already defined, retrieve view holder
                            holder = (ViewHolder) convertView.getTag();
                        }

                        holder.title.setText(getListPreference().getEntries()[position]);
                        holder.subTitle.setText(getListPreference().getEntriesSubtitles()[position]);
                        if (holder.radio != null) {
                            holder.radio.setChecked(position == mClickedDialogEntryIndex);
                        }

                        return convertView;
                    }
                };


                this.mClickedDialogEntryIndex = preference.findIndexOfValue(preference.getValue());
                builder.setSingleChoiceItems(adapter, this.mClickedDialogEntryIndex, (dialog, which) -> {
                    TwoLinesListPreferenceDialogFragment.this.mClickedDialogEntryIndex = which;
                    TwoLinesListPreferenceDialogFragment.this.onClick(dialog, -1);
                    dialog.dismiss();
                });
                builder.setPositiveButton(null, null);
            } else {
                throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
            }
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            ListPreference preference = this.getListPreference();
            if (positiveResult && this.mClickedDialogEntryIndex >= 0 && preference.getEntryValues() != null) {
                String value = preference.getEntryValues()[this.mClickedDialogEntryIndex].toString();
                if (preference.callChangeListener(value)) {
                    preference.setValue(value);
                }
            }

        }
    }

    public static class TimePickerDialogFragment extends PreferenceDialogFragmentCompat
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
}
