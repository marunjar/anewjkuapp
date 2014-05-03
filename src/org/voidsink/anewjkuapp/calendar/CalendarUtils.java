package org.voidsink.anewjkuapp.calendar;

import org.voidsink.anewjkuapp.KusssAuthenticator;

import android.accounts.AccountManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

public final class CalendarUtils {

	private CalendarUtils() {
	}

	private static final String TAG = CalendarUtils.class.getSimpleName();

	public static final int COLOR_DEFAULT_EXAM = Color.rgb(240, 149, 0); 
	public static final int COLOR_DEFAULT_LVA = Color.rgb(43, 127, 194);

	public static final String ARG_CALENDAR_ID_EXAM = "ARG_EXAM_CALENDAR_ID";
	public static final String ARG_CALENDAR_ID_LVA = "ARG_LVA_CALENDAR_ID";

	public static Uri createCalendar(Context context, Intent intent,
			String name, int color) {
		Log.d(TAG, "create calendar: " + name);

		String accountName = intent
				.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
		String accountType = intent
				.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

		Uri target = KusssAuthenticator.asCalendarSyncAdapter(
				CalendarContractWrapper.Calendars.CONTENT_URI(), accountName,
				accountType);

		ContentValues values = new ContentValues();
		values.put(CalendarContractWrapper.Calendars.OWNER_ACCOUNT(),
				accountName);
		values.put(CalendarContractWrapper.Calendars.ACCOUNT_NAME(),
				accountName);
		values.put(CalendarContractWrapper.Calendars.ACCOUNT_TYPE(),
				accountType);
		values.put(CalendarContractWrapper.Calendars.NAME(), name);
		values.put(CalendarContractWrapper.Calendars.CALENDAR_DISPLAY_NAME(),
				name);
		values.put(CalendarContractWrapper.Calendars.CALENDAR_COLOR(), color);
		// read only, CAL_ACCESS_OWNER() will be editable but is it useful?
		values.put(CalendarContractWrapper.Calendars.CALENDAR_ACCESS_LEVEL(),
				CalendarContractWrapper.Calendars.CAL_ACCESS_READ());

		values.put(CalendarContractWrapper.Calendars.SYNC_EVENTS(), 1);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			values.put(CalendarContractWrapper.Calendars.VISIBLE(), 1);

			values.put(
					CalendarContractWrapper.Calendars.CAN_PARTIALLY_UPDATE(), 0);

			values.put(
					CalendarContractWrapper.Calendars.ALLOWED_ATTENDEE_TYPES(),
					CalendarContractWrapper.Attendees.TYPE_NONE());
		}

		Uri newCalendar = context.getContentResolver().insert(target, values);

		return newCalendar;
	}

}
