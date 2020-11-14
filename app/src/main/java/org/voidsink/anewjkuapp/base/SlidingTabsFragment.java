/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2020 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a tab to be displayed by {@link ViewPager2} and it's associated
 * {@link TabLayout}.
 */
public abstract class SlidingTabsFragment extends BaseFragment {

    /**
     * A {@link ViewPager2} which will be used in conjunction with the {@link TabLayout} above.
     */
    private ViewPager2 mViewPager;

    /**
     * List of {@link SlidingTabItem} which represent this sample's tabs.
     */
    private final List<SlidingTabItem> mTabs = new ArrayList<>();

    private TabLayout mTabLayout;

    protected abstract void fillTabs(@NonNull List<SlidingTabItem> mTabs);

    /**
     * Inflates the {@link android.view.View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_sliding_tabs, container, false);
    }

    /**
     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
     * Here we can pick out the {@link View}s we need to configure from the content view.
     * <p/>
     * We set the {@link ViewPager2}'s adapter to be an instance of
     * {@link SlidingTabsFragmentPagerAdapter}. The {@link TabLayout} is then given the
     * {@link ViewPager2} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewPager = view.findViewById(R.id.viewpager);
        mTabLayout = view.findViewById(R.id.sliding_tabs);
    }

    private void onConfigureTab(TabLayout.Tab tab, int position) {
        if (position < mTabs.size()) {
            tab.setText(mTabs.get(position).getTitle());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        createTabs(savedInstanceState);
    }

    protected void createTabs(Bundle savedInstanceState) {
        mTabs.clear();
        fillTabs(mTabs);

        mViewPager.setAdapter(new SlidingTabsFragmentPagerAdapter(this, mTabs));
        new TabLayoutMediator(mTabLayout, mViewPager, this::onConfigureTab).attach();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mViewPager != null) {
            int pos = mViewPager.getCurrentItem();
            if (pos >= 0) {
                outState.putInt(Consts.ARG_TAB_FRAGMENT_POS, pos);
            }
        }
    }
}
