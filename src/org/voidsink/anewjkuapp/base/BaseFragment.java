package org.voidsink.anewjkuapp.base;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.SettingsActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class BaseFragment extends Fragment {

	private static final String TAG = BaseFragment.class.getSimpleName();
	protected Context mContext = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Log.i(getClass().getSimpleName(), "onCreate");

		this.mContext = this.getActivity();
		setHasOptionsMenu(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "onOptionsItemSelected");

		switch (item.getItemId()) {
		case R.id.action_refresh_calendar:
			return onRefreshSelected(item);
		case R.id.action_settings:
			startActivity(new Intent(mContext, SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected boolean onRefreshSelected(MenuItem item) {
		Log.d(TAG, "onRefreshSelected");
		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Log.i(getClass().getSimpleName(), "onSaveInstanceState");
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.main, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onDestroyOptionsMenu() {
		Log.i(getClass().getSimpleName(), "onDestroyOptionsMenu");
		super.onDestroyOptionsMenu();
	}

	public void handleIntent(Intent intent) {
		
	}
}
