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
