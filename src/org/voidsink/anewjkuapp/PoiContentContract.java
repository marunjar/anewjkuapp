package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;

import android.net.Uri;
import android.os.Build;

public class PoiContentContract {

	public static final String AUTHORITY = "org.voidsink.anewjkuapp.provider.poi";

	public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir";
	public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item";

	public static Uri CONTENT_URI = Uri.parse(String.format("content://%1$s",
			AUTHORITY));

	public static final String getFTS() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return "fts4";
		} else {
			return "fts3";
		}
	}	
	
	public static class Poi {
		public static final String PATH = "poi";
		public static final Uri CONTENT_URI = PoiContentContract.CONTENT_URI
				.buildUpon().appendPath(PATH).build();

		// DB Table consts
		public static final String TABLE_NAME = "poi";
		public static final String COL_ID = "_id";
		public static final String COL_LAT = "latitude";
		public static final String COL_LON = "longtitude";
		public static final String COL_NAME = "name";
		public static final String COL_DESCR = "description";
		public static final String COL_ADR_STREET = "adr_street";
		public static final String COL_ADR_CITY = "adr_city";
		public static final String COL_ADR_STATE = "adr_state";
		public static final String COL_ADR_COUNTRY = "adr_country";
		public static final String COL_ADR_POSTAL_CODE = "adr_postal_code";
		public static final String COL_IS_DEFAULT = "from_user";
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
