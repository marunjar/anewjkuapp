package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;

import android.net.Uri;

public class KusssContentContract {

	public static final String AUTHORITY = "org.voidsink.anewjkuapp.provider";

	public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir";
	public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item";

	public static Uri CONTENT_URI = Uri.parse(String.format("content://%1$s",
			AUTHORITY));

	public static class Lva {
		public static final String PATH = "lva";
		public static final String PATH_CONTENT_CHANGED = "lva_changed";
		public static final Uri CONTENT_URI = KusssContentContract.CONTENT_URI
				.buildUpon().appendPath(PATH).build();
		public static final Uri CONTENT_CHANGED_URI = KusssContentContract.CONTENT_URI
				.buildUpon().appendPath(PATH_CONTENT_CHANGED).build();

		// DB Table consts
		public static final String LVA_TABLE_NAME = "lva";
		public static final String LVA_COL_ID = "_id";
		public static final String LVA_COL_TERM = "term";
		public static final String LVA_COL_LVANR = "lvanr";
		public static final String LVA_COL_TITLE = "title";
		public static final String LVA_COL_TYPE = "type";
		public static final String LVA_COL_TEACHER = "teacher";
		public static final String LVA_COL_SKZ = "skz";
		public static final String LVA_COL_ECTS = "ects";
		public static final String LVA_COL_SWS = "sws";
		public static final String LVA_COL_CODE = "code";
	}

	public static class Exam {
		public static final String PATH = "exam";
		public static final String PATH_CONTENT_CHANGED = "exam_changed";
		public static final Uri CONTENT_URI = KusssContentContract.CONTENT_URI
				.buildUpon().appendPath(PATH).build();
		public static final Uri CONTENT_CHANGED_URI = KusssContentContract.CONTENT_URI
				.buildUpon().appendPath(PATH_CONTENT_CHANGED).build();

		public static final String EXAM_TABLE_NAME = "exam";
		public static final String EXAM_COL_ID = "_id";
		public static final String EXAM_COL_TERM = "term";
		public static final String EXAM_COL_LVANR = "lvanr";
		public static final String EXAM_COL_DATE = "date";
		public static final String EXAM_COL_TIME = "time";
		public static final String EXAM_COL_LOCATION = "location";
		public static final String EXAM_COL_DESCRIPTION = "description";
		public static final String EXAM_COL_INFO = "info";
		public static final String EXAM_COL_IS_REGISTERED = "registered";
		public static final String EXAM_COL_TITLE = "title";
	}

	public static class Grade {
		public static final String PATH = "grade";
		public static final String PATH_CONTENT_CHANGED = "grade_changed";
		public static final Uri CONTENT_URI = KusssContentContract.CONTENT_URI
				.buildUpon().appendPath(PATH).build();
		public static final Uri CONTENT_CHANGED_URI = KusssContentContract.CONTENT_URI
				.buildUpon().appendPath(PATH_CONTENT_CHANGED).build();

		public static final String GRADE_TABLE_NAME = "grade";
		public static final String GRADE_COL_ID = "_id";
		public static final String GRADE_COL_TERM = "term";
		public static final String GRADE_COL_LVANR = "lvanr";
		public static final String GRADE_COL_DATE = "date";
		public static final String GRADE_COL_SKZ = "skz";
		public static final String GRADE_COL_GRADE = "grade";
		public static final String GRADE_COL_TYPE = "type";
		public static final String GRADE_COL_TITLE = "title";
		public static final String GRADE_COL_CODE = "code";
		public static final String GRADE_COL_ECTS = "ects";
		public static final String GRADE_COL_SWS = "sws";
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
