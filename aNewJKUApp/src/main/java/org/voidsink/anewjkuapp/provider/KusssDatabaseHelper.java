package org.voidsink.anewjkuapp.provider;

import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PoiContentContract;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class KusssDatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = KusssDatabaseHelper.class.getSimpleName();

	private static final String DATABASE_NAME = "kusss.db";
	private static final int DATABASE_VERSION = 11;

	// Database creation sql statement
	public static final String DB_CREATE_LVA = "create table if not exists "
			+ KusssContentContract.Lva.LVA_TABLE_NAME + "("
			+ KusssContentContract.Lva.LVA_COL_ID
			+ " integer primary key autoincrement not null, "
			+ KusssContentContract.Lva.LVA_COL_TERM + " text not null, "
			+ KusssContentContract.Lva.LVA_COL_COURSEID + " text not null, "
			+ KusssContentContract.Lva.LVA_COL_CODE + " text not null, "
			+ KusssContentContract.Lva.LVA_COL_TITLE + " text not null, "
			+ KusssContentContract.Lva.LVA_COL_TYPE + " integer not null, "
			+ KusssContentContract.Lva.LVA_COL_TEACHER + " text, "
			+ KusssContentContract.Lva.LVA_COL_CURRICULA_ID + " integer, "
			+ KusssContentContract.Lva.LVA_COL_ECTS + " real, "
			+ KusssContentContract.Lva.LVA_COL_SWS + " real" + ");";

	public static final String DB_CREATE_EXAM = "create table if not exists "
			+ KusssContentContract.Exam.EXAM_TABLE_NAME + "("
			+ KusssContentContract.Exam.EXAM_COL_ID
			+ " integer primary key autoincrement not null, "
			+ KusssContentContract.Exam.EXAM_COL_TERM + " text not null, "
			+ KusssContentContract.Exam.EXAM_COL_COURSEID + " text not null, "
			+ KusssContentContract.Exam.EXAM_COL_DTSTART + " integer not null, "
			+ KusssContentContract.Exam.EXAM_COL_DTEND + " integer not null, "
			+ KusssContentContract.Exam.EXAM_COL_LOCATION + " text, "
			+ KusssContentContract.Exam.EXAM_COL_DESCRIPTION + " text, "
			+ KusssContentContract.Exam.EXAM_COL_INFO + " text, "
			+ KusssContentContract.Exam.EXAM_COL_IS_REGISTERED + " integer, "
			+ KusssContentContract.Exam.EXAM_COL_TITLE + " text" + ");";

	public static final String DB_CREATE_GRADE = "create table if not exists "
			+ KusssContentContract.Grade.GRADE_TABLE_NAME + "("
			+ KusssContentContract.Grade.GRADE_COL_ID
			+ " integer primary key autoincrement not null, "
			+ KusssContentContract.Grade.GRADE_COL_TERM + " text, "
			+ KusssContentContract.Grade.GRADE_COL_COURSEID + " text, "
			+ KusssContentContract.Grade.GRADE_COL_DATE + " integer not null, "
			+ KusssContentContract.Grade.GRADE_COL_CURRICULA_ID + " integer, "
			+ KusssContentContract.Grade.GRADE_COL_GRADE
			+ " integer not null, " + KusssContentContract.Grade.GRADE_COL_ECTS
			+ " real, " + KusssContentContract.Grade.GRADE_COL_SWS + " real, "
			+ KusssContentContract.Grade.GRADE_COL_CODE + " text not null, "
			+ KusssContentContract.Grade.GRADE_COL_TITLE + " text, "
			+ KusssContentContract.Grade.GRADE_COL_TYPE + " integer" + ");";

	public static final String DB_CREATE_POI = "create virtual table "
			+ PoiContentContract.Poi.TABLE_NAME + " using "
			+ PoiContentContract.getFTS() + " ("
			+ PoiContentContract.Poi.COL_NAME + " text primary key not null, "
			+ PoiContentContract.Poi.COL_LAT + " real not null, "
			+ PoiContentContract.Poi.COL_LON + " real not null, "
			+ PoiContentContract.Poi.COL_IS_DEFAULT + " integer, "
			+ PoiContentContract.Poi.COL_ADR_CITY + " text, "
			+ PoiContentContract.Poi.COL_ADR_COUNTRY + " text, "
			+ PoiContentContract.Poi.COL_ADR_POSTAL_CODE + " integer, "
			+ PoiContentContract.Poi.COL_ADR_STATE + " text, "
			+ PoiContentContract.Poi.COL_ADR_STREET + " text, "
			+ PoiContentContract.Poi.COL_DESCR + " text " + ");";

    public static final String DB_CREATE_STUDIES = "create table if not exists "
            + KusssContentContract.Studies.TABLE_NAME + "("
            + KusssContentContract.Studies.COL_ID
            + " integer primary key autoincrement not null, "
            + KusssContentContract.Studies.COL_IS_STD + " integer, "
            + KusssContentContract.Studies.COL_CURRICULA_ID + " integer, "
            + KusssContentContract.Studies.COL_TITLE + " text, "
            + KusssContentContract.Studies.COL_STEOP_DONE + " integer, "
            + KusssContentContract.Studies.COL_ACTIVE_STATE + " integer, "
            + KusssContentContract.Studies.COL_UNI + " string, "
            + KusssContentContract.Studies.COL_DT_START + " integer not null, "
            + KusssContentContract.Studies.COL_DT_END + " integer" + ")";

	private Context mContext = null;

	public KusssDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "Create database");
		db.execSQL(DB_CREATE_LVA);
		db.execSQL(DB_CREATE_EXAM);
		db.execSQL(DB_CREATE_GRADE);
        db.execSQL(DB_CREATE_STUDIES);

		db.execSQL("DROP TABLE IF EXISTS " + PoiContentContract.Poi.TABLE_NAME);
		db.execSQL(DB_CREATE_POI);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy most of all old data");
		if (oldVersion < 9) {
			db.execSQL("DROP TABLE IF EXISTS "
					+ KusssContentContract.Grade.GRADE_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "
					+ KusssContentContract.Exam.EXAM_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "
					+ KusssContentContract.Lva.LVA_TABLE_NAME);
		}
        if (oldVersion < 10) {
            // try to import studies
        }
        if (oldVersion < 11) {
            db.execSQL("DROP TABLE IF EXISTS "
                    + KusssContentContract.Exam.EXAM_TABLE_NAME);
        }
		onCreate(db);
	}

	public static void drop(Context context) {
		try {
            context.deleteDatabase(DATABASE_NAME);
        } catch (Exception e) {
            Analytics.sendException(context, e, true);
        }
	}

	public static boolean toBool(int integer) {
		return (integer == 1) ? true : false;
	}

	public static int toInt(boolean bool) {
		return (bool) ? 1 : 0;
	}

	public static void dropUserData(Context context) {
		try {
            KusssDatabaseHelper mDbHelper = new KusssDatabaseHelper(context);

            SQLiteDatabase db = mDbHelper.getWritableDatabase();

			db.execSQL("DROP TABLE IF EXISTS "
					+ KusssContentContract.Grade.GRADE_TABLE_NAME);
            db.execSQL(DB_CREATE_GRADE);

			db.execSQL("DROP TABLE IF EXISTS "
					+ KusssContentContract.Exam.EXAM_TABLE_NAME);
            db.execSQL(DB_CREATE_EXAM);

			db.execSQL("DROP TABLE IF EXISTS "
					+ KusssContentContract.Lva.LVA_TABLE_NAME);
            db.execSQL(DB_CREATE_LVA);

            db.execSQL("DROP TABLE IF EXISTS "
                    + KusssContentContract.Studies.TABLE_NAME);
            db.execSQL(DB_CREATE_STUDIES);
		} catch (Exception e) {
            Analytics.sendException(context, e, true);
            drop(context);
		}
	}
}
