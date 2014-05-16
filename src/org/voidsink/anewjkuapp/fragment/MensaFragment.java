package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.IndicatedViewPagerFragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.view.Menu;
import android.view.MenuInflater;

public class MensaFragment extends IndicatedViewPagerFragment {

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);
	}

	@Override
	protected PagerAdapter getPagerAdapter(FragmentManager fm) {
		return new MensaPagerAdapter(fm);
	}

}
