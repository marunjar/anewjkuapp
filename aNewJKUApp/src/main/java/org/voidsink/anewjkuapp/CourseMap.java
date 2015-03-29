/*******************************************************************************
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
 ******************************************************************************/

package org.voidsink.anewjkuapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.update.ImportCourseTask;
import org.voidsink.anewjkuapp.utils.Analytics;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.Collections;

public class CourseMap {

    private static final Comparator<Course> CourseTermComparator = new Comparator<Course>() {
        @Override
        public int compare(Course lhs, Course rhs) {
            // sort courses by term desc
            return rhs.getTerm().compareTo(lhs.getTerm());
        }
    };

    private Map<String, Course> map;

    public CourseMap(Context context) {
        this.map = new HashMap<>();

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(KusssContentContract.Course.CONTENT_URI,
                ImportCourseTask.COURSE_PROJECTION, null, null,
                KusssContentContract.Course.COL_TERM + " DESC");

        if (c != null) {
            while (c.moveToNext()) {
                try {
                    Course course = KusssHelper.createCourse(c);
                    this.map.put(KusssHelper.getCourseKey(course.getTerm(), course.getCourseId()), course);
                } catch (ParseException e) {
                    Analytics.sendException(context, e, true);
                }
            }
            c.close();
        }

    }

    public Course getExactCourse(Term term, String courseId) {
        return this.map.get(KusssHelper.getCourseKey(term, courseId));
    }

    public Course getCourse(Term term, String courseId) {
        Course course = this.map.get(KusssHelper.getCourseKey(term, courseId));
        if (course != null) {
            return course;
        }

        List<Course> courses = new ArrayList<>();
        for (Course tmp : this.map.values()) {
            if (courseId.equals(tmp.getCourseId())) {
                courses.add(tmp);
            }
        }

        if (courses.size() == 0) {
            return null;
        }

        Collections.sort(courses, CourseTermComparator);
        return courses.get(0);
    }

    public List<Course> getCourses() {
        return new ArrayList<Course>(this.map.values());
    }

}
