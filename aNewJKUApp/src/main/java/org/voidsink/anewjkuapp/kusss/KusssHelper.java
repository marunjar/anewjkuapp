package org.voidsink.anewjkuapp.kusss;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;
import org.voidsink.anewjkuapp.update.ImportAssessmentTask;
import org.voidsink.anewjkuapp.update.ImportCurriculaTask;
import org.voidsink.anewjkuapp.update.ImportExamTask;
import org.voidsink.anewjkuapp.update.ImportCourseTask;

import java.util.Date;

public class KusssHelper {

    private static final String URL_GET_EXAMS = "https://www.kusss.jku.at/kusss/szexaminationlist.action";

    public static void showExamInBrowser(Context context, String courseId) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_GET_EXAMS));
        context.startActivity(intent);
        // TODO: create activity with webview that uses stored credentials to login and open page with search for courseId
    }

    public static String getCourseKey(String term, String courseId) {
        return term + "-" + courseId;
    }

    public static Course createCourse(Cursor c) {
        return new Course(c.getString(ImportCourseTask.COLUMN_LVA_TERM),
                c.getString(ImportCourseTask.COLUMN_LVA_COURSEID),
                c.getString(ImportCourseTask.COLUMN_LVA_TITLE),
                c.getInt(ImportCourseTask.COLUMN_LVA_CURRICULA_ID),
                c.getString(ImportCourseTask.COLUMN_LVA_TEACHER),
                c.getDouble(ImportCourseTask.COLUMN_LVA_SWS),
                c.getDouble(ImportCourseTask.COLUMN_LVA_ECTS),
                c.getString(ImportCourseTask.COLUMN_LVA_TYPE),
                c.getString(ImportCourseTask.COLUMN_LVA_CODE));
    }

    public static ContentValues getLvaContentValues(Course course) {
        ContentValues cv = new ContentValues();
        cv.put(KusssContentContract.Course.COL_TITLE, course.getTitle());
        cv.put(KusssContentContract.Course.COL_ECTS, course.getEcts());
        cv.put(KusssContentContract.Course.COL_SWS, course.getSws());
        cv.put(KusssContentContract.Course.COL_COURSEID, course.getCourseId());
        cv.put(KusssContentContract.Course.COL_CURRICULA_ID, course.getCid());
        cv.put(KusssContentContract.Course.COL_CLASS_CODE, course.getCode());
        cv.put(KusssContentContract.Course.COL_LECTURER, course.getTeacher());
        cv.put(KusssContentContract.Course.COL_TERM, course.getTerm());
        cv.put(KusssContentContract.Course.COL_TYPE, course.getLvaType());

        return cv;
    }

    public static Exam createExam(Cursor c) {
        return new Exam(
                c.getString(ImportExamTask.COLUMN_EXAM_COURSEID),
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
        cv.put(KusssContentContract.Exam.COL_DTSTART, exam.getDtStart().getTime());
        cv.put(KusssContentContract.Exam.COL_DESCRIPTION, exam.getDescription());
        cv.put(KusssContentContract.Exam.COL_INFO, exam.getInfo());
        cv.put(KusssContentContract.Exam.COL_LOCATION, exam.getLocation());
        cv.put(KusssContentContract.Exam.COL_COURSEID, exam.getCourseId());
        cv.put(KusssContentContract.Exam.COL_TERM, exam.getTerm());
        cv.put(KusssContentContract.Exam.COL_DTEND, exam.getDtEnd().getTime());
        cv.put(KusssContentContract.Exam.COL_IS_REGISTERED,
                KusssDatabaseHelper.toInt(exam.isRegistered()));
        cv.put(KusssContentContract.Exam.COL_TITLE, exam.getTitle());
        return cv;
    }

    public static Curriculum createCurricula(Cursor c) {
        return new Curriculum(KusssDatabaseHelper.toBool(c.getInt(ImportCurriculaTask.COLUMN_STUDIES_IS_STD)),
                c.getString(ImportCurriculaTask.COLUMN_STUDIES_CURRICULA_ID),
                c.getString(ImportCurriculaTask.COLUMN_STUDIES_TITLE),
                KusssDatabaseHelper.toBool(c.getInt(ImportCurriculaTask.COLUMN_STUDIES_STEOP_DONE)),
                KusssDatabaseHelper.toBool(c.getInt(ImportCurriculaTask.COLUMN_STUDIES_ACTIVE_STATE)),
                c.getString(ImportCurriculaTask.COLUMN_STUDIES_UNI),
                new Date(c.getLong(ImportCurriculaTask.COLUMN_STUDIES_DT_START)),
                !c.isNull(ImportCurriculaTask.COLUMN_STUDIES_DT_END) ? new Date(c.getLong(ImportCurriculaTask.COLUMN_STUDIES_DT_END)) : null);
    }

    public static ContentValues getStudiesContentValues(Curriculum curriculum) {
        ContentValues cv = new ContentValues();
        cv.put(KusssContentContract.Curricula.COL_IS_STD, KusssDatabaseHelper.toInt(curriculum.isStandard()));
        cv.put(KusssContentContract.Curricula.COL_CURRICULA_ID, curriculum.getCid());
        cv.put(KusssContentContract.Curricula.COL_TITLE, curriculum.getTitle());
        cv.put(KusssContentContract.Curricula.COL_STEOP_DONE, KusssDatabaseHelper.toInt(curriculum.isSteopDone()));
        cv.put(KusssContentContract.Curricula.COL_ACTIVE_STATE, KusssDatabaseHelper.toInt(curriculum.isActive()));
        cv.put(KusssContentContract.Curricula.COL_UNI, curriculum.getUni());
        cv.put(KusssContentContract.Curricula.COL_DT_START, curriculum.getDtStart().getTime());

        Date date = curriculum.getDtEnd();
        if (date != null) {
            cv.put(KusssContentContract.Curricula.COL_DT_END, date.getTime());
        } else {
            cv.putNull(KusssContentContract.Curricula.COL_DT_END);
        }

        return cv;
    }


    public static Assessment createGrade(Cursor c) {
        return new Assessment(
                AssessmentType.parseAssessmentType(c.getInt(ImportAssessmentTask.COLUMN_ASSESSMENT_TYPE)),
                new Date(c.getLong(ImportAssessmentTask.COLUMN_ASSESSMENT_DATE)),
                c.getString(ImportAssessmentTask.COLUMN_ASSESSMENT_COURSEID),
                c.getString(ImportAssessmentTask.COLUMN_ASSESSMENT_TERM),
                Grade.parseGradeType(c.getInt(ImportAssessmentTask.COLUMN_ASSESSMENT_GRADE)),
                c.getInt(ImportAssessmentTask.COLUMN_ASSESSMENT_CURRICULA_ID),
                c.getString(ImportAssessmentTask.COLUMN_ASSESSMENT_TITLE),
                c.getString(ImportAssessmentTask.COLUMN_ASSESSMENT_CODE),
                c.getDouble(ImportAssessmentTask.COLUMN_ASSESSMENT_ECTS),
                c.getDouble(ImportAssessmentTask.COLUMN_ASSESSMENT_SWS));
    }


    public static ContentValues getAssessmentContentValues(Assessment assessment) {
        ContentValues cv = new ContentValues();
        cv.put(KusssContentContract.Assessment.COL_DATE, assessment.getDate().getTime());
        cv.put(KusssContentContract.Assessment.COL_GRADE, assessment.getGrade().ordinal());
        cv.put(KusssContentContract.Assessment.COL_COURSEID, assessment.getCourseId());
        cv.put(KusssContentContract.Assessment.COL_CURRICULA_ID, assessment.getCid());
        cv.put(KusssContentContract.Assessment.COL_TERM, assessment.getTerm());
        cv.put(KusssContentContract.Assessment.COL_TYPE, assessment.getAssessmentType().ordinal());
        cv.put(KusssContentContract.Assessment.COL_CODE, assessment.getCode());
        cv.put(KusssContentContract.Assessment.COL_TITLE, assessment.getTitle());
        cv.put(KusssContentContract.Assessment.COL_ECTS, assessment.getEcts());
        cv.put(KusssContentContract.Assessment.COL_SWS, assessment.getSws());
        return cv;
    }

}


