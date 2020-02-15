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

package org.voidsink.anewjkuapp.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.PreferenceHelper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;
import org.voidsink.anewjkuapp.base.ThemedActivity;
import org.voidsink.anewjkuapp.fragment.SettingsFragment;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

public class SettingsActivity extends ThemedActivity implements SharedPreferences.OnSharedPreferenceChangeListener, PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    public static final String ARG_SHOW_FRAGMENT = "show_fragment";

    private static final Logger logger = LoggerFactory.getLogger(SettingsActivity.class);

    private boolean mThemeChanged = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_toolbar_content);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ARG_SHOW_FRAGMENT);
        if (fragment == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_container, new SettingsFragment(), ARG_SHOW_FRAGMENT)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mThemeChanged) {
            logger.info("theme changed");

            AlarmManager alm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alm.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));

            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    protected void onInitActionBar(ActionBar actionBar) {
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceHelper.PREF_GET_NEW_EXAMS:
                AppUtils.triggerSync(this, true, Consts.ARG_WORKER_KUSSS_EXAMS);
                break;
            case PreferenceHelper.PREF_SYNC_INTERVAL_KEY:
                PreferenceHelper.applySyncInterval(this);
                break;
            case PreferenceHelper.PREF_USE_LIGHT_THEME:
            case PreferenceHelper.PREF_OVERRIDE_THEME:
                mThemeChanged = true;
                break;
            case PreferenceHelper.PREF_TRACKING_ERRORS:
                boolean trackingErrors = sharedPreferences.getBoolean(key, PreferenceHelper.PREF_TRACKING_ERRORS_DEFAULT);
                AnalyticsHelper.setEnabled(trackingErrors);
                AnalyticsHelper.sendPreferenceChanged(key, Boolean.toString(trackingErrors));
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_container, fragment)
                .addToBackStack(null)
                .commit();
        return true;
    }
}
