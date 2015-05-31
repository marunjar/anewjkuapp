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

package org.voidsink.anewjkuapp.provider;

import android.accounts.Account;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.kusss.Curriculum;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.update.ImportAssessmentTask;
import org.voidsink.anewjkuapp.update.ImportCourseTask;
import org.voidsink.anewjkuapp.update.ImportCurriculaTask;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class KusssContentProvider extends ContentProvider {

    private static final int CODE_COURSE = 1;
    private static final int CODE_COURSE_ID = 2;
    private static final int CODE_EXAM = 3;
    private static final int CODE_EXAM_ID = 4;
    private static final int CODE_GRADE = 5;
    private static final int CODE_GRADE_ID = 6;
    private static final int CODE_CURRICULA = 7;
    private static final int CODE_CURRICULA_ID = 8;

    private static final Comparator<String> TermComparator = new Comparator<String>() {
        @Override
        public int compare(String lhs, String rhs) {
            return rhs.compareTo(lhs);
        }
    };

    private static final UriMatcher sUriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);
//	private static final String TAG = KusssContentProvider.class.getSimpleName();

    static {
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Course.PATH, CODE_COURSE);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Course.PATH + "/#", CODE_COURSE_ID);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Exam.PATH, CODE_EXAM);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Exam.PATH + "/#", CODE_EXAM_ID);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Assessment.PATH, CODE_GRADE);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Assessment.PATH + "/#", CODE_GRADE_ID);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Curricula.PATH, CODE_CURRICULA);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Curricula.PATH + "/#", CODE_CURRICULA_ID);
    }

    private KusssDatabaseHelper mDbHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String whereIdClause = "";
        int rowsDeleted = -1;
        switch (sUriMatcher.match(uri)) {
            case CODE_COURSE:
                rowsDeleted = db.delete(KusssContentContract.Course.TABLE_NAME,
                        selection, selectionArgs);
                break;
            case CODE_EXAM:
                rowsDeleted = db.delete(KusssContentContract.Exam.TABLE_NAME,
                        selection, selectionArgs);
                break;
            case CODE_GRADE:
                rowsDeleted = db.delete(
                        KusssContentContract.Assessment.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case CODE_CURRICULA:
                rowsDeleted = db.delete(
                        KusssContentContract.Curricula.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case CODE_COURSE_ID:
                whereIdClause = KusssContentContract.Course.COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                rowsDeleted = db.delete(KusssContentContract.Course.TABLE_NAME,
                        whereIdClause, selectionArgs);
                break;
            case CODE_EXAM_ID:
                whereIdClause = KusssContentContract.Exam.COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                rowsDeleted = db.delete(KusssContentContract.Exam.TABLE_NAME,
                        whereIdClause, selectionArgs);
                break;
            case CODE_GRADE_ID:
                whereIdClause = KusssContentContract.Assessment.COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                rowsDeleted = db.delete(
                        KusssContentContract.Assessment.TABLE_NAME, whereIdClause,
                        selectionArgs);
                break;
            case CODE_CURRICULA_ID:
                whereIdClause = KusssContentContract.Curricula.COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                rowsDeleted = db.delete(
                        KusssContentContract.Curricula.TABLE_NAME, whereIdClause,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // Notifying the changes, if there are any
        if (rowsDeleted != -1)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CODE_COURSE:
                return KusssContentContract.CONTENT_TYPE_DIR + "/"
                        + KusssContentContract.Course.PATH;
            case CODE_COURSE_ID:
                return KusssContentContract.CONTENT_TYPE_ITEM + "/"
                        + KusssContentContract.Course.PATH;
            case CODE_EXAM:
                return KusssContentContract.CONTENT_TYPE_DIR + "/"
                        + KusssContentContract.Exam.PATH;
            case CODE_EXAM_ID:
                return KusssContentContract.CONTENT_TYPE_ITEM + "/"
                        + KusssContentContract.Exam.PATH;
            case CODE_GRADE:
                return KusssContentContract.CONTENT_TYPE_DIR + "/"
                        + KusssContentContract.Assessment.PATH;
            case CODE_GRADE_ID:
                return KusssContentContract.CONTENT_TYPE_ITEM + "/"
                        + KusssContentContract.Assessment.PATH;
            case CODE_CURRICULA:
                return KusssContentContract.CONTENT_TYPE_DIR + "/"
                        + KusssContentContract.Curricula.PATH;
            case CODE_CURRICULA_ID:
                return KusssContentContract.CONTENT_TYPE_ITEM + "/"
                        + KusssContentContract.Curricula.PATH;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case CODE_COURSE: {
                long id = db.insert(KusssContentContract.Course.TABLE_NAME, null,
                        values);
                if (id != -1)
                    getContext().getContentResolver().notifyChange(uri, null);
                return KusssContentContract.Course.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(id)).build();
            }
            case CODE_EXAM: {
                long id = db.insert(KusssContentContract.Exam.TABLE_NAME,
                        null, values);
                if (id != -1)
                    getContext().getContentResolver().notifyChange(uri, null);
                return KusssContentContract.Exam.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(id)).build();
            }
            case CODE_GRADE: {
                long id = db.insert(KusssContentContract.Assessment.TABLE_NAME,
                        null, values);
                if (id != -1)
                    getContext().getContentResolver().notifyChange(uri, null);
                return KusssContentContract.Assessment.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(id)).build();
            }
            case CODE_CURRICULA: {
                long id = db.insert(KusssContentContract.Curricula.TABLE_NAME,
                        null, values);
                if (id != -1)
                    getContext().getContentResolver().notifyChange(uri, null);
                return KusssContentContract.Curricula.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(id)).build();
            }
            default: {
                throw new IllegalArgumentException("Unsupported URI: " + uri);
            }
        }
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new KusssDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

		/*
         * Choose the table to query and a sort order based on the code returned
		 * for the incoming URI. Here, too, only the statements for table 3 are
		 * shown.
		 */
        switch (sUriMatcher.match(uri)) {
            case CODE_COURSE_ID:
                builder.appendWhere(KusssContentContract.Course.COL_ID + "="
                        + uri.getLastPathSegment());
            case CODE_COURSE:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = KusssContentContract.Course.COL_ID + " ASC";
                builder.setTables(KusssContentContract.Course.TABLE_NAME);
                return builder.query(db, projection, selection, selectionArgs,
                        null, null, sortOrder);
            case CODE_EXAM_ID:
                builder.appendWhere(KusssContentContract.Exam.COL_ID + "="
                        + uri.getLastPathSegment());
            case CODE_EXAM:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = KusssContentContract.Exam.COL_ID + " ASC";
                builder.setTables(KusssContentContract.Exam.TABLE_NAME);
                return builder.query(db, projection, selection, selectionArgs,
                        null, null, sortOrder);
            case CODE_GRADE_ID:
                builder.appendWhere(KusssContentContract.Assessment.COL_ID + "="
                        + uri.getLastPathSegment());
            case CODE_GRADE:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = KusssContentContract.Assessment.COL_ID + " ASC";
                builder.setTables(KusssContentContract.Assessment.TABLE_NAME);
                return builder.query(db, projection, selection, selectionArgs,
                        null, null, sortOrder);
            case CODE_CURRICULA_ID:
                builder.appendWhere(KusssContentContract.Curricula.COL_ID + "="
                        + uri.getLastPathSegment());
            case CODE_CURRICULA:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = KusssContentContract.Curricula.COL_ID + " ASC";
                builder.setTables(KusssContentContract.Curricula.TABLE_NAME);
                return builder.query(db, projection, selection, selectionArgs,
                        null, null, sortOrder);
            default:
                throw new IllegalArgumentException("URI " + uri
                        + " is not supported.");
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String whereIdClause = "";

        switch (sUriMatcher.match(uri)) {
            case CODE_COURSE: {
                return db.update(KusssContentContract.Course.TABLE_NAME, values,
                        selection, selectionArgs);
            }
            case CODE_EXAM: {
                return db.update(KusssContentContract.Exam.TABLE_NAME, values,
                        selection, selectionArgs);
            }
            case CODE_GRADE: {
                return db.update(KusssContentContract.Assessment.TABLE_NAME,
                        values, selection, selectionArgs);
            }
            case CODE_CURRICULA: {
                return db.update(KusssContentContract.Curricula.TABLE_NAME,
                        values, selection, selectionArgs);
            }
            case CODE_COURSE_ID: {
                whereIdClause = KusssContentContract.Course.COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                return db.update(KusssContentContract.Course.TABLE_NAME, values,
                        whereIdClause, selectionArgs);
            }
            case CODE_EXAM_ID: {
                whereIdClause = KusssContentContract.Exam.COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                return db.update(KusssContentContract.Exam.TABLE_NAME, values,
                        whereIdClause, selectionArgs);
            }
            case CODE_GRADE_ID: {
                whereIdClause = KusssContentContract.Assessment.COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                return db.update(KusssContentContract.Assessment.TABLE_NAME,
                        values, whereIdClause, selectionArgs);
            }
            case CODE_CURRICULA_ID: {
                whereIdClause = KusssContentContract.Curricula.COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                return db.update(KusssContentContract.Curricula.TABLE_NAME,
                        values, whereIdClause, selectionArgs);
            }
            default:
                throw new IllegalArgumentException("URI " + uri
                        + " is not supported.");
        }
    }

    public static List<Assessment> getAssessmentsFromCursor(Context context, Cursor data) {
        List<Assessment> mAssessments = new ArrayList<>();

        if (data != null) {
            data.moveToFirst();
            try {
                while (data.moveToNext()) {
                    mAssessments.add(KusssHelper.createAssessment(data));
                }
            } catch (ParseException e) {
                Analytics.sendException(context, e, false);
                mAssessments.clear();
            }
        }
        return mAssessments;
    }

    public static List<Assessment> getAssessments(Context context) {
        List<Assessment> mAssessments = new ArrayList<>();

        Account mAccount = AppUtils.getAccount(context);
        if (mAccount != null) {
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(KusssContentContract.Assessment.CONTENT_URI,
                    ImportAssessmentTask.ASSESSMENT_PROJECTION, null, null,
                    KusssContentContract.Assessment.TABLE_NAME + "."
                            + KusssContentContract.Assessment.COL_TYPE
                            + " ASC,"
                            + KusssContentContract.Assessment.TABLE_NAME + "."
                            + KusssContentContract.Assessment.COL_DATE
                            + " DESC");

            if (c != null) {
                mAssessments = getAssessmentsFromCursor(context, c);
                c.close();
            }
        }
        return mAssessments;
    }

    public static List<Course> getCourses(Context context) {
        List<Course> mCourses = new ArrayList<>();
        Account mAccount = AppUtils.getAccount(context);
        if (mAccount != null) {
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(KusssContentContract.Course.CONTENT_URI,
                    ImportCourseTask.COURSE_PROJECTION, null, null,
                    KusssContentContract.Course.COL_TERM + " DESC");

            if (c != null) {
                try {
                    while (c.moveToNext()) {
                        mCourses.add(KusssHelper.createCourse(c));
                    }
                } catch (ParseException e) {
                    Analytics.sendException(context, e, false);
                    mCourses.clear();
                }
                c.close();
            }
        }
        return mCourses;
    }

    public static List<Curriculum> getCurriculaFromCursor(Context context, Cursor c) {
        List<Curriculum> mCurriculum = new ArrayList<>();
        if (c != null) {
            while (c.moveToNext()) {
                mCurriculum.add(KusssHelper.createCurricula(c));
            }
            AppUtils.sortCurricula(mCurriculum);
        }
        return mCurriculum;
    }

    public static List<Curriculum> getCurricula(Context context) {
        List<Curriculum> mCurriculum = new ArrayList<>();
        Account mAccount = AppUtils.getAccount(context);
        if (mAccount != null) {
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(KusssContentContract.Curricula.CONTENT_URI,
                    ImportCurriculaTask.CURRICULA_PROJECTION, null, null,
                    KusssContentContract.Curricula.COL_DT_START + " DESC");

            if (c != null) {
                mCurriculum = getCurriculaFromCursor(context, c);
                c.close();
            }
        }

        return mCurriculum;
    }

    public static List<Term> getTerms(Context context) {
        List<String> terms = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        List<Curriculum> mCurriculum = getCurricula(context);

        if (mCurriculum == null) {
            mCurriculum = new ArrayList<>();
        }

        if (mCurriculum.size() == 0) {
            new ImportCurriculaTask(AppUtils.getAccount(context), context).execute();

            try {
                List<Assessment> assessments = getAssessments(context);

                Date dtStart = null;

                for (Assessment assessment : assessments) {
                    Date date = assessment.getDate();
                    if (date != null) {
                        if (dtStart == null || date.before(dtStart)) {
                            dtStart = date;
                        }
                    }
                }

                if (dtStart != null) {
                    // subtract -1 term for sure
                    cal.setTime(dtStart);
                    cal.add(Calendar.MONTH, -6);
                    dtStart = cal.getTime();

                    mCurriculum.add(new Curriculum(dtStart, null));
                }
            } catch (Exception e) {
                Analytics.sendException(context, e, false);
            }
        }

        // always load current term, subtract -1 term for sure
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, -6);
        mCurriculum.add(new Curriculum(cal.getTime(), null));

        if (mCurriculum.size() > 0) {
            // calculate terms from curricula duration
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, 1);
            Date then = cal.getTime();

            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            int year = 2010;

            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, Calendar.MARCH);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date startSS = cal.getTime(); // 1.3.

            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, Calendar.OCTOBER);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date startWS = cal.getTime(); // 1.10.

            while (startSS.before(then) || startWS.before(then)) {
                if (startSS.before(then) && dateInRange(startSS, mCurriculum)) {
                    terms.add(String.format("%dS", year));
                }
                if (startWS.before(then) && dateInRange(startWS, mCurriculum)) {
                    terms.add(String.format("%dW", year));
                }

                // inc year
                year++;

                cal.setTime(startSS);
                cal.set(Calendar.YEAR, year);
                startSS.setTime(cal.getTimeInMillis());

                cal.setTime(startWS);
                cal.set(Calendar.YEAR, year);
                startWS.setTime(cal.getTimeInMillis());
            }
        }

        if (terms.size() == 0) {
            // get Terms from Data, may take a little bit longer
        }

        Collections.sort(terms, TermComparator);

        List<Term> objects = new ArrayList<>();
        try {
            for (String term : terms) {
                objects.add(Term.parseTerm(term));
            }
        } catch (ParseException e) {
            Analytics.sendException(context, e, true);
            objects.clear();
        }

        return Collections.unmodifiableList(objects);
    }

    private static boolean dateInRange(Date date, List<Curriculum> curricula) {
        for (Curriculum curriculum : curricula) {
            if (curriculum.dateInRange(date)) {
                return true;
            }
        }
        return false;
    }

}
