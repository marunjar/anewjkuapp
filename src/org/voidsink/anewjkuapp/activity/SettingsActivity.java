package org.voidsink.anewjkuapp.activity;

import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (PreferenceWrapper.getUseLightDesign(this)) {
			this.setTheme(R.style.AppTheme_Light);
		} else {
			this.setTheme(R.style.AppTheme);
		}

		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource(R.xml.preference_kusss);
			addPreferencesFromResource(R.xml.preference_app);
		}

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);

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

	protected boolean findMapFile() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplication(), "TODO", Toast.LENGTH_SHORT).show();
		return false;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(View parent, String name, Context context,
			AttributeSet attrs) {
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
		case PreferenceWrapper.PREF_SYNC_INTERVAL_KEY:
			PreferenceWrapper.applySyncInterval(this);
			break;
		case PreferenceWrapper.PREF_USE_LIGHT_THEME:
			Intent i = new Intent(this, MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
			break;
		default:
			break;
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
