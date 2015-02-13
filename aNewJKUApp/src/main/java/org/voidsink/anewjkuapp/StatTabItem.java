package org.voidsink.anewjkuapp;

import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.fragment.StatFragmentDetail;

import java.util.List;

public class StatTabItem extends TermTabItem {

    public StatTabItem(String title, List<String> terms) {
        super(title, terms, null);
    }

    @Override
    public Fragment createFragment() {
        return new StatFragmentDetail(getTerms());
    }
}
