package org.voidsink.anewjkuapp;

import android.graphics.Color;
import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.fragment.LvaDetailFragment;
import org.voidsink.anewjkuapp.fragment.StatFragmentDetail;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 03.09.2014.
 */
public class StatTabItem extends SlidingTabItem{

    private final List<String> mTerms;
    private final List<ExamGrade> mGrades;
    private final List<Lva> mLvas;

    public StatTabItem(String term, List<Lva> lvas, List<ExamGrade> grades) {
        super(term, LvaDetailFragment.class, CalendarUtils.COLOR_DEFAULT_LVA, Color.GRAY);

        this.mTerms = new ArrayList<String>();
        this.mTerms.add(term);
        this.mGrades = grades;
        this.mLvas = lvas;
    }

    public StatTabItem(String title, List<String> terms, List<Lva> lvas, List<ExamGrade> grades) {
        super(title, null, CalendarUtils.COLOR_DEFAULT_EXAM, Color.GRAY);
        this.mTerms = terms;
        this.mLvas = lvas;
        this.mGrades = grades;
    }

    @Override
    public Fragment createFragment() {
        return new StatFragmentDetail(this.mTerms, this.mLvas, this.mGrades);
    }
}
