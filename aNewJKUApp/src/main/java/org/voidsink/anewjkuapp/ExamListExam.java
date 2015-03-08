package org.voidsink.anewjkuapp;

import android.database.Cursor;

import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.kusss.Exam;
import org.voidsink.anewjkuapp.kusss.KusssHelper;

import java.util.Date;

public class ExamListExam {

    private final Exam exam;
    private final Course course;

    public ExamListExam(Cursor c, CourseMap map) {
        this.exam = KusssHelper.createExam(c);
        this.course = map.getCourse(exam.getTerm(), exam.getCourseId());
    }

    public boolean mark() {
        return isRegistered();
    }

    public String getTitle() {
        if (course != null) {
            return course.getTitle();
        }
        return exam.getTitle();
    }

    public String getDescription() {
        return exam.getDescription();
    }

    public String getInfo() {
        return exam.getInfo();
    }

    public String getCourseId() {
        return exam.getCourseId();
    }

    public String getTerm() {
        return exam.getTerm();
    }

    public int getCid() {
        if (course != null) {
            return course.getCid();
        }
        return 0;
    }

    public Date getDtStart() {
        return exam.getDtStart();
    }

    public Date getDtEnd() {
        return exam.getDtEnd();
    }

    public String getLocation() {
        return exam.getLocation();
    }

    public boolean isRegistered() {
        return exam.isRegistered();
    }

}
