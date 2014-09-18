package org.voidsink.anewjkuapp;

import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.fragment.MensaDayFragment;

import java.text.DateFormat;
import java.util.Date;


/**
 * Created by paul on 18.09.2014.
 */
public class MensaDayTabItem extends SlidingTabItem{

    private final Date mDate;

    public MensaDayTabItem(Date date, int indicatorColor, int dividerColor) {
        super(DateFormat.getDateInstance().format(date), null, indicatorColor, dividerColor);

        this.mDate = date;
    }

    @Override
    public Fragment createFragment() {
        MensaDayFragment f = new MensaDayFragment();
        f.setDate(mDate);
        return f;
    }
}
