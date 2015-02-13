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

import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.update.ImportGradeTask;
import org.voidsink.anewjkuapp.update.ImportLvaTask;
import org.voidsink.anewjkuapp.update.ImportStudiesTask;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.kusss.Studies;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class KusssContentProvider extends ContentProvider {

    private static final int CODE_LVA = 1;
    private static final int CODE_LVA_ID = 2;
    private static final int CODE_EXAM = 3;
    private static final int CODE_EXAM_ID = 4;
    private static final int CODE_GRADE = 5;
    private static final int CODE_GRADE_ID = 6;
    private static final int CODE_STUDIES = 7;
    private static final int CODE_STUDIES_ID = 8;

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
                KusssContentContract.Lva.PATH, CODE_LVA);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Lva.PATH + "/#", CODE_LVA_ID);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Exam.PATH, CODE_EXAM);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Exam.PATH + "/#", CODE_EXAM_ID);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Grade.PATH, CODE_GRADE);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Grade.PATH + "/#", CODE_GRADE_ID);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Studies.PATH, CODE_STUDIES);
        sUriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Studies.PATH + "/#", CODE_STUDIES_ID);
    }

    private KusssDatabaseHelper mDbHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String whereIdClause = "";
        int rowsDeleted = -1;
        switch (sUriMatcher.match(uri)) {
            case CODE_LVA:
                rowsDeleted = db.delete(KusssContentContract.Lva.LVA_TABLE_NAME,
                        selection, selectionArgs);
                break;
            case CODE_EXAM:
                rowsDeleted = db.delete(KusssContentContract.Exam.EXAM_TABLE_NAME,
                        selection, selectionArgs);
                break;
            case CODE_GRADE:
                rowsDeleted = db.delete(
                        KusssContentContract.Grade.GRADE_TABLE_NAME, selection,
                        selectionArgs);
                break;
            case CODE_STUDIES:
                rowsDeleted = db.delete(
                        KusssContentContract.Studies.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case CODE_LVA_ID:
                whereIdClause = KusssContentContract.Lva.LVA_COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                rowsDeleted = db.delete(KusssContentContract.Lva.LVA_TABLE_NAME,
                        whereIdClause, selectionArgs);
                break;
            case CODE_EXAM_ID:
                whereIdClause = KusssContentContract.Exam.EXAM_COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                rowsDeleted = db.delete(KusssContentContract.Exam.EXAM_TABLE_NAME,
                        whereIdClause, selectionArgs);
                break;
            case CODE_GRADE_ID:
                whereIdClause = KusssContentContract.Grade.GRADE_COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                rowsDeleted = db.delete(
                        KusssContentContract.Grade.GRADE_TABLE_NAME, whereIdClause,
                        selectionArgs);
                break;
            case CODE_STUDIES_ID:
                whereIdClause = KusssContentContract.Studies.COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                rowsDeleted = db.delete(
                        KusssContentContract.Studies.TABLE_NAME, whereIdClause,
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
            case CODE_LVA:
                return KusssContentContract.CONTENT_TYPE_DIR + "/"
                        + KusssContentContract.Lva.PATH;
            case CODE_LVA_ID:
                return KusssContentContract.CONTENT_TYPE_ITEM + "/"
                        + KusssContentContract.Lva.PATH;
            case CODE_EXAM:
                return KusssContentContract.CONTENT_TYPE_DIR + "/"
                        + KusssContentContract.Exam.PATH;
            case CODE_EXAM_ID:
                return KusssContentContract.CONTENT_TYPE_ITEM + "/"
                        + KusssContentContract.Exam.PATH;
            case CODE_GRADE:
                return KusssContentContract.CONTENT_TYPE_DIR + "/"
                        + KusssContentContract.Grade.PATH;
            case CODE_GRADE_ID:
                return KusssContentContract.CONTENT_TYPE_ITEM + "/"
                        + KusssContentContract.Grade.PATH;
            case CODE_STUDIES:
                return KusssContentContract.CONTENT_TYPE_DIR + "/"
                        + KusssContentContract.Studies.PATH;
            case CODE_STUDIES_ID:
                return KusssContentContract.CONTENT_TYPE_ITEM + "/"
                        + KusssContentContract.Studies.PATH;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case CODE_LVA: {
                long id = db.insert(KusssContentContract.Lva.LVA_TABLE_NAME, null,
                        values);
                if (id != -1)
                    getContext().getContentResolver().notifyChange(uri, null);
                return KusssContentContract.Lva.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(id)).build();
            }
            case CODE_EXAM: {
                long id = db.insert(KusssContentContract.Exam.EXAM_TABLE_NAME,
                        null, values);
                if (id != -1)
                    getContext().getContentResolver().notifyChange(uri, null);
                return KusssContentContract.Exam.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(id)).build();
            }
            case CODE_GRADE: {
                long id = db.insert(KusssContentContract.Grade.GRADE_TABLE_NAME,
                        null, values);
                if (id != -1)
                    getContext().getContentResolver().notifyChange(uri, null);
                return KusssContentContract.Grade.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(id)).build();
            }
            case CODE_STUDIES: {
                long id = db.insert(KusssContentContract.Studies.TABLE_NAME,
                        null, values);
                if (id != -1)
                    getContext().getContentResolver().notifyChange(uri, null);
                return KusssContentContract.Studies.CONTENT_URI.buildUpon()
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
            case CODE_LVA_ID:
                builder.appendWhere(KusssContentContract.Lva.LVA_COL_ID + "="
                        + uri.getLastPathSegment());
            case CODE_LVA:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = KusssContentContract.Lva.LVA_COL_ID + " ASC";
                builder.setTables(KusssContentContract.Lva.LVA_TABLE_NAME);
                return builder.query(db, projection, selection, selectionArgs,
                        null, null, sortOrder);
            case CODE_EXAM_ID:
                builder.appendWhere(KusssContentContract.Exam.EXAM_COL_ID + "="
                        + uri.getLastPathSegment());
            case CODE_EXAM:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = KusssContentContract.Exam.EXAM_COL_ID + " ASC";
                builder.setTables(KusssContentContract.Exam.EXAM_TABLE_NAME);
                return builder.query(db, projection, selection, selectionArgs,
                        null, null, sortOrder);
            case CODE_GRADE_ID:
                builder.appendWhere(KusssContentContract.Grade.GRADE_COL_ID + "="
                        + uri.getLastPathSegment());
            case CODE_GRADE:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = KusssContentContract.Grade.GRADE_COL_ID + " ASC";
                builder.setTables(KusssContentContract.Grade.GRADE_TABLE_NAME);
                return builder.query(db, projection, selection, selectionArgs,
                        null, null, sortOrder);
            case CODE_STUDIES_ID:
                builder.appendWhere(KusssContentContract.Studies.COL_ID + "="
                        + uri.getLastPathSegment());
            case CODE_STUDIES:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = KusssContentContract.Studies.COL_ID + " ASC";
                builder.setTables(KusssContentContract.Studies.TABLE_NAME);
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
            case CODE_LVA: {
                return db.update(KusssContentContract.Lva.LVA_TABLE_NAME, values,
                        selection, selectionArgs);
            }
            case CODE_EXAM: {
                return db.update(KusssContentContract.Exam.EXAM_TABLE_NAME, values,
                        selection, selectionArgs);
            }
            case CODE_GRADE: {
                return db.update(KusssContentContract.Grade.GRADE_TABLE_NAME,
                        values, selection, selectionArgs);
            }
            case CODE_STUDIES: {
                return db.update(KusssContentContract.Studies.TABLE_NAME,
                        values, selection, selectionArgs);
            }
            case CODE_LVA_ID: {
                whereIdClause = KusssContentContract.Lva.LVA_COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                return db.update(KusssContentContract.Lva.LVA_TABLE_NAME, values,
                        whereIdClause, selectionArgs);
            }
            case CODE_EXAM_ID: {
                whereIdClause = KusssContentContract.Exam.EXAM_COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                return db.update(KusssContentContract.Exam.EXAM_TABLE_NAME, values,
                        whereIdClause, selectionArgs);
            }
            case CODE_GRADE_ID: {
                whereIdClause = KusssContentContract.Grade.GRADE_COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                return db.update(KusssContentContract.Grade.GRADE_TABLE_NAME,
                        values, whereIdClause, selectionArgs);
            }
            case CODE_STUDIES_ID: {
                whereIdClause = KusssContentContract.Studies.COL_ID + "="
                        + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereIdClause += " AND " + selection;
                return db.update(KusssContentContract.Studies.TABLE_NAME,
                        values, whereIdClause, selectionArgs);
            }
            default:
                throw new IllegalArgumentException("URI " + uri
                        + " is not supported.");
        }
    }

    public static List<ExamGrade> getGrades(Context context) {
        List<ExamGrade> mGrades = new ArrayList<ExamGrade>();

        Account mAccount = AppUtils.getAccount(context);
        if (mAccount != null) {
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(KusssContentContract.Grade.CONTENT_URI,
                    ImportGradeTask.GRADE_PROJECTION, null, null,
                    KusssContentContract.Grade.GRADE_TABLE_NAME + "."
                            + KusssContentContract.Grade.GRADE_COL_TYPE
                            + " ASC,"
                            + KusssContentContract.Grade.GRADE_TABLE_NAME + "."
                            + KusssContentContract.Grade.GRADE_COL_DATE
                            + " DESC");

            if (c != null) {
                while (c.moveToNext()) {
                    mGrades.add(new ExamGrade(c));
                }
                c.close();
            }
            c = null;
        }
        return mGrades;
    }

    public static List<Lva> getLvas(Context context) {
        List<Lva> mLvas = new ArrayList<Lva>();
        Account mAccount = AppUtils.getAccount(context);
        if (mAccount != null) {
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(KusssContentContract.Lva.CONTENT_URI,
                    ImportLvaTask.LVA_PROJECTION, null, null,
                    KusssContentContract.Lva.LVA_COL_TERM + " DESC");

            if (c != null) {
                while (c.moveToNext()) {
                    mLvas.add(new Lva(c));
                }
                c.close();
            }
        }
        return mLvas;
    }

    public static List<Studies> getStudies(Context context) {
        List<Studies> mStudies = new ArrayList<>();
        Account mAccount = AppUtils.getAccount(context);
        if (mAccount != null) {
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(KusssContentContract.Studies.CONTENT_URI,
                    ImportStudiesTask.STUDIES_PROJECTION, null, null,
                    KusssContentContract.Studies.COL_DT_START + " DESC");

            if (c != null) {
                while (c.moveToNext()) {
                    mStudies.add(new Studies(c));
                }
                c.close();
            }
        }
        AppUtils.sortStudies(mStudies);

        return mStudies;
    }

    public static List<Term> getTerms(Context context) {
        List<String> terms = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        List<Studies> studies = getStudies(context);

        if (studies == null) {
            studies = new ArrayList<>();
        }

        if (studies.size() == 0) {
            new ImportStudiesTask(AppUtils.getAccount(context), context).execute();

            try {
                List<ExamGrade> grades = getGrades(context);

                Date dtStart = null;

                for (ExamGrade grade : grades) {
                    Date date = grade.getDate();
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

                    studies.add(new Studies(dtStart, null));
                }
            } catch (Exception e) {
                Analytics.sendException(context, e, false);
            }
        }

        // always load current term, subtract -1 term for sure
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, -6);
        studies.add(new Studies(cal.getTime(), null));

        if (studies.size() > 0) {
            // calculate terms from studies duration
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
                if (startSS.before(then) && dateInRange(startSS, studies)) {
                    terms.add(String.format("%dS", year));
                }
                if (startWS.before(then) && dateInRange(startWS, studies)) {
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
        for (String term : terms) {
            objects.add(new Term(term));
        }

        return Collections.unmodifiableList(objects);
    }

    private static boolean dateInRange(Date date, List<Studies> studies) {
        for (Studies studie : studies) {
            if (studie.dateInRange(date)) {
                return true;
            }
        }
        return false;
    }

}
