package org.voidsink.anewjkuapp.dashclock;

import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.utils.Consts;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class MensaDashclockSettings extends PreferenceActivity {

	private static final String TAG = MensaDashclockSettings.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i(TAG, "onCreate");
		
		addPreferencesFromResource(R.xml.preference_dashclock_extension_mensa);
	}

    @Override
    protected void onStart() {
        super.onStart();

        Analytics.sendScreen(this, Consts.SCREEN_SETTINGS_DASHCLOCK);
    }
}
