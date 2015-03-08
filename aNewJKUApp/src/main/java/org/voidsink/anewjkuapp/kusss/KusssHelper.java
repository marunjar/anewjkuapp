package org.voidsink.anewjkuapp.kusss;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import org.voidsink.kussslib.Course;
import org.voidsink.kussslib.Term;

public class KusssHelper {

    private static final String URL_GET_EXAMS = "https://www.kusss.jku.at/kusss/szexaminationlist.action";

    public static void showExamInBrowser(Context context, String lvaNr) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_GET_EXAMS));
        context.startActivity(intent);
        // TODO: create activity with webview that uses stored credentials to login and open page with search for lvanr
    }

    public static Course createCourse(Cursor c) {
        return null;
    }

    public static String getCourseKey(Term term, String courseId) {
        return null;
    }
}
