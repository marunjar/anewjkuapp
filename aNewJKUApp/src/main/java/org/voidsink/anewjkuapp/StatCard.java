package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.List;

/**
 * Created by paul on 08.12.2014.
 */
public class StatCard {

    public static final int TYPE_LVA = 0;
    public static final int TYPE_GRADE = 1;

    private final int mType;
    private final List<Lva> mLvas;
    private final List<ExamGrade> mGrades;
    private final boolean mWeighted;
    private final boolean mPositiveOnly;
    private final List<String> mTerms;

    public StatCard(int type, List<String> terms, List<Lva> lvas, List<ExamGrade> grades, boolean weighted, boolean positiveOnly) {
        this.mType = type;
        this.mTerms = terms;
        this.mLvas = lvas;
        this.mGrades = grades;
        this.mWeighted = weighted;
        this.mPositiveOnly = positiveOnly;
    }

    public static StatCard getLvaInstance(List<String> terms, List<Lva> lvas, List<ExamGrade> grades) {
        return new StatCard(TYPE_LVA, terms, lvas, grades, false, false);
    }


    public static StatCard getGradeInstance(List<String> terms, List<ExamGrade> grades, boolean weighted, boolean positiveOnly) {
        return new StatCard(TYPE_GRADE, terms, null, grades, weighted, positiveOnly);
    }

    public int getType() {
        return mType;
    }

    public List<String> getTerms() {
        return mTerms;
    }

    public List<Lva> getLvas() {
        return mLvas;
    }

    public List<ExamGrade> getGrades() {
        return mGrades;
    }

    public boolean isWeighted() {
        return mWeighted;
    }

    public boolean isPositiveOnly() {
        return mPositiveOnly;
    }

    public List<LvaWithGrade> getLvasWithGrades() {
        return AppUtils.getLvasWithGrades(getTerms(), getLvas(), getGrades());
    }
}