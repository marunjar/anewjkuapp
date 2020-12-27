/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2020 Paul "Marunjar" Pretsch
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


package org.voidsink.anewjkuapp;

import android.net.Uri;
import android.provider.CalendarContract;

public interface KusssContentContract {

    String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    String CONTENT_TYPE_DIR = "vnd.android.cursor.dir";
    String CONTENT_TYPE_ITEM = "vnd.android.cursor.item";

    Uri CONTENT_URI = Uri.parse(String.format("content://%1$s",
            AUTHORITY));

    interface Course {
        String PATH = "lva";
        String PATH_CONTENT_CHANGED = "lva_changed";
        Uri CONTENT_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH).build();
        Uri CONTENT_CHANGED_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH_CONTENT_CHANGED).build();

        // DB Table consts
        String TABLE_NAME = "lva";
        String COL_ID = "_id";
        String COL_TERM = "term";
        String COL_COURSEID = "lvanr";
        String COL_TITLE = "title";
        String COL_LVATYPE = "type";
        String COL_LECTURER = "teacher";
        String COL_CURRICULA_ID = "skz";
        String COL_ECTS = "ects";
        String COL_SWS = "sws";
        String COL_CLASS_CODE = "code";

        interface DB {
            static String[] getProjection() {
                return new String[]{
                        Course.COL_ID,
                        Course.COL_TERM,
                        Course.COL_COURSEID,
                        Course.COL_TITLE,
                        Course.COL_CURRICULA_ID,
                        Course.COL_LVATYPE,
                        Course.COL_LECTURER,
                        Course.COL_SWS,
                        Course.COL_ECTS,
                        Course.COL_CLASS_CODE};
            }

            int COL_ID = 0;
            int COL_TERM = 1;
            int COL_COURSEID = 2;
            int COL_TITLE = 3;
            int COL_CURRICULA_ID = 4;
            int COL_TYPE = 5;
            int COL_TEACHER = 6;
            int COL_SWS = 7;
            int COL_ECTS = 8;
            int COL_CODE = 9;
        }
    }

    interface Exam {
        String PATH = "exam";
        String PATH_CONTENT_CHANGED = "exam_changed";
        Uri CONTENT_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH).build();
        Uri CONTENT_CHANGED_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH_CONTENT_CHANGED).build();

        String TABLE_NAME = "exam";
        String COL_ID = "_id";
        String COL_TERM = "term";
        String COL_COURSEID = "lvanr";
        String COL_DTSTART = "dtstart";
        String COL_DTEND = "dtend";
        String COL_LOCATION = "location";
        String COL_DESCRIPTION = "description";
        String COL_INFO = "info";
        String COL_IS_REGISTERED = "registered";
        String COL_TITLE = "title";

        interface DB {
            static String[] getProjection() {
                return new String[]{
                        Exam.COL_ID,
                        Exam.COL_TERM,
                        Exam.COL_COURSEID,
                        Exam.COL_DTSTART,
                        Exam.COL_DTEND,
                        Exam.COL_LOCATION,
                        Exam.COL_DESCRIPTION,
                        Exam.COL_INFO,
                        Exam.COL_IS_REGISTERED,
                        Exam.COL_TITLE};
            }

            int COL_ID = 0;
            int COL_TERM = 1;
            int COL_COURSEID = 2;
            int COL_DTSTART = 3;
            int COL_DTEND = 4;
            int COL_LOCATION = 5;
            int COL_DESCRIPTION = 6;
            int COL_INFO = 7;
            int COL_IS_REGISTERED = 8;
            int COL_TITLE = 9;
        }
    }

    interface Assessment {
        String PATH = "grade";
        String PATH_CONTENT_CHANGED = "grade_changed";
        Uri CONTENT_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH).build();
        Uri CONTENT_CHANGED_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH_CONTENT_CHANGED).build();

        String TABLE_NAME = "grade";
        String COL_ID = "_id";
        String COL_TERM = "term";
        String COL_COURSEID = "lvanr";
        String COL_DATE = "date";
        String COL_CURRICULA_ID = "skz";
        String COL_GRADE = "grade";
        String COL_TYPE = "type";
        String COL_TITLE = "title";
        String COL_CODE = "code";
        String COL_ECTS = "ects";
        String COL_SWS = "sws";
        String COL_LVATYPE = "lvatype";

        interface DB {
            static String[] getProjection() {
                return new String[]{
                        Assessment.COL_ID,
                        Assessment.COL_TERM,
                        Assessment.COL_COURSEID,
                        Assessment.COL_DATE,
                        Assessment.COL_CURRICULA_ID,
                        Assessment.COL_TYPE,
                        Assessment.COL_GRADE,
                        Assessment.COL_TITLE,
                        Assessment.COL_CODE,
                        Assessment.COL_ECTS,
                        Assessment.COL_SWS,
                        Assessment.COL_LVATYPE};
            }

            // Constants representing column positions from PROJECTION.
            int COL_ID = 0;
            int COL_TERM = 1;
            int COL_COURSEID = 2;
            int COL_DATE = 3;
            int COL_CURRICULA_ID = 4;
            int COL_TYPE = 5;
            int COL_GRADE = 6;
            int COL_TITLE = 7;
            int COL_CODE = 8;
            int COL_ECTS = 9;
            int COL_SWS = 10;
            int COL_LVATYPE = 11;
        }
    }

    interface Curricula {
        String PATH = "studies";
        String PATH_CONTENT_CHANGED = "studies_changed";
        Uri CONTENT_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH).build();
        Uri CONTENT_CHANGED_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH_CONTENT_CHANGED).build();

        String TABLE_NAME = "studies";
        String COL_ID = "_id";
        String COL_IS_STD = "std";
        String COL_CURRICULUM_ID = "skz";
        String COL_TITLE = "title";
        String COL_STEOP_DONE = "steopDone";
        String COL_ACTIVE_STATE = "active";
        String COL_UNI = "uni";
        String COL_DT_START = "dtStart";
        String COL_DT_END = "dtEnd";

        interface DB {
            static String[] getProjection() {
                return new String[]{
                        Curricula.COL_ID,
                        Curricula.COL_IS_STD,
                        Curricula.COL_CURRICULUM_ID,
                        Curricula.COL_TITLE,
                        Curricula.COL_STEOP_DONE,
                        Curricula.COL_ACTIVE_STATE,
                        Curricula.COL_UNI,
                        Curricula.COL_DT_START,
                        Curricula.COL_DT_END};
            }

            int COL_ID = 0;
            int COL_IS_STD = 1;
            int COL_CURRICULUM_ID = 2;
            int COL_TITLE = 3;
            int COL_STEOP_DONE = 4;
            int COL_ACTIVE_STATE = 5;
            int COL_UNI = 6;
            int COL_DT_START = 7;
            int COL_DT_END = 8;
        }
    }

    static Uri asEventSyncAdapter(Uri uri, String account,
                                  String accountType) {
        return uri
                .buildUpon()
                .appendQueryParameter(
                        CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(
                        CalendarContract.Events.ACCOUNT_NAME, account)
                .appendQueryParameter(
                        CalendarContract.Events.ACCOUNT_TYPE,
                        accountType).build();
    }

}
