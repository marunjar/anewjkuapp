package org.voidsink.anewjkuapp;

import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.fragment.LvaDetailFragment;

import java.util.List;

/**
 * Created by paul on 03.09.2014.
 */
public class LvaTabItem extends SlidingTabItem{

    private final List<String> mTerms;

    public LvaTabItem(String title, List<String> terms) {
        super(title, null);
        this.mTerms = terms;
    }

    @Override
    public Fragment createFragment() {
        return new LvaDetailFragment(this.mTerms);
    }
}
