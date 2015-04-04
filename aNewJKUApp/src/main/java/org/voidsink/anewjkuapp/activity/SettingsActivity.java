/*******************************************************************************
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
 ******************************************************************************/

package org.voidsink.anewjkuapp.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.util.Log;

import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.base.ThemedActivity;
import org.voidsink.anewjkuapp.fragment.SettingsFragment;
import org.voidsink.anewjkuapp.update.UpdateService;
import org.voidsink.anewjkuapp.utils.Consts;

public class SettingsActivity extends ThemedActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String ACTION_PREFS_LEGACY = "org.voidsink.anewjkuapp.prefs.LEGACY";
    public static final String ARG_SHOW_FRAGMENT = "show_fragment";

    private static final String TAG = SettingsActivity.class.getSimpleName();

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && ACTION_PREFS_LEGACY.equals(intent.getAction())) {
            Fragment fragment = null;
            try {
                String clazzname = intent.getDataString();
                if (clazzname != null) {
                    Class<?> clazz = getClassLoader().loadClass(clazzname);
                    if (PreferenceFragment.class.isAssignableFrom(clazz)) {
                        fragment = (Fragment) clazz.newInstance();
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            if (fragment != null) {
                Fragment oldFragment = getSupportFragmentManager().findFragmentByTag(ARG_SHOW_FRAGMENT);
                if (oldFragment == null || !fragment.getClass().equals(oldFragment.getClass())) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_container, fragment, ARG_SHOW_FRAGMENT)
                            .addToBackStack(fragment.getClass().getCanonicalName())
                            .commit();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

        if (mThemeChanged) {
            Log.i(TAG, "theme changed");

            AlarmManager alm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alm.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));

            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    protected void onInitActionBar(ActionBar actionBar) {
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceWrapper.PREF_GET_NEW_EXAMS:
                Intent mUpdateService = new Intent(this, UpdateService.class);
                mUpdateService.putExtra(Consts.ARG_UPDATE_KUSSS_EXAMS, true);
                this.startService(mUpdateService);

                break;
            case PreferenceWrapper.PREF_SYNC_INTERVAL_KEY:
                PreferenceWrapper.applySyncInterval(this);
                break;
            case PreferenceWrapper.PREF_USE_LIGHT_THEME:
                mThemeChanged = true;
                break;
            case PreferenceWrapper.PREF_TRACKING_ERRORS:
                boolean trackingErrors = sharedPreferences.getBoolean(key, PreferenceWrapper.PREF_TRACKING_ERRORS_DEFAULT);
                if (trackingErrors) {
                    // track opting in
                    Analytics.setEnabled(trackingErrors);
                    Analytics.sendPreferenceChanged(key, Boolean.toString(trackingErrors));
                } else {
                    // track opting out
                    Analytics.sendPreferenceChanged(key, Boolean.toString(trackingErrors));
                    Analytics.setEnabled(trackingErrors);
                }

                break;
            default:
                break;
        }
    }
}
