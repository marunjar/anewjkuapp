package org.voidsink.anewjkuapp.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import org.voidsink.anewjkuapp.ImportExamTask;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SettingsActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    private boolean mThemeChanged = false;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        AppUtils.applyTheme(this);

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            addPreferencesFromResource(R.xml.preference_kusss);
            addPreferencesFromResource(R.xml.preference_app);
        }

        // Preference mPrefMapFile =
        // findPreference(PreferenceWrapper.PREF_MAP_FILE);
        // mPrefMapFile.setOnPreferenceClickListener(new
        // OnPreferenceClickListener() {
        //
        // @Override
        // public boolean onPreferenceClick(Preference preference) {
        // return findMapFile();
        // }
        // });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Analytics.sendScreen(this, Consts.SCREEN_SETTINGS);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.action_settings));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(View parent, String name, Context context,
                             AttributeSet attrs) {
        initActionBar();

        return super.onCreateView(parent, name, context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(List<Header> target) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            loadHeadersFromResource(R.xml.preference_headers, target);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        switch (key) {
            case PreferenceWrapper.PREF_GET_NEW_EXAMS:
                new ImportExamTask(AppUtils.getAccount(this), this).execute();
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

    @Override
    protected void onStop() {
        super.onStop();

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

        if (mThemeChanged) {
            AlarmManager alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alm.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));

            Process.killProcess(Process.myPid());
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (KusssSettingsFragment.class.getName().equals(fragmentName)
                    || AppSettingsFragment.class.getName().equals(fragmentName)
                    || DashclockMensaSettingsFragment.class.getName().equals(
                    fragmentName)

                    ) {
                return (true);
            } else {
                return super.isValidFragment(fragmentName);
            }
        }
        return super.isValidFragment(fragmentName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DashclockMensaSettingsFragment extends
            PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied. In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.preference_dashclock_extension_mensa, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_dashclock_extension_mensa);
        }
    }

}
