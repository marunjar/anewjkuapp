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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

/**
 * The {@link FragmentPagerAdapter} used to display pages in this sample. The individual pages
 * are instances of {@link Fragment}. Each page is
 * created by the relevant {@link SlidingTabItem} for the requested position.
 */
public class SlidingTabsFragmentPagerAdapter extends FragmentStateAdapter {

    private final List<SlidingTabItem> mTabs;

    public SlidingTabsFragmentPagerAdapter(@NonNull Fragment fragment, List<SlidingTabItem> tabs) {
        super(fragment);
        this.mTabs = tabs;
    }

    /**
     * Return the {@link Fragment} to be displayed at {@code position}.
     * <p/>
     * Here we return the value returned from {@link SlidingTabItem#createFragment()}.
     */
    @Override
    @NonNull
    public Fragment createFragment(int i) {
        return mTabs.get(i).createFragment();
    }

    @Override
    public int getItemCount() {
        return mTabs.size();
    }
}