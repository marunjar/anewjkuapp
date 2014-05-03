package org.voidsink.anewjkuapp.base;

import org.voidsink.anewjkuapp.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

public class BaseFragment extends Fragment {

	protected Context mContext = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		Log.i(getClass().getSimpleName(), "onCreate");

		this.mContext = this.getActivity();
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
//		Log.i(getClass().getSimpleName(), "onSaveInstanceState");
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.main, menu);
	}
	
	@Override
	public void onDestroyOptionsMenu() {
		Log.i(getClass().getSimpleName(), "onDestroyOptionsMenu");
		super.onDestroyOptionsMenu();
	}
}
