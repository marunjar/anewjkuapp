package org.voidsink.anewjkuapp;

import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.fragment.StatFragmentDetail;

import java.util.List;

/**
 * Created by paul on 03.09.2014.
 */
public class StatTabItem extends SlidingTabItem {

    private final List<String> mTerms;

    public StatTabItem(String title, List<String> terms) {
        super(title, null);
        this.mTerms = terms;
    }

    @Override
    public Fragment createFragment() {
        return new StatFragmentDetail(this.mTerms);
    }
}
