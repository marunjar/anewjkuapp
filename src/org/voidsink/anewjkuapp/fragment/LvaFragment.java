package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.LvaDetailPageAdapter;
import org.voidsink.anewjkuapp.base.IndicatedViewPagerFragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;

public class LvaFragment extends IndicatedViewPagerFragment {

	@Override
	protected PagerAdapter getPagerAdapter(FragmentManager fm) {
		return new LvaDetailPageAdapter(fm, getActivity());
	}
	
}
