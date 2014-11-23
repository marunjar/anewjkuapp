package org.voidsink.anewjkuapp.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.view.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 03.09.2014.
 */
public abstract class SlidingTabsFragment extends BaseFragment {

    /**
     * This class represents a tab to be displayed by {@link android.support.v4.view.ViewPager} and it's associated
     * {@link org.voidsink.anewjkuapp.view.SlidingTabLayout}.
     */

    static final String TAG = SlidingTabsFragment.class.getSimpleName();

    /**
     * A custom {@link android.support.v4.view.ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    private SlidingTabLayout mSlidingTabLayout;

    /**
     * A {@link android.support.v4.view.ViewPager} which will be used in conjunction with the {@link SlidingTabLayout} above.
     */
    private ViewPager mViewPager;

    /**
     * List of {@link SlidingTabItem} which represent this sample's tabs.
     */
    private List<SlidingTabItem> mTabs = new ArrayList<SlidingTabItem>();

    public void updateData() {
        if (isAdded()) {
            mTabs.clear();
            fillTabs(mTabs);
            notifyDataSetChanged();
        }
    }

    protected abstract void fillTabs(List<SlidingTabItem> mTabs);

    /**
     * Inflates the {@link android.view.View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        updateData();

        return LayoutInflater.from(new ContextThemeWrapper(getContext(), R.style.AppTheme)).inflate(R.layout.fragment_sliding_tabs, container, false);
    }

    // BEGIN_INCLUDE (fragment_onviewcreated)

    /**
     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
     * Here we can pick out the {@link View}s we need to configure from the content view.
     * <p/>
     * We set the {@link ViewPager}'s adapter to be an instance of
     * {@link org.voidsink.anewjkuapp.base.SlidingTabsFragment.SlidingFragmentPagerAdapter}. The {@link SlidingTabLayout} is then given the
     * {@link ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SlidingFragmentPagerAdapter(getChildFragmentManager()));
        // END_INCLUDE (setup_viewpager)

        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

        SlidingTabLayout.TabColorizer tc = createTabColorizer();

        if (tc != null) {
            // BEGIN_INCLUDE (tab_colorizer)
            // Set a TabColorizer to customize the indicator and divider colors. Here we just retrieve
            // the tab at the position, and return it's set color
            mSlidingTabLayout.setCustomTabColorizer(tc);

//            mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
//
//                @Override
//                public int getIndicatorColor(int position) {
//                    return mTabs.get(position).getIndicatorColor();
//                }
//
//                @Override
//                public int getDividerColor(int position) {
//                    return mTabs.get(position).getDividerColor();
//                }
//
//            });
        }
        // END_INCLUDE (tab_colorizer)
        // END_INCLUDE (setup_slidingtablayout)
    }

    /**
     * use this to create your own TabColorizer
     */
    protected SlidingTabLayout.TabColorizer createTabColorizer() {
        return null;
    }
    // END_INCLUDE (fragment_onviewcreated)

    public void notifyDataSetChanged() {
        if (mViewPager != null) {
//        Log.i(LOG_TAG, "notifyDataSetChanged");
            if (mViewPager.getAdapter() != null) {
                mViewPager.getAdapter().notifyDataSetChanged();
            }
            if (mSlidingTabLayout != null) {
                mSlidingTabLayout.setViewPager(mViewPager);
            }
        }
    }

    /**
     * The {@link android.support.v4.app.FragmentPagerAdapter} used to display pages in this sample. The individual pages
     * are instances of {@link Fragment}. Each page is
     * created by the relevant {@link SlidingTabItem} for the requested position.
     * <p/>
     * The important section of this class is the {@link #getPageTitle(int)} method which controls
     * what is displayed in the {@link SlidingTabLayout}.
     */
    class SlidingFragmentPagerAdapter extends FragmentStatePagerAdapter {

        SlidingFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return the {@link android.support.v4.app.Fragment} to be displayed at {@code position}.
         * <p/>
         * Here we return the value returned from {@link SlidingTabItem#createFragment()}.
         */
        @Override
        public Fragment getItem(int i) {
            return mTabs.get(i).createFragment();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object o = super.instantiateItem(container, position);
//            Log.i(LOG_TAG, "instantiateItem() [position: " + position + "]");
            return o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
//            Log.i(LOG_TAG, "destroyItem() [position: " + position + "]");
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        // BEGIN_INCLUDE (pageradapter_getpagetitle)

        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p/>
         * Here we return the value returned from {@link SlidingTabItem#getTitle()}.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position).getTitle();
        }
        // END_INCLUDE (pageradapter_getpagetitle)

    }


}
