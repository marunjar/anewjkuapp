/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2017 Paul "Marunjar" Pretsch
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

import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;

public class KusssContentContract {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item";

    private static final Uri CONTENT_URI = Uri.parse(String.format("content://%1$s",
            AUTHORITY));

    public static class Course {
        public static final String PATH = "lva";
        public static final String PATH_CONTENT_CHANGED = "lva_changed";
        public static final Uri CONTENT_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH).build();
        public static final Uri CONTENT_CHANGED_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH_CONTENT_CHANGED).build();

        // DB Table consts
        public static final String TABLE_NAME = "lva";
        public static final String COL_ID = "_id";
        public static final String COL_TERM = "term";
        public static final String COL_COURSEID = "lvanr";
        public static final String COL_TITLE = "title";
        public static final String COL_TYPE = "type";
        public static final String COL_LECTURER = "teacher";
        public static final String COL_CURRICULA_ID = "skz";
        public static final String COL_ECTS = "ects";
        public static final String COL_SWS = "sws";
        public static final String COL_CLASS_CODE = "code";
    }

    public static class Exam {
        public static final String PATH = "exam";
        public static final String PATH_CONTENT_CHANGED = "exam_changed";
        public static final Uri CONTENT_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH).build();
        public static final Uri CONTENT_CHANGED_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH_CONTENT_CHANGED).build();

        public static final String TABLE_NAME = "exam";
        public static final String COL_ID = "_id";
        public static final String COL_TERM = "term";
        public static final String COL_COURSEID = "lvanr";
        public static final String COL_DTSTART = "dtstart";
        public static final String COL_DTEND = "dtend";
        public static final String COL_LOCATION = "location";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_INFO = "info";
        public static final String COL_IS_REGISTERED = "registered";
        public static final String COL_TITLE = "title";
    }

    public static class Assessment {
        public static final String PATH = "grade";
        public static final String PATH_CONTENT_CHANGED = "grade_changed";
        public static final Uri CONTENT_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH).build();
        public static final Uri CONTENT_CHANGED_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH_CONTENT_CHANGED).build();

        public static final String TABLE_NAME = "grade";
        public static final String COL_ID = "_id";
        public static final String COL_TERM = "term";
        public static final String COL_COURSEID = "lvanr";
        public static final String COL_DATE = "date";
        public static final String COL_CURRICULA_ID = "skz";
        public static final String COL_GRADE = "grade";
        public static final String COL_TYPE = "type";
        public static final String COL_TITLE = "title";
        public static final String COL_CODE = "code";
        public static final String COL_ECTS = "ects";
        public static final String COL_SWS = "sws";
    }

    public static class Curricula {
        public static final String PATH = "studies";
        public static final String PATH_CONTENT_CHANGED = "studies_changed";
        public static final Uri CONTENT_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH).build();
        public static final Uri CONTENT_CHANGED_URI = KusssContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH_CONTENT_CHANGED).build();

        public static final String TABLE_NAME = "studies";
        public static final String COL_ID = "_id";
        public static final String COL_IS_STD = "std";
        public static final String COL_CURRICULUM_ID = "skz";
        public static final String COL_TITLE = "title";
        public static final String COL_STEOP_DONE = "steopDone";
        public static final String COL_ACTIVE_STATE = "active";
        public static final String COL_UNI = "uni";
        public static final String COL_DT_START = "dtStart";
        public static final String COL_DT_END = "dtEnd";
    }

    public static Uri asEventSyncAdapter(Uri uri, String account,
                                         String accountType) {
        return uri
                .buildUpon()
                .appendQueryParameter(
                        CalendarContractWrapper.CALLER_IS_SYNCADAPTER(), "true")
                .appendQueryParameter(
                        CalendarContractWrapper.Events.ACCOUNT_NAME(), account)
                .appendQueryParameter(
                        CalendarContractWrapper.Events.ACCOUNT_TYPE(),
                        accountType).build();
    }

}
