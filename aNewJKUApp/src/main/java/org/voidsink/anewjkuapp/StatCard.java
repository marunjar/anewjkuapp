/*
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.List;

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
        return AppUtils.getLvasWithGrades(getTerms(), getLvas(), getAssessments(), false, null);
    }
}