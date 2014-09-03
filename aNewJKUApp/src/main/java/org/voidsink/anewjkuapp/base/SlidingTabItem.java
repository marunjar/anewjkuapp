package org.voidsink.anewjkuapp.base;

import android.support.v4.app.Fragment;

/**
 * Created by paul on 03.09.2014.
 */
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

    /**
     * @return A new {@link Fragment} to be displayed by a {@link android.support.v4.view.ViewPager}
     */
    public Fragment createFragment() {
        try {
            return mFragment.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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
