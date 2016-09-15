/*
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
 *
 */

package org.voidsink.anewjkuapp.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PoiContentContract;
import org.voidsink.anewjkuapp.analytics.Analytics;

public class KusssDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = KusssDatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "kusss.db";
    private static final int DATABASE_VERSION = 11;
    private static KusssDatabaseHelper instance = null;

    // Database creation sql statement
    public static final String DB_CREATE_COURSE = "create table if not exists "
            + KusssContentContract.Course.TABLE_NAME + "("
            + KusssContentContract.Course.COL_ID
            + " integer primary key autoincrement not null, "
            + KusssContentContract.Course.COL_TERM + " text not null, "
            + KusssContentContract.Course.COL_COURSEID + " text not null, "
            + KusssContentContract.Course.COL_CLASS_CODE + " text not null, "
            + KusssContentContract.Course.COL_TITLE + " text not null, "
            + KusssContentContract.Course.COL_TYPE + " integer not null, "
            + KusssContentContract.Course.COL_LECTURER + " text, "
            + KusssContentContract.Course.COL_CURRICULA_ID + " integer, "
            + KusssContentContract.Course.COL_ECTS + " real, "
            + KusssContentContract.Course.COL_SWS + " real" + ");";

    public static final String DB_CREATE_EXAM = "create table if not exists "
            + KusssContentContract.Exam.TABLE_NAME + "("
            + KusssContentContract.Exam.COL_ID
            + " integer primary key autoincrement not null, "
            + KusssContentContract.Exam.COL_TERM + " text not null, "
            + KusssContentContract.Exam.COL_COURSEID + " text not null, "
            + KusssContentContract.Exam.COL_DTSTART + " integer not null, "
            + KusssContentContract.Exam.COL_DTEND + " integer not null, "
            + KusssContentContract.Exam.COL_LOCATION + " text, "
            + KusssContentContract.Exam.COL_DESCRIPTION + " text, "
            + KusssContentContract.Exam.COL_INFO + " text, "
            + KusssContentContract.Exam.COL_IS_REGISTERED + " integer, "
            + KusssContentContract.Exam.COL_TITLE + " text" + ");";

    public static final String DB_CREATE_ASSESSMENT = "create table if not exists "
            + KusssContentContract.Assessment.TABLE_NAME + "("
            + KusssContentContract.Assessment.COL_ID
            + " integer primary key autoincrement not null, "
            + KusssContentContract.Assessment.COL_TERM + " text, "
            + KusssContentContract.Assessment.COL_COURSEID + " text, "
            + KusssContentContract.Assessment.COL_DATE + " integer not null, "
            + KusssContentContract.Assessment.COL_CURRICULA_ID + " integer, "
            + KusssContentContract.Assessment.COL_GRADE + " integer not null, "
            + KusssContentContract.Assessment.COL_ECTS + " real, "
            + KusssContentContract.Assessment.COL_SWS + " real, "
            + KusssContentContract.Assessment.COL_CODE + " text not null, "
            + KusssContentContract.Assessment.COL_TITLE + " text, "
            + KusssContentContract.Assessment.COL_TYPE + " integer" + ");";

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

    public static final String DB_CREATE_CURRICULUM = "create table if not exists "
            + KusssContentContract.Curricula.TABLE_NAME + "("
            + KusssContentContract.Curricula.COL_ID
            + " integer primary key autoincrement not null, "
            + KusssContentContract.Curricula.COL_IS_STD + " integer, "
            + KusssContentContract.Curricula.COL_CURRICULUM_ID + " integer, "
            + KusssContentContract.Curricula.COL_TITLE + " text, "
            + KusssContentContract.Curricula.COL_STEOP_DONE + " integer, "
            + KusssContentContract.Curricula.COL_ACTIVE_STATE + " integer, "
            + KusssContentContract.Curricula.COL_UNI + " string, "
            + KusssContentContract.Curricula.COL_DT_START + " integer not null, "
            + KusssContentContract.Curricula.COL_DT_END + " integer" + ")";

    private KusssDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Create database");
        db.execSQL(DB_CREATE_COURSE);
        db.execSQL(DB_CREATE_EXAM);
        db.execSQL(DB_CREATE_ASSESSMENT);
        db.execSQL(DB_CREATE_CURRICULUM);

        db.execSQL("DROP TABLE IF EXISTS " + PoiContentContract.Poi.TABLE_NAME);
        db.execSQL(DB_CREATE_POI);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy most of all old data");
        if (oldVersion < 9) {
            db.execSQL("DROP TABLE IF EXISTS "
                    + KusssContentContract.Assessment.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS "
                    + KusssContentContract.Exam.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS "
                    + KusssContentContract.Course.TABLE_NAME);
        }
        /*
        if (oldVersion < 10) {
            // try to import curricula
        }
        */
        if (oldVersion < 11) {
            db.execSQL("DROP TABLE IF EXISTS "
                    + KusssContentContract.Exam.TABLE_NAME);
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
        return (integer == 1);
    }

    public static int toInt(boolean bool) {
        return (bool) ? 1 : 0;
    }

    public static void dropUserData(Context context) {
        try {
            KusssDatabaseHelper mDbHelper = new KusssDatabaseHelper(context);

            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            db.execSQL("DROP TABLE IF EXISTS "
                    + KusssContentContract.Assessment.TABLE_NAME);
            db.execSQL(DB_CREATE_ASSESSMENT);

            db.execSQL("DROP TABLE IF EXISTS "
                    + KusssContentContract.Exam.TABLE_NAME);
            db.execSQL(DB_CREATE_EXAM);

            db.execSQL("DROP TABLE IF EXISTS "
                    + KusssContentContract.Course.TABLE_NAME);
            db.execSQL(DB_CREATE_COURSE);

            db.execSQL("DROP TABLE IF EXISTS "
                    + KusssContentContract.Curricula.TABLE_NAME);
            db.execSQL(DB_CREATE_CURRICULUM);
        } catch (Exception e) {
            Analytics.sendException(context, e, true);
            drop(context);
        }
    }

    public static synchronized KusssDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (KusssDatabaseHelper.class) {
                if (instance == null) instance = new KusssDatabaseHelper(context);
            }
        }
        return instance;
    }

}
