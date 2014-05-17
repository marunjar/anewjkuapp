package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.base.IndicatedViewPagerFragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;

public class StatisticFragment extends IndicatedViewPagerFragment {

	@Override
	protected PagerAdapter getPagerAdapter(FragmentManager fm) {
		return new StatisticPageAdapter(fm, getActivity());
	}
	
}
