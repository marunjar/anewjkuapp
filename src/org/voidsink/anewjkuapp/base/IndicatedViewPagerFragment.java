package org.voidsink.anewjkuapp.base;

import org.voidsink.anewjkuapp.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public abstract class IndicatedViewPagerFragment extends Fragment {

	private static final String ARG_CURRENT_TAB = "CURRENT_TAB";
	// When requested, this adapter returns a DemoObjectFragment,
	// representing an object in the collection.
	PagerAdapter mPagerAdapter;
	ViewPager mViewPager;
	FragmentTabHost mTabHost;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ViewPager and its adapters use support library
		// fragments, so use getSupportFragmentManager.
		mPagerAdapter = getPagerAdapter(getActivity()
				.getSupportFragmentManager());
	}

	protected abstract PagerAdapter getPagerAdapter(FragmentManager fm);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mTabHost = null;
		View view = null;

		if (!useTabHost()) {
			view = inflater.inflate(R.layout.fragment_tabbed_view_pager,
					container, false);
			mViewPager = (ViewPager) view.findViewById(R.id.pager);
		} else {
			view = inflater.inflate(
					R.layout.fragment_tabbed_view_pager_with_tabs, container,
					false);
			mTabHost = (FragmentTabHost) view;
			mTabHost.setup(getActivity(), getChildFragmentManager(),
					android.R.id.tabcontent);

			for (int i = 0; i < mPagerAdapter.getCount(); i++) {
				String title = (String) mPagerAdapter.getPageTitle(i);
				TabSpec tab = mTabHost.newTabSpec("tag" + Integer.toString(i))
						.setIndicator(title);
				mTabHost.addTab(tab, DummyFragment.class, null);
				mTabHost.getTabWidget().getChildAt(i)
						.setFocusableInTouchMode(true);
			}

			mViewPager = (ViewPager) view.findViewById(R.id.pager);
			mViewPager
					.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
						@Override
						public void onPageSelected(int position) {
							// When swiping between pages, select the
							// corresponding tab.
							mTabHost.setCurrentTab(position);
							mTabHost.getTabWidget().focusCurrentTab(position);
						}
					});
			mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
				@Override
				public void onTabChanged(String tabId) {
					int pos = mTabHost.getCurrentTab();
					mViewPager.setCurrentItem(pos, true);
				}
			});
		}

		mViewPager.setAdapter(mPagerAdapter);
		// wont work at the moment
		// if (savedInstanceState != null) {
		// int startPosition = savedInstanceState.getInt(ARG_CURRENT_TAB);
		// if (startPosition >= 0 && startPosition <= mPagerAdapter.getCount())
		// {
		// mViewPager.setCurrentItem(startPosition, true);
		// if (mTabHost != null) {
		// mTabHost.setCurrentTab(startPosition);
		// }
		// }
		// }

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(ARG_CURRENT_TAB, mTabHost.getCurrentTab());
	}

	protected boolean useTabHost() {
		return true;
	}

	public static class DummyFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return null;// super.onCreateView(inflater, container,
						// savedInstanceState);
		}
	}

	@Override
	public void onDestroy() {
		mTabHost = null;
		super.onDestroy();
	}

}
