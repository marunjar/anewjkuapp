package org.voidsink.anewjkuapp;

import android.graphics.Color;
import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.fragment.GradeDetailFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;

import java.util.Arrays;
import java.util.List;

/**
 * Created by paul on 03.09.2014.
 */
public class GradeTabItem extends SlidingTabItem {

    private final List<String> mTerms;

    public GradeTabItem(String title, List<String> terms) {
        super(title, GradeDetailFragment.class);

        this.mTerms = terms;
    }

    @Override
    public Fragment createFragment() {
        return new GradeDetailFragment(this.mTerms);
    }


}
