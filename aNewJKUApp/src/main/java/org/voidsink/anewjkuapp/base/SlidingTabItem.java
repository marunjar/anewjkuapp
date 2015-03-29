/*******************************************************************************
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
 ******************************************************************************/

package org.voidsink.anewjkuapp.base;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class SlidingTabItem {
    private final CharSequence mTitle;
    private final int mIndicatorColor;
    private final int mDividerColor;

    protected final Class<? extends Fragment> mFragment;

    public SlidingTabItem(CharSequence title, Class<? extends Fragment> fragment, int indicatorColor, int dividerColor) {
        mTitle = title;
        mIndicatorColor = indicatorColor;
        mDividerColor = dividerColor;
        mFragment = fragment;
    }

    public SlidingTabItem(CharSequence title, Class<? extends Fragment> fragment) {
        this(title, fragment, Color.WHITE, Color.WHITE);
    }

    /**
     * @return A new {@link Fragment} to be displayed by a {@link android.support.v4.view.ViewPager}
     */
    public Fragment createFragment() {
        try {
            Fragment f = mFragment.newInstance();
            Bundle b = getArguments();
            if (b != null) {
                f.setArguments(b);
            }
            return f;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Bundle getArguments() {
        return null;
    }

    /**
     * @return the title which represents this tab. In this sample this is used directly by
     * {@link android.support.v4.view.PagerAdapter#getPageTitle(int)}
     */
    CharSequence getTitle() {
        return mTitle;
    }

    /**
     * @return the color to be used for indicator on the {@link org.voidsink.anewjkuapp.view.SlidingTabLayout}
     */
    int getIndicatorColor() {
        return mIndicatorColor;
    }

    /**
     * @return the color to be used for right divider on the {@link org.voidsink.anewjkuapp.view.SlidingTabLayout}
     */
    int getDividerColor() {
        return mDividerColor;
    }

}
