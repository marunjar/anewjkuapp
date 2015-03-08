package org.voidsink.anewjkuapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.update.ImportLvaTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.Collections;

public class LvaMap {

    private static final Comparator<Course> LvaTermComparator = new Comparator<Course>() {
        @Override
        public int compare(Course lhs, Course rhs) {
            // sort lvas by term desc
            return rhs.getTerm().compareTo(lhs.getTerm());
        }
    };

    private Map<String, Course> map;

    public LvaMap(Context context) {
        this.map = new HashMap<String, Course>();

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(KusssContentContract.Lva.CONTENT_URI,
                ImportLvaTask.LVA_PROJECTION, null, null,
                KusssContentContract.Lva.LVA_COL_TERM + " DESC");

        if (c != null) {
            while (c.moveToNext()) {
                Course course = KusssHelper.createLva(c);
                this.map.put(KusssHelper.getLvaKey(course.getTerm(), course.getLvaNr()), course);
            }
            c.close();
        }

    }

    public Course getExactLVA(String term, String lvaNr) {
        return this.map.get(KusssHelper.getLvaKey(term, lvaNr));
    }

    public Course getLVA(String term, String lvaNr) {
        Course course = this.map.get(KusssHelper.getLvaKey(term, lvaNr));
        if (course != null) {
            return course;
        }

        List<Course> courses = new ArrayList<Course>();
        for (Course tmp : this.map.values()) {
            if (lvaNr.equals(tmp.getLvaNr())) {
                courses.add(tmp);
            }
        }

        if (courses.size() == 0) {
            return null;
        }

        Collections.sort(courses, LvaTermComparator);
        return courses.get(0);
    }

    public List<Course> getLVAs() {
        return new ArrayList<Course>(this.map.values());
    }

}
