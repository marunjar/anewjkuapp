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

package org.voidsink.anewjkuapp.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import org.voidsink.anewjkuapp.BuildConfig;
import org.voidsink.anewjkuapp.PreferenceHelper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;
import org.voidsink.anewjkuapp.base.BasePreferenceFragment;
import org.voidsink.anewjkuapp.base.TwoLinesListPreference;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.io.File;
import java.util.ArrayList;

public class SettingsFragment extends BasePreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onStart() {
        super.onStart();

        AnalyticsHelper.sendScreen(getActivity(), Consts.SCREEN_SETTINGS);
    }

    public static class KusssSettingsFragment extends BasePreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            // Make sure default values are applied. In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getContext(),
                    R.xml.preference_kusss, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_kusss);
        }

        @Override
        public void onStart() {
            super.onStart();

            AnalyticsHelper.sendScreen(getActivity(), Consts.SCREEN_SETTINGS_KUSSS);
        }
    }

    public static class AppSettingsFragment extends BasePreferenceFragment {

        private void collectMapFiles(ArrayList<String> entries, ArrayList<String> entryValues) {
            entries.add("no .map file");
            entryValues.add("");

            ProgressDialog progressDialog = ProgressDialog.show(getContext(),
                    getContext().getString(R.string.progress_title), getContext()
                            .getString(R.string.progress_load_map_files), true);

            File root = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath());
            iterateDir(root, entries, entryValues);

            progressDialog.dismiss();
        }

        private void iterateDir(File f, ArrayList<String> entries,
                                ArrayList<String> entryValues) {

            File[] files = f.listFiles(pathname -> pathname.isDirectory()
                    || (pathname.isFile() && pathname.toString().endsWith(
                    ".map")));
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        iterateDir(file, entries, entryValues);
                    } else {
                        entries.add(file.getPath());
                        entryValues.add(file.getPath());
                    }
                }
            }
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            // Make sure default values are applied. In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getContext(),
                    R.xml.preference_app, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_app);

            if (BuildConfig.FOSS_ONLY) {
                // disable tracking if GA is not used
                SwitchPreferenceCompat trackingErrors = findPreference(PreferenceHelper.PREF_TRACKING_ERRORS);
                trackingErrors.setEnabled(false);
                trackingErrors.setChecked(false);
            }

            ListPreference mapFiles = findPreference(PreferenceHelper.PREF_MAP_FILE);
            if (mapFiles != null) {
                ArrayList<String> entries = new ArrayList<>();
                ArrayList<String> entryValues = new ArrayList<>();

                collectMapFiles(entries, entryValues);

                mapFiles.setEntries(entries.toArray(new CharSequence[0]));
                mapFiles.setEntryValues(entryValues.toArray(new CharSequence[0]));

                int index = Math
                        .max(mapFiles.findIndexOfValue(getPreferenceManager().getSharedPreferences().getString(
                                mapFiles.getKey(), "")), 0);
                mapFiles.setValueIndex(index);

            }
        }

        @Override
        public void onStart() {
            super.onStart();

            AnalyticsHelper.sendScreen(getActivity(), Consts.SCREEN_SETTINGS_APP);
        }
    }

    public static class TimetableSettingsFragment extends BasePreferenceFragment {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Make sure default values are applied. In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getContext(),
                    R.xml.preference_timetable, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_timetable);

            CalendarUtils.CalendarList calendars = CalendarUtils.getCalendars(getContext(), true);

            TwoLinesListPreference calendarLva = findPreference(PreferenceHelper.PREF_EXTENDED_CALENDAR_LVA);
            if (calendarLva != null) {
                calendarLva.setSummaryProvider(createCalendarSummaryProvider(R.string.pref_kusss_calendar_extended_summary));
                setEntries(calendars, calendarLva);
            }

            TwoLinesListPreference calendarExam = findPreference(PreferenceHelper.PREF_EXTENDED_CALENDAR_EXAM);
            if (calendarExam != null) {
                calendarExam.setSummaryProvider(createCalendarSummaryProvider(R.string.pref_kusss_calendar_extended_summary));
                setEntries(calendars, calendarExam);
            }
        }

        private Preference.SummaryProvider<ListPreference> createCalendarSummaryProvider(int defaultSummaryResId) {
            return preference -> {
                String value = preference.getValue();
                int index = preference.findIndexOfValue(value);
                if (index >= 0) {
                    return preference.getEntries()[index];
                } else {
                    return getContext().getString(defaultSummaryResId);
                }
            };
        }

        private void setEntries(CalendarUtils.CalendarList calendars, TwoLinesListPreference preference) {
            preference.setEntries(calendars.getDisplayNames().toArray(new String[0]));
            preference.setEntryValues(calendars.getIdsAsStrings());
            preference.setEntriesSubtitles(calendars.getAccountNames().toArray(new String[0]));
        }

        @Override
        public void onStart() {
            super.onStart();

            AnalyticsHelper.sendScreen(getActivity(), Consts.SCREEN_SETTINGS_TIMETABLE);
        }
    }
}