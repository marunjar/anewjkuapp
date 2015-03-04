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

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ARG_SHOW_FRAGMENT);
        if (fragment == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, new SettingsFragment(), ARG_SHOW_FRAGMENT)
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
                            .replace(android.R.id.content, fragment, ARG_SHOW_FRAGMENT)
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
            default:
                break;
        }
    }
}
