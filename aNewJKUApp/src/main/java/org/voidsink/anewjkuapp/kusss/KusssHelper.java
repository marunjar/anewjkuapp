package org.voidsink.anewjkuapp.kusss;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;
import org.voidsink.anewjkuapp.update.ImportExamTask;
import org.voidsink.anewjkuapp.update.ImportLvaTask;

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

    public static Lva createLva(Cursor c) {
        return new Lva(c.getString(ImportLvaTask.COLUMN_LVA_TERM),
                c.getString(ImportLvaTask.COLUMN_LVA_LVANR),
                c.getString(ImportLvaTask.COLUMN_LVA_TITLE),
                c.getInt(ImportLvaTask.COLUMN_LVA_SKZ),
                c.getString(ImportLvaTask.COLUMN_LVA_TEACHER),
                c.getDouble(ImportLvaTask.COLUMN_LVA_SWS),
                c.getDouble(ImportLvaTask.COLUMN_LVA_ECTS),
                c.getString(ImportLvaTask.COLUMN_LVA_TYPE),
                c.getString(ImportLvaTask.COLUMN_LVA_CODE));
    }

    public static ContentValues getLvaContentValues(Lva lva) {
        ContentValues cv = new ContentValues();
        cv.put(KusssContentContract.Lva.LVA_COL_TITLE, lva.getTitle());
        cv.put(KusssContentContract.Lva.LVA_COL_ECTS, lva.getEcts());
        cv.put(KusssContentContract.Lva.LVA_COL_SWS, lva.getSws());
        cv.put(KusssContentContract.Lva.LVA_COL_LVANR, lva.getLvaNr());
        cv.put(KusssContentContract.Lva.LVA_COL_SKZ, lva.getSKZ());
        cv.put(KusssContentContract.Lva.LVA_COL_CODE, lva.getCode());
        cv.put(KusssContentContract.Lva.LVA_COL_TEACHER, lva.getTeacher());
        cv.put(KusssContentContract.Lva.LVA_COL_TERM, lva.getTerm());
        cv.put(KusssContentContract.Lva.LVA_COL_TYPE, lva.getLvaType());

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
}
