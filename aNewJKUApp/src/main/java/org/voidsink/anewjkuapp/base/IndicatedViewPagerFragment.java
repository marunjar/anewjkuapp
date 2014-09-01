package org.voidsink.anewjkuapp.base;

import org.voidsink.anewjkuapp.R;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public abstract class IndicatedViewPagerFragment extends BaseFragment {

	private static final String ARG_CURRENT_TAB = "CURRENT_TAB";
	private static final String TAG = IndicatedViewPagerFragment.class
			.getSimpleName();
	private PagerAdapter mPagerAdapter;
	private ViewPager mViewPager;
	private FragmentTabHost mTabHost;
	private DataSetObserver mDataSetObserver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ViewPager and its adapters use support library
		// fragments, so use getSupportFragmentManager.
		mPagerAdapter = createPagerAdapter(getActivity()
				.getSupportFragmentManager());
		// new observer to refresh tabs on notifyDataSetChanged
		mDataSetObserver = new DataSetObserver() {
			@Override
			public void onChanged() {
//				Log.i(TAG, "onChanged");

				super.onChanged();

				if (isResumed()) {
					generateTabs();
				}
			}

			@Override
			public void onInvalidated() {
				Log.i(TAG, "onInvalidated");
				super.onInvalidated();
			}

		};
		mPagerAdapter.registerDataSetObserver(mDataSetObserver);
	}

	@Override
	public void onDestroy() {
		mPagerAdapter.unregisterDataSetObserver(mDataSetObserver);
		mTabHost = null;
		super.onDestroy();
	}

	protected abstract PagerAdapter createPagerAdapter(FragmentManager fm);
	
	protected PagerAdapter getPagerAdapter() {
		return this.mPagerAdapter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.i(TAG, "onCreateView");

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

			generateTabs();

			mViewPager = (ViewPager) view.findViewById(R.id.pager);
			mViewPager
					.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
						@Override
						public void onPageSelected(int position) {
							// When swiping between pages, select the
							// corresponding tab.
							mTabHost.setCurrentTab(position);
						}
					});
		}

		mViewPager.setAdapter(mPagerAdapter);

		if (savedInstanceState != null) {
			int startPosition = savedInstanceState.getInt(ARG_CURRENT_TAB);
			if (startPosition >= 0 && startPosition <= mPagerAdapter.getCount()) {
				mViewPager.setCurrentItem(startPosition, true);
			}
		}

		return view;
	}

	private void generateTabs() {
		if (mTabHost != null) {
			// store position
			int pos = mTabHost.getCurrentTab();
			// clear tabs
			mTabHost.setOnTabChangedListener(null);
			mTabHost.clearAllTabs();
			// create new tabs
			for (int i = 0; i < mPagerAdapter.getCount(); i++) {
				String title = (String) mPagerAdapter.getPageTitle(i);
				TabSpec tab = mTabHost.newTabSpec("tag" + Integer.toString(i))
						.setIndicator(title);
				mTabHost.addTab(tab, DummyFragment.class, null);
				mTabHost.getTabWidget().getChildAt(i)
						.setFocusableInTouchMode(true);
			}
			// create new listener
			mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
				@Override
				public void onTabChanged(String tabId) {
					int pos = mTabHost.getCurrentTab();
					mViewPager.setCurrentItem(pos, true);
				}
			});
			// recover position
			if (pos >= 0 && pos < mTabHost.getTabWidget().getTabCount()) {
				mTabHost.setCurrentTab(pos);
			}
		}
	}

	@Override
	public void onStart() {
		Log.i(TAG, "onStart");
		super.onStart();
	}

	@Override
	public void onStop() {
		Log.i(TAG, "onStop");
		super.onStart();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(ARG_CURRENT_TAB, mViewPager.getCurrentItem());
		super.onSaveInstanceState(outState);
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

}
