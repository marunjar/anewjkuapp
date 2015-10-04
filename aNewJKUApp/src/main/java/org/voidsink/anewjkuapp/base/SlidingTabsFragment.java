/*
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.voidsink.anewjkuapp.base;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.List;

public abstract class SlidingTabsFragment extends BaseFragment {

    /**
     * This class represents a tab to be displayed by {@link android.support.v4.view.ViewPager} and it's associated
     * {@link TabLayout}.
     */

    static final String TAG = SlidingTabsFragment.class.getSimpleName();

    /**
     * A {@link android.support.v4.view.ViewPager} which will be used in conjunction with the {@link TabLayout} above.
     */
    private ViewPager mViewPager;

    /**
     * List of {@link SlidingTabItem} which represent this sample's tabs.
     */
    private final List<SlidingTabItem> mTabs = new ArrayList<>();

    private TabLayout mTabLayout;

    protected abstract void fillTabs(List<SlidingTabItem> mTabs);

    /**
     * Inflates the {@link android.view.View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_sliding_tabs, container, false);
    }

    /**
     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
     * Here we can pick out the {@link View}s we need to configure from the content view.
     * <p/>
     * We set the {@link ViewPager}'s adapter to be an instance of
     * {@link org.voidsink.anewjkuapp.base.SlidingTabsFragment.SlidingFragmentPagerAdapter}. The {@link TabLayout} is then given the
     * {@link ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mTabLayout = (TabLayout) view.findViewById(R.id.sliding_tabs);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        createTabs(savedInstanceState);
    }

    protected void createTabs(Bundle savedInstanceState) {
        mTabs.clear();
        fillTabs(mTabs);

        mViewPager.setAdapter(new SlidingFragmentPagerAdapter(getChildFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public void notifyDataSetChanged() {
        if (mViewPager != null) {
//        Log.i(LOG_TAG, "notifyDataSetChanged");
            if (mViewPager.getAdapter() != null) {
                mViewPager.getAdapter().notifyDataSetChanged();
            }
        }
    }

    /**
     * The {@link android.support.v4.app.FragmentPagerAdapter} used to display pages in this sample. The individual pages
     * are instances of {@link Fragment}. Each page is
     * created by the relevant {@link SlidingTabItem} for the requested position.
     * <p/>
     * The important section of this class is the {@link #getPageTitle(int)} method which controls
     * what is displayed in the {@link TabLayout}.
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
            return super.instantiateItem(container, position);
//            Log.i(LOG_TAG, "instantiateItem() [position: " + position + "]");
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

        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link TabLayout}.
         * <p/>
         * Here we return the value returned from {@link SlidingTabItem#getTitle()}.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position).getTitle();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mViewPager != null) {
            int pos = mViewPager.getCurrentItem();
            if (pos >= 0) {
                outState.putCharSequence(Consts.ARG_TAB_FRAGMENT_TITLE, mViewPager.getAdapter().getPageTitle(pos));
                outState.putInt(Consts.ARG_TAB_FRAGMENT_POS, pos);
            }
        }
    }
}
