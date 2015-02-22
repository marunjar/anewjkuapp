package org.voidsink.anewjkuapp;

import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.fragment.GradeDetailFragment;

import java.util.List;

public class GradeTabItem extends TermTabItem {

    public GradeTabItem(String title, List<String> terms) {
        super(title, terms, GradeDetailFragment.class);
    }

    @Override
    public Fragment createFragment() {
        return new GradeDetailFragment(getTerms());
    }


}
