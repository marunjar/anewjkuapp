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

package org.voidsink.anewjkuapp.fragment;

import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.support.v4.preference.PreferenceFragment;
import android.text.TextUtils;

import org.voidsink.anewjkuapp.BuildConfig;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.SettingsActivity;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.base.TwoLinesListPreference;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.utils.Consts;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onStart() {
        super.onStart();

        Analytics.sendScreen(getActivity(), Consts.SCREEN_SETTINGS);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference instanceof PreferenceScreen) {
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                if (preference.getFragment() != null) {
                    try {
                        Class<?> clazz = getActivity().getClassLoader().loadClass(preference.getFragment());
                        if (PreferenceFragment.class.isAssignableFrom(clazz)) {
                            Fragment pf = (Fragment) clazz.newInstance();
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
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public static class KusssSettingsFragment extends PreferenceFragment {
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
        public void onStart() {
            super.onStart();

            Analytics.sendScreen(getActivity(), Consts.SCREEN_SETTINGS_KUSSS);
        }
    }

    public static class AppSettingsFragment extends PreferenceFragment {
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
        }

        @Override
        public void onStart() {
            super.onStart();

            Analytics.sendScreen(getActivity(), Consts.SCREEN_SETTINGS_APP);
        }
    }

    public static class TimetableSettingsFragment extends PreferenceFragment {

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
            calendarLva.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updateTextPrefSummary((ListPreference) preference, newValue.toString(), R.string.pref_kusss_calendar_extended_summary);
                    return true;
                }
            });

            TwoLinesListPreference calendarExam = (TwoLinesListPreference) findPreference(PreferenceWrapper.PREF_EXTENDED_CALENDAR_EXAM);
            setEntries(calendars, calendarExam);
            updateTextPrefSummary(calendarExam, null, R.string.pref_kusss_calendar_extended_summary);
            calendarExam.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    updateTextPrefSummary((ListPreference) preference, newValue.toString(), R.string.pref_kusss_calendar_extended_summary);
                    return true;
                }
            });

            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                // disable calendar extension before ICS
                calendarLva.setEnabled(false);
                findPreference(PreferenceWrapper.PREF_EXTEND_CALENDAR_LVA).setEnabled(false);
                calendarExam.setEnabled(false);
                findPreference(PreferenceWrapper.PREF_EXTEND_CALENDAR_EXAM).setEnabled(false);
            }
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
            preference.setEntries(calendars.getDisplayNames().toArray(new String[calendars.getDisplayNames().size()]));
            preference.setEntryValues(calendars.getIdsAsStrings());
            preference.setEntriesSubtitles(calendars.getAccountNames().toArray(new String[calendars.getAccountNames().size()]));
        }

        @Override
        public void onStart() {
            super.onStart();

            Analytics.sendScreen(getActivity(), Consts.SCREEN_SETTINGS_TIMETABLE);
        }
    }
}