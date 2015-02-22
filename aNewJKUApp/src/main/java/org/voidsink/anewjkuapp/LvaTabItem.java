package org.voidsink.anewjkuapp;

import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.fragment.LvaDetailFragment;

import java.util.List;

public class LvaTabItem extends TermTabItem{

    public LvaTabItem(String title, List<String> terms) {
        super(title, terms, null);
    }

    @Override
    public Fragment createFragment() {
        return new LvaDetailFragment(getTerms());
    }
}
