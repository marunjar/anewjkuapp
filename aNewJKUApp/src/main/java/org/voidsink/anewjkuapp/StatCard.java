package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.List;

/**
 * Created by paul on 08.12.2014.
 */
public class StatCard {

    public static final int TYPE_LVA = 0;
    public static final int TYPE_GRADE = 1;

    private final int mType;
    private final List<Course> mCourses;
    private final List<Assessment> mAssessments;
    private final boolean mWeighted;
    private final boolean mPositiveOnly;
    private final List<Term> mTerms;

    public StatCard(int type, List<Term> terms, List<Course> courses, List<Assessment> assessments, boolean weighted, boolean positiveOnly) {
        this.mType = type;
        this.mTerms = terms;
        this.mCourses = courses;
        this.mAssessments = assessments;
        this.mWeighted = weighted;
        this.mPositiveOnly = positiveOnly;
    }

    public static StatCard getLvaInstance(List<Term> terms, List<Course> courses, List<Assessment> assessments) {
        return new StatCard(TYPE_LVA, terms, courses, assessments, false, false);
    }


    public static StatCard getAssessmentInstance(List<Term> terms, List<Assessment> assessments, boolean weighted, boolean positiveOnly) {
        return new StatCard(TYPE_GRADE, terms, null, assessments, weighted, positiveOnly);
    }

    public int getType() {
        return mType;
    }

    public List<Term> getTerms() {
        return mTerms;
    }

    public List<Course> getLvas() {
        return mCourses;
    }

    public List<Assessment> getAssessments() {
        return mAssessments;
    }

    public boolean isWeighted() {
        return mWeighted;
    }

    public boolean isPositiveOnly() {
        return mPositiveOnly;
    }

    public List<LvaWithGrade> getLvasWithGrades() {
        return AppUtils.getLvasWithGrades(getTerms(), getLvas(), getAssessments());
    }
}