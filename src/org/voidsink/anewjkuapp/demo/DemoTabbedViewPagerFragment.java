package org.voidsink.anewjkuapp.demo;

import org.voidsink.anewjkuapp.base.IndicatedViewPagerFragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;

public class DemoTabbedViewPagerFragment extends IndicatedViewPagerFragment {

	@Override
	protected PagerAdapter getPagerAdapter(FragmentManager fm) {
		return new DemoPagerAdapter(fm);
	}

}
