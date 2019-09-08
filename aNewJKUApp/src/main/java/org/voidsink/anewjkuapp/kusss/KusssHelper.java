/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2019 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */


package org.voidsink.anewjkuapp.kusss;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class KusssHelper {

    private static final String URL_GET_EXAMS = "https://www.kusss.jku.at/kusss/szexaminationlist.action";
    private static final DateFormat dateFormat = DateFormat.getDateTimeInstance();

    public static void showExamInBrowser(Context context, String courseId) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_GET_EXAMS));
        context.startActivity(intent);
        // TODO: create activity with webview that uses stored credentials to login and open page with search for courseId
    }

    public static String getCourseKey(Term term, String courseId) {
        return term.toString() + "-" + courseId;
    }

    public static Course createCourse(Cursor c) throws ParseException {
        return new Course(Term.parseTerm(c.getString(KusssContentContract.Course.DB.COL_TERM)),
                c.getString(KusssContentContract.Course.DB.COL_COURSEID),
                c.getString(KusssContentContract.Course.DB.COL_TITLE),
                c.getInt(KusssContentContract.Course.DB.COL_CURRICULA_ID),
                c.getString(KusssContentContract.Course.DB.COL_TEACHER),
                c.getDouble(KusssContentContract.Course.DB.COL_SWS),
                c.getDouble(KusssContentContract.Course.DB.COL_ECTS),
                c.getString(KusssContentContract.Course.DB.COL_TYPE),
                c.getString(KusssContentContract.Course.DB.COL_CODE));
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
        cv.put(KusssContentContract.Course.COL_TERM, AppUtils.termToString(course.getTerm()));
        cv.put(KusssContentContract.Course.COL_LVATYPE, course.getLvaType());

        return cv;
    }

    public static String getExamKey(String courseId, String term, long date) {
        return String.format(Locale.GERMAN, "%s-%s-%d", courseId, term, date);
    }


    public static Exam createExam(Cursor c) throws ParseException {
        return new Exam(
                c.getString(KusssContentContract.Exam.DB.COL_COURSEID),
                Term.parseTerm(c.getString(KusssContentContract.Exam.DB.COL_TERM)),
                new Date(c.getLong(KusssContentContract.Exam.DB.COL_DTSTART)),
                new Date(c.getLong(KusssContentContract.Exam.DB.COL_DTEND)),
                c.getString(KusssContentContract.Exam.DB.COL_LOCATION),
                c.getString(KusssContentContract.Exam.DB.COL_DESCRIPTION),
                c.getString(KusssContentContract.Exam.DB.COL_INFO),
                c.getString(KusssContentContract.Exam.DB.COL_TITLE),
                KusssDatabaseHelper.toBool(c.getInt(KusssContentContract.Exam.DB.COL_IS_REGISTERED)));
    }

    public static ContentValues getExamContentValues(Exam exam) {
        ContentValues cv = new ContentValues();
        cv.put(KusssContentContract.Exam.COL_DTSTART, exam.getDtStart().getTime());
        cv.put(KusssContentContract.Exam.COL_DESCRIPTION, exam.getDescription());
        cv.put(KusssContentContract.Exam.COL_INFO, exam.getInfo());
        cv.put(KusssContentContract.Exam.COL_LOCATION, exam.getLocation());
        cv.put(KusssContentContract.Exam.COL_COURSEID, exam.getCourseId());
        cv.put(KusssContentContract.Exam.COL_TERM, AppUtils.termToString(exam.getTerm()));
        cv.put(KusssContentContract.Exam.COL_DTEND, exam.getDtEnd().getTime());
        cv.put(KusssContentContract.Exam.COL_IS_REGISTERED,
                KusssDatabaseHelper.toInt(exam.isRegistered()));
        cv.put(KusssContentContract.Exam.COL_TITLE, exam.getTitle());
        return cv;
    }

    public static String getCurriculumKey(String cid, Date dtStart) {
        return cid + "-" + dateFormat.format(dtStart);
    }

    public static Curriculum createCurricula(Cursor c) {
        return new Curriculum(KusssDatabaseHelper.toBool(c.getInt(KusssContentContract.Curricula.DB.COL_IS_STD)),
                c.getString(KusssContentContract.Curricula.DB.COL_CURRICULUM_ID),
                c.getString(KusssContentContract.Curricula.DB.COL_TITLE),
                KusssDatabaseHelper.toBool(c.getInt(KusssContentContract.Curricula.DB.COL_STEOP_DONE)),
                KusssDatabaseHelper.toBool(c.getInt(KusssContentContract.Curricula.DB.COL_ACTIVE_STATE)),
                c.getString(KusssContentContract.Curricula.DB.COL_UNI),
                new Date(c.getLong(KusssContentContract.Curricula.DB.COL_DT_START)),
                !c.isNull(KusssContentContract.Curricula.DB.COL_DT_END) ? new Date(c.getLong(KusssContentContract.Curricula.DB.COL_DT_END)) : null);
    }

    public static ContentValues getCurriculumContentValues(Curriculum curriculum) {
        ContentValues cv = new ContentValues();
        cv.put(KusssContentContract.Curricula.COL_IS_STD, KusssDatabaseHelper.toInt(curriculum.isStandard()));
        cv.put(KusssContentContract.Curricula.COL_CURRICULUM_ID, curriculum.getCid());
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

    public static String getAssessmentKey(String classCode, String courseId, long date) {
        return String.format(Locale.GERMAN, "%s-%s-%d", classCode, courseId, date);
    }

    public static Assessment createAssessment(Cursor c) throws ParseException {
        String termStr = c.getString(KusssContentContract.Assessment.DB.COL_TERM);

        return new Assessment(
                AssessmentType.parseAssessmentType(c.getInt(KusssContentContract.Assessment.DB.COL_TYPE)),
                new Date(c.getLong(KusssContentContract.Assessment.DB.COL_DATE)),
                c.getString(KusssContentContract.Assessment.DB.COL_COURSEID),
                TextUtils.isEmpty(termStr) ? null : Term.parseTerm(termStr),
                Grade.parseGradeType(c.getInt(KusssContentContract.Assessment.DB.COL_GRADE)),
                c.getInt(KusssContentContract.Assessment.DB.COL_CURRICULA_ID),
                c.getString(KusssContentContract.Assessment.DB.COL_TITLE),
                c.getString(KusssContentContract.Assessment.DB.COL_CODE),
                c.getDouble(KusssContentContract.Assessment.DB.COL_ECTS),
                c.getDouble(KusssContentContract.Assessment.DB.COL_SWS),
                c.getString(KusssContentContract.Assessment.DB.COL_LVATYPE));
    }

    public static ContentValues getAssessmentContentValues(Assessment assessment) {
        ContentValues cv = new ContentValues();
        cv.put(KusssContentContract.Assessment.COL_DATE, assessment.getDate().getTime());
        cv.put(KusssContentContract.Assessment.COL_GRADE, assessment.getGrade().ordinal());
        cv.put(KusssContentContract.Assessment.COL_COURSEID, assessment.getCourseId());
        cv.put(KusssContentContract.Assessment.COL_CURRICULA_ID, assessment.getCid());
        cv.put(KusssContentContract.Assessment.COL_TERM, AppUtils.termToString(assessment.getTerm()));
        cv.put(KusssContentContract.Assessment.COL_TYPE, assessment.getAssessmentType().ordinal());
        cv.put(KusssContentContract.Assessment.COL_CODE, assessment.getCode());
        cv.put(KusssContentContract.Assessment.COL_TITLE, assessment.getTitle());
        cv.put(KusssContentContract.Assessment.COL_ECTS, assessment.getEcts());
        cv.put(KusssContentContract.Assessment.COL_SWS, assessment.getSws());
        cv.put(KusssContentContract.Assessment.COL_LVATYPE, assessment.getLvaType());
        return cv;
    }

}


