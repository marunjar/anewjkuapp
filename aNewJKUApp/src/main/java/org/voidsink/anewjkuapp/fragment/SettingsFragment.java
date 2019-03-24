/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2018 Paul "Marunjar" Pretsch
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
import android.text.TextUtils;

import org.voidsink.anewjkuapp.BuildConfig;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.SettingsActivity;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.base.BasePreferenceFragment;
import org.voidsink.anewjkuapp.base.TwoLinesListPreference;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.io.File;
import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

public class SettingsFragment extends BasePreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    @Override
    public void onStart() {
        super.onStart();

        Analytics.sendScreen(getActivity(), Consts.SCREEN_SETTINGS);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof PreferenceScreen) {
            if (preference.getFragment() != null) {
                try {
                    Class<?> clazz = getActivity().getClassLoader().loadClass(preference.getFragment());
                    if (PreferenceFragmentCompat.class.isAssignableFrom(clazz)) {
                        Fragment pf = (Fragment) clazz.getConstructor().newInstance();
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.content_container, pf, SettingsActivity.ARG_SHOW_FRAGMENT)
                                .addToBackStack(pf.getClass().getCanonicalName())
                                .commit();
                        return true;
                    }
                } catch (Exception e) {
                    Analytics.sendException(getActivity(), e, false);
                }
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    public static class KusssSettingsFragment extends BasePreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Make sure default values are applied. In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.preference_kusss, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_kusss);
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {

        }

        @Override
        public void onStart() {
            super.onStart();

            Analytics.sendScreen(getActivity(), Consts.SCREEN_SETTINGS_KUSSS);
        }
    }

    public static class AppSettingsFragment extends BasePreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied. In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.preference_app, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_app);

            if (BuildConfig.FOSS_ONLY) {
                // disable tracking if GA is not used
                CheckBoxPreference trackingErrors = (CheckBoxPreference) findPreference(PreferenceWrapper.PREF_TRACKING_ERRORS);
                trackingErrors.setEnabled(false);
                trackingErrors.setChecked(false);
            }

            ListPreference mapFiles = (ListPreference) findPreference(PreferenceWrapper.PREF_MAP_FILE);
            if (mapFiles != null) {
                ArrayList<String> entries = new ArrayList<>();
                ArrayList<String> entryValues = new ArrayList<>();

                CollectMapFiles(entries, entryValues);

                mapFiles.setEntries(entries.toArray(new CharSequence[0]));
                mapFiles.setEntryValues(entryValues.toArray(new CharSequence[0]));

                int index = Math
                        .max(mapFiles.findIndexOfValue(getPreferenceManager().getSharedPreferences().getString(
                                mapFiles.getKey(), "")), 0);
                mapFiles.setValueIndex(index);

            }

        }

        private void CollectMapFiles(ArrayList<String> entries, ArrayList<String> entryValues) {
            entries.add("no .map file");
            entryValues.add("");

            ProgressDialog progressDialog = ProgressDialog.show(getContext(),
                    getContext().getString(R.string.progress_title), getContext()
                            .getString(R.string.progress_load_map_files), true);

            File root = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath());
            IterateDir(root, entries, entryValues);

            progressDialog.dismiss();
        }

        private void IterateDir(File f, ArrayList<String> entries,
                                ArrayList<String> entryValues) {

            File[] files = f.listFiles(pathname -> pathname.isDirectory()
                    || (pathname.isFile() && pathname.toString().endsWith(
                    ".map")));
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        IterateDir(file, entries, entryValues);
                    } else {
                        entries.add(file.getPath());
                        entryValues.add(file.getPath());
                    }
                }
            }
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {

        }

        @Override
        public void onStart() {
            super.onStart();

            Analytics.sendScreen(getActivity(), Consts.SCREEN_SETTINGS_APP);
        }
    }

    public static class TimetableSettingsFragment extends BasePreferenceFragment {

        private CalendarUtils.CalendarList calendars;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied. In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.preference_timetable, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_timetable);

            calendars = CalendarUtils.getCalendars(getActivity(), true);

            TwoLinesListPreference calendarLva = (TwoLinesListPreference) findPreference(PreferenceWrapper.PREF_EXTENDED_CALENDAR_LVA);
            setEntries(calendars, calendarLva);
            updateTextPrefSummary(calendarLva, null, R.string.pref_kusss_calendar_extended_summary);
            calendarLva.setOnPreferenceChangeListener((preference, newValue) -> {
                updateTextPrefSummary((ListPreference) preference, newValue.toString(), R.string.pref_kusss_calendar_extended_summary);
                return true;
            });

            TwoLinesListPreference calendarExam = (TwoLinesListPreference) findPreference(PreferenceWrapper.PREF_EXTENDED_CALENDAR_EXAM);
            setEntries(calendars, calendarExam);
            updateTextPrefSummary(calendarExam, null, R.string.pref_kusss_calendar_extended_summary);
            calendarExam.setOnPreferenceChangeListener((preference, newValue) -> {
                updateTextPrefSummary((ListPreference) preference, newValue.toString(), R.string.pref_kusss_calendar_extended_summary);
                return true;
            });
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {

        }

        private void updateTextPrefSummary(ListPreference preference, String value, int defaultSummaryResId) {
            if (preference != null) {
                if (TextUtils.isEmpty(value)) {
                    value = preference.getValue();
                }

                int index = preference.findIndexOfValue(value);
                if (index >= 0) {
                    preference.setSummary(preference.getEntries()[index]);
                } else {
                    preference.setSummary(defaultSummaryResId);
                }
            }
        }

        private void setEntries(CalendarUtils.CalendarList calendars, TwoLinesListPreference preference) {
            preference.setEntries(calendars.getDisplayNames().toArray(new String[0]));
            preference.setEntryValues(calendars.getIdsAsStrings());
            preference.setEntriesSubtitles(calendars.getAccountNames().toArray(new String[0]));
        }

        @Override
        public void onStart() {
            super.onStart();

            Analytics.sendScreen(getActivity(), Consts.SCREEN_SETTINGS_TIMETABLE);
        }
    }
}