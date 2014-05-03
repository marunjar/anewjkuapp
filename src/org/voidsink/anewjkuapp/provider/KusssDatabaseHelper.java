package org.voidsink.anewjkuapp.provider;

import org.voidsink.anewjkuapp.KusssContentContract;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class KusssDatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = KusssDatabaseHelper.class.getSimpleName();
	
    private static final String DATABASE_NAME = "kusss.db";
    private static final int DATABASE_VERSION = 2;
    
    // Database creation sql statement
    public static final String DB_CREATE_LVA = "create table " 
    		+ KusssContentContract.Lva.LVA_TABLE_NAME + "(" +
    		KusssContentContract.Lva.LVA_COL_ID + " integer primary key autoincrement, " +
    		KusssContentContract.Lva.LVA_COL_TERM + " text not null, " + 
    		KusssContentContract.Lva.LVA_COL_LVANR + " integer not null, " +
    		KusssContentContract.Lva.LVA_COL_TITLE + " text not null, " +
    		KusssContentContract.Lva.LVA_COL_TYPE + " integer not null, " + 
    		KusssContentContract.Lva.LVA_COL_TEACHER + " text, " +
    		KusssContentContract.Lva.LVA_COL_SKZ + " integer, " +
    		KusssContentContract.Lva.LVA_COL_ECTS + " real, " + 
    		KusssContentContract.Lva.LVA_COL_SWS + " real" +
            ");";

    public static final String DB_CREATE_EXAM = "create table "
            + KusssContentContract.Exam.EXAM_TABLE_NAME + "(" +
            KusssContentContract.Exam.EXAM_COL_ID + " integer primary key autoincrement, " +
            KusssContentContract.Exam.EXAM_COL_TERM + " text not null, " + 
            KusssContentContract.Exam.EXAM_COL_LVANR + " integer not null, " +
            KusssContentContract.Exam.EXAM_COL_DATE + " integer not null, " +
            KusssContentContract.Exam.EXAM_COL_TIME + " text not null, " + 
            KusssContentContract.Exam.EXAM_COL_LOCATION + " text, " +
            KusssContentContract.Exam.EXAM_COL_DESCRIPTION + " text, " +
            KusssContentContract.Exam.EXAM_COL_INFO + " text" + 
            ");";
    
    public static final String DB_CREATE_GRADE = "create table "
            + KusssContentContract.Grade.GRADE_TABLE_NAME + "(" +
            KusssContentContract.Grade.GRADE_COL_ID + " integer primary key autoincrement, " +
            KusssContentContract.Grade.GRADE_COL_TERM + " text, " + 
            KusssContentContract.Grade.GRADE_COL_LVANR + " integer, " +
            KusssContentContract.Grade.GRADE_COL_DATE + " integer not null, " +
            KusssContentContract.Grade.GRADE_COL_SKZ + " integer, " + 
            KusssContentContract.Grade.GRADE_COL_GRADE + " integer not null, " +
            KusssContentContract.Grade.GRADE_COL_CODE + " text not null, " +
            KusssContentContract.Grade.GRADE_COL_TITLE + " text, " +
            KusssContentContract.Grade.GRADE_COL_TYPE + " integer" + 
            ");";
    
	public KusssDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE_LVA);
		db.execSQL(DB_CREATE_EXAM);
		db.execSQL(DB_CREATE_GRADE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + KusssContentContract.Lva.LVA_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + KusssContentContract.Exam.EXAM_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + KusssContentContract.Grade.GRADE_TABLE_NAME);
		onCreate(db);
	}

	public static void drop(Context context) {
		context.deleteDatabase(DATABASE_NAME);
	}

}
