package org.voidsink.anewjkuapp.fragment;

import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.support.v4.preference.PreferenceFragment;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.SettingsActivity;
import org.voidsink.anewjkuapp.utils.Analytics;
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
                                    .replace(android.R.id.content, pf, SettingsActivity.ARG_SHOW_FRAGMENT)
                                    .addToBackStack(pf.getClass().getCanonicalName())
                                    .commit();
                            return true;
                        }
                    } catch (ClassNotFoundException | java.lang.InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
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
        }

        @Override
        public void onStart() {
            super.onStart();

            Analytics.sendScreen(getActivity(), Consts.SCREEN_SETTINGS_APP);
        }
    }
}