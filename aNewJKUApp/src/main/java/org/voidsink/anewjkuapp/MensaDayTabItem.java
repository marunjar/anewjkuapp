package org.voidsink.anewjkuapp;

import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.fragment.MensaDayFragment;

import java.util.Date;


/**
 * Created by paul on 18.09.2014.
 */
public class MensaDayTabItem extends SlidingTabItem {

    private static final String DATE_PATTERN = "EEEE";
    private final Date mDate;

    public MensaDayTabItem(String title, Date date, int indicatorColor, int dividerColor) {
        super(title, null, indicatorColor, dividerColor);

        this.mDate = date;
    }

    @Override
    public Fragment createFragment() {
        MensaDayFragment f = new MensaDayFragment();
        f.setDate(mDate);
        return f;
    }
}
