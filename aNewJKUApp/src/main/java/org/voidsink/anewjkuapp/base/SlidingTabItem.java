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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.voidsink.anewjkuapp.utils.Consts;

public class SlidingTabItem {
    private final CharSequence mTitle;

    private final Class<? extends Fragment> mFragment;

    public SlidingTabItem(CharSequence title, Class<? extends Fragment> fragment) {
        mTitle = title;
        mFragment = fragment;
    }

    /**
     * @return A new {@link Fragment} to be displayed by a {@link ViewPager}
     */
    public Fragment createFragment() {
        try {
            Fragment f = mFragment.getConstructor().newInstance();
            Bundle arguments = new Bundle();
            getArguments(arguments);
            arguments.putCharSequence(Consts.ARG_FRAGMENT_TITLE, mTitle);
            f.setArguments(arguments);
            return f;
        } catch (Exception e) {
            return null;
        }
    }

    protected void getArguments(@NonNull Bundle arguments) {
    }

    /**
     * @return the title which represents this tab. In this sample this is used directly by
     * {@link PagerAdapter#getPageTitle(int)}
     */
    public CharSequence getTitle() {
        return mTitle;
    }
}
