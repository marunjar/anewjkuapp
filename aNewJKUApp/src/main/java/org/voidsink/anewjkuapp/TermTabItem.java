package org.voidsink.anewjkuapp;

import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.base.SlidingTabItem;

import java.util.List;

public class TermTabItem extends SlidingTabItem {

    private final List<String> mTerms;

    public TermTabItem(CharSequence title, List<String> terms, Class<? extends Fragment> fragment) {
        super(title, fragment);
        this.mTerms = terms;
    }

    protected List<String> getTerms() {
        return mTerms;
    }
}
