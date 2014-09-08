package org.voidsink.anewjkuapp;

import android.graphics.Color;
import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.fragment.GradeDetailFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;

import java.util.List;

/**
 * Created by paul on 03.09.2014.
 */
public class GradeTabItem extends SlidingTabItem {

    private final String mTerm;
    private final List<ExamGrade> mGrades;

    public GradeTabItem(String title, String term, List<ExamGrade> grades) {
        super(title, GradeDetailFragment.class, (term == null ? CalendarUtils.COLOR_DEFAULT_EXAM : CalendarUtils.COLOR_DEFAULT_LVA), Color.GRAY);

        this.mTerm = term;
        this.mGrades = grades;
    }

    @Override
    public Fragment createFragment() {
        return new GradeDetailFragment(this.mTerm, this.mGrades);
    }


}
