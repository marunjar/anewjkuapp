package org.voidsink.anewjkuapp.dashclock;

import org.voidsink.anewjkuapp.base.ThemedActivity;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.utils.Consts;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;

public class MensaDashclockSettings extends ThemedActivity {

	private static final String TAG = MensaDashclockSettings.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i(TAG, "onCreate");
        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, new MensaDashclockSettingsFragment())
                .commit();
	}

    @Override
    protected void onStart() {
        super.onStart();

        Analytics.sendScreen(this, Consts.SCREEN_SETTINGS_DASHCLOCK);
    }

    public static class MensaDashclockSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preference_dashclock_extension_mensa);
        }
    }
}
