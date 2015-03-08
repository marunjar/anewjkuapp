package org.voidsink.anewjkuapp.kusss;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;
import org.voidsink.anewjkuapp.update.ImportExamTask;
import org.voidsink.anewjkuapp.update.ImportGradeTask;
import org.voidsink.anewjkuapp.update.ImportLvaTask;
import org.voidsink.anewjkuapp.update.ImportStudiesTask;

import java.util.Date;

public class KusssHelper {

    private static final String URL_GET_EXAMS = "https://www.kusss.jku.at/kusss/szexaminationlist.action";

    public static void showExamInBrowser(Context context, String lvaNr) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_GET_EXAMS));
        context.startActivity(intent);
        // TODO: create activity with webview that uses stored credentials to login and open page with search for lvanr
    }

    public static String getLvaKey(String term, String lvaNr) {
        return term + "-" + lvaNr;
    }

    public static Course createLva(Cursor c) {
        return new Course(c.getString(ImportLvaTask.COLUMN_LVA_TERM),
                c.getString(ImportLvaTask.COLUMN_LVA_LVANR),
                c.getString(ImportLvaTask.COLUMN_LVA_TITLE),
                c.getInt(ImportLvaTask.COLUMN_LVA_SKZ),
                c.getString(ImportLvaTask.COLUMN_LVA_TEACHER),
                c.getDouble(ImportLvaTask.COLUMN_LVA_SWS),
                c.getDouble(ImportLvaTask.COLUMN_LVA_ECTS),
                c.getString(ImportLvaTask.COLUMN_LVA_TYPE),
                c.getString(ImportLvaTask.COLUMN_LVA_CODE));
    }

    public static ContentValues getLvaContentValues(Course course) {
        ContentValues cv = new ContentValues();
        cv.put(KusssContentContract.Lva.LVA_COL_TITLE, course.getTitle());
        cv.put(KusssContentContract.Lva.LVA_COL_ECTS, course.getEcts());
        cv.put(KusssContentContract.Lva.LVA_COL_SWS, course.getSws());
        cv.put(KusssContentContract.Lva.LVA_COL_LVANR, course.getLvaNr());
        cv.put(KusssContentContract.Lva.LVA_COL_SKZ, course.getSKZ());
        cv.put(KusssContentContract.Lva.LVA_COL_CODE, course.getCode());
        cv.put(KusssContentContract.Lva.LVA_COL_TEACHER, course.getTeacher());
        cv.put(KusssContentContract.Lva.LVA_COL_TERM, course.getTerm());
        cv.put(KusssContentContract.Lva.LVA_COL_TYPE, course.getLvaType());

        return cv;
    }

    public static Exam createExam(Cursor c) {
        return new Exam(
                c.getString(ImportExamTask.COLUMN_EXAM_LVANR),
                c.getString(ImportExamTask.COLUMN_EXAM_TERM),
                new Date(c.getLong(ImportExamTask.COLUMN_EXAM_DTSTART)),
                new Date(c.getLong(ImportExamTask.COLUMN_EXAM_DTEND)),
                c.getString(ImportExamTask.COLUMN_EXAM_LOCATION),
                c.getString(ImportExamTask.COLUMN_EXAM_DESCRIPTION),
                c.getString(ImportExamTask.COLUMN_EXAM_INFO),
                c.getString(ImportExamTask.COLUMN_EXAM_TITLE),
                KusssDatabaseHelper.toBool(c.getInt(ImportExamTask.COLUMN_EXAM_IS_REGISTERED)));


    }

    public static ContentValues getExamContentValues(Exam exam) {
        ContentValues cv = new ContentValues();
        cv.put(KusssContentContract.Exam.EXAM_COL_DTSTART, exam.getDtStart().getTime());
        cv.put(KusssContentContract.Exam.EXAM_COL_DESCRIPTION, exam.getDescription());
        cv.put(KusssContentContract.Exam.EXAM_COL_INFO, exam.getInfo());
        cv.put(KusssContentContract.Exam.EXAM_COL_LOCATION, exam.getLocation());
        cv.put(KusssContentContract.Exam.EXAM_COL_LVANR, exam.getLvaNr());
        cv.put(KusssContentContract.Exam.EXAM_COL_TERM, exam.getTerm());
        cv.put(KusssContentContract.Exam.EXAM_COL_DTEND, exam.getDtEnd().getTime());
        cv.put(KusssContentContract.Exam.EXAM_COL_IS_REGISTERED,
                KusssDatabaseHelper.toInt(exam.isRegistered()));
        cv.put(KusssContentContract.Exam.EXAM_COL_TITLE, exam.getTitle());
        return cv;
    }

    public static Curricula createStudies(Cursor c) {
        return new Curricula(KusssDatabaseHelper.toBool(c.getInt(ImportStudiesTask.COLUMN_STUDIES_IS_STD)),
                c.getString(ImportStudiesTask.COLUMN_STUDIES_SKZ),
                c.getString(ImportStudiesTask.COLUMN_STUDIES_TITLE),
                KusssDatabaseHelper.toBool(c.getInt(ImportStudiesTask.COLUMN_STUDIES_STEOP_DONE)),
                KusssDatabaseHelper.toBool(c.getInt(ImportStudiesTask.COLUMN_STUDIES_ACTIVE_STATE)),
                c.getString(ImportStudiesTask.COLUMN_STUDIES_UNI),
                new Date(c.getLong(ImportStudiesTask.COLUMN_STUDIES_DT_START)),
                !c.isNull(ImportStudiesTask.COLUMN_STUDIES_DT_END) ? new Date(c.getLong(ImportStudiesTask.COLUMN_STUDIES_DT_END)) : null);
    }

    public static ContentValues getStudiesContentValues(Curricula curricula) {
        ContentValues cv = new ContentValues();
        cv.put(KusssContentContract.Studies.COL_IS_STD, KusssDatabaseHelper.toInt(curricula.isStandard()));
        cv.put(KusssContentContract.Studies.COL_SKZ, curricula.getSkz());
        cv.put(KusssContentContract.Studies.COL_TITLE, curricula.getTitle());
        cv.put(KusssContentContract.Studies.COL_STEOP_DONE, KusssDatabaseHelper.toInt(curricula.isSteopDone()));
        cv.put(KusssContentContract.Studies.COL_ACTIVE_STATE, KusssDatabaseHelper.toInt(curricula.isActive()));
        cv.put(KusssContentContract.Studies.COL_UNI, curricula.getUni());
        cv.put(KusssContentContract.Studies.COL_DT_START, curricula.getDtStart().getTime());

        Date date = curricula.getDtEnd();
        if (date != null) {
            cv.put(KusssContentContract.Studies.COL_DT_END, date.getTime());
        } else {
            cv.putNull(KusssContentContract.Studies.COL_DT_END);
        }

        return cv;
    }


    public static Assessment createGrade(Cursor c) {
        return new Assessment(
                AssessmentType.parseGradeType(c.getInt(ImportGradeTask.COLUMN_GRADE_TYPE)),
                new Date(c.getLong(ImportGradeTask.COLUMN_GRADE_DATE)),
                c.getString(ImportGradeTask.COLUMN_GRADE_LVANR),
                c.getString(ImportGradeTask.COLUMN_GRADE_TERM),
                Grade.parseGradeType(c.getInt(ImportGradeTask.COLUMN_GRADE_GRADE)),
                c.getInt(ImportGradeTask.COLUMN_GRADE_SKZ),
                c.getString(ImportGradeTask.COLUMN_GRADE_TITLE),
                c.getString(ImportGradeTask.COLUMN_GRADE_CODE),
                c.getDouble(ImportGradeTask.COLUMN_GRADE_ECTS),
                c.getDouble(ImportGradeTask.COLUMN_GRADE_SWS));
    }


    public static ContentValues getGradeContentValues(Assessment grade) {
        ContentValues cv = new ContentValues();
        cv.put(KusssContentContract.Grade.GRADE_COL_DATE, grade.getDate().getTime());
        cv.put(KusssContentContract.Grade.GRADE_COL_GRADE, grade.getGrade().ordinal());
        cv.put(KusssContentContract.Grade.GRADE_COL_LVANR, grade.getLvaNr());
        cv.put(KusssContentContract.Grade.GRADE_COL_SKZ, grade.getSkz());
        cv.put(KusssContentContract.Grade.GRADE_COL_TERM, grade.getTerm());
        cv.put(KusssContentContract.Grade.GRADE_COL_TYPE, grade.getAssessmentType().ordinal());
        cv.put(KusssContentContract.Grade.GRADE_COL_CODE, grade.getCode());
        cv.put(KusssContentContract.Grade.GRADE_COL_TITLE, grade.getTitle());
        cv.put(KusssContentContract.Grade.GRADE_COL_ECTS, grade.getEcts());
        cv.put(KusssContentContract.Grade.GRADE_COL_SWS, grade.getSws());
        return cv;
    }
}
