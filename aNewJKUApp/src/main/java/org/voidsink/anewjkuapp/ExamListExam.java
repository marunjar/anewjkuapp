package org.voidsink.anewjkuapp;

import android.database.Cursor;

import org.voidsink.anewjkuapp.kusss.Exam;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.kusss.Lva;

import java.util.Date;

public class ExamListExam {

    private final Exam exam;
    private final Lva lva;

    public ExamListExam(Cursor c, LvaMap map) {
        this.exam = KusssHelper.createExam(c);
        this.lva = map.getLVA(exam.getTerm(), exam.getLvaNr());
    }

    public boolean mark() {
        return isRegistered();
    }

    public String getTitle() {
        if (lva != null) {
            return lva.getTitle();
        }
        return exam.getTitle();
    }

    public String getDescription() {
        return exam.getDescription();
    }

    public String getInfo() {
        return exam.getInfo();
    }

    public String getLvaNr() {
        return exam.getLvaNr();
    }

    public String getTerm() {
        return exam.getTerm();
    }

    public int getSkz() {
        if (lva != null) {
            return lva.getSKZ();
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
