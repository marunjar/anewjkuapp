package org.voidsink.anewjkuapp.provider;

import org.voidsink.anewjkuapp.Analytics;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PoiContentContract;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class KusssDatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = KusssDatabaseHelper.class.getSimpleName();

	private static final String DATABASE_NAME = "kusss.db";
	private static final int DATABASE_VERSION = 9;

	// Database creation sql statement
	public static final String DB_CREATE_LVA = "create table if not exists "
			+ KusssContentContract.Lva.LVA_TABLE_NAME + "("
			+ KusssContentContract.Lva.LVA_COL_ID
			+ " integer primary key autoincrement not null, "
			+ KusssContentContract.Lva.LVA_COL_TERM + " text not null, "
			+ KusssContentContract.Lva.LVA_COL_LVANR + " text not null, "
			+ KusssContentContract.Lva.LVA_COL_CODE + " text not null, "
			+ KusssContentContract.Lva.LVA_COL_TITLE + " text not null, "
			+ KusssContentContract.Lva.LVA_COL_TYPE + " integer not null, "
			+ KusssContentContract.Lva.LVA_COL_TEACHER + " text, "
			+ KusssContentContract.Lva.LVA_COL_SKZ + " integer, "
			+ KusssContentContract.Lva.LVA_COL_ECTS + " real, "
			+ KusssContentContract.Lva.LVA_COL_SWS + " real" + ");";

	public static final String DB_CREATE_EXAM = "create table if not exists "
			+ KusssContentContract.Exam.EXAM_TABLE_NAME + "("
			+ KusssContentContract.Exam.EXAM_COL_ID
			+ " integer primary key autoincrement not null, "
			+ KusssContentContract.Exam.EXAM_COL_TERM + " text not null, "
			+ KusssContentContract.Exam.EXAM_COL_LVANR + " text not null, "
			+ KusssContentContract.Exam.EXAM_COL_DATE + " integer not null, "
			+ KusssContentContract.Exam.EXAM_COL_TIME + " text not null, "
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
			+ KusssContentContract.Grade.GRADE_COL_LVANR + " text, "
			+ KusssContentContract.Grade.GRADE_COL_DATE + " integer not null, "
			+ KusssContentContract.Grade.GRADE_COL_SKZ + " integer, "
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
		onCreate(db);
	}

	public static void drop(Context context) {
		context.deleteDatabase(DATABASE_NAME);
	}

	public static boolean toBool(int integer) {
		return (integer == 1) ? true : false;
	}

	public static int toInt(boolean bool) {
		return (bool) ? 1 : 0;
	}

	public void dropUserData() {
		try {
			SQLiteDatabase db = this.getWritableDatabase();

			db.execSQL("DROP TABLE IF EXISTS "
					+ KusssContentContract.Grade.GRADE_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "
					+ KusssContentContract.Exam.EXAM_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "
					+ KusssContentContract.Lva.LVA_TABLE_NAME);
		} catch (Exception e) {
			Analytics.sendException(mContext, e, true);
			Log.e(TAG, "dropUserData", e);
		}
	}
}
