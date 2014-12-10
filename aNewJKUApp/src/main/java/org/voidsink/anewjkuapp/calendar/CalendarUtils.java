package org.voidsink.anewjkuapp.calendar;

import java.util.HashMap;
import java.util.Map;

import org.voidsink.anewjkuapp.KusssAuthenticator;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.Consts;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

public final class CalendarUtils {

	private CalendarUtils() {
	}

	private static final String TAG = CalendarUtils.class.getSimpleName();

	public static final int COLOR_DEFAULT_EXAM = Consts.COLOR_DEFAULT_EXAM;
	public static final int COLOR_DEFAULT_LVA = Consts.COLOR_DEFAULT_LVA;

	public static final String ARG_CALENDAR_EXAM = "ARG_EXAM_CALENDAR";
	public static final String ARG_CALENDAR_LVA = "ARG_LVA_CALENDAR";

	// Constants representing column positions from PROJECTION.
	public static final String[] CALENDAR_PROJECTION = new String[] {
			CalendarContractWrapper.Calendars._ID(),
			CalendarContractWrapper.Calendars.NAME(),
			CalendarContractWrapper.Calendars.ACCOUNT_NAME(),
			CalendarContractWrapper.Calendars.ACCOUNT_TYPE() };

	public static final int COLUMN_CAL_ID = 0;
	public static final int COLUMN_CAL_NAME = 1;
	public static final int COLUMN_CAL_ACCOUNT_NAME = 2;
	public static final int COLUMN_CAL_ACCOUNT_TYPE = 3;

	public static Uri createCalendar(Context context, Account account,
			String name, int color) {
        try {
            String accountName = account.name;
            String accountType = account.type;

            String displayName = getCalendarName(context, name);

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
                    displayName);
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
        } catch (Exception e) {
            Analytics.sendException(context, e, true, name);
            return null;
        }
	}

	private static boolean createCalendarIfNecessary(Context context,
			Account account, String name, int color) {
		String calId = getCalIDByName(context, account, name);
		if (calId == null) {
			createCalendar(context, account, name, color);
			if (getCalIDByName(context, account, name) != null) {
				Log.d(TAG, String.format("calendar '%s' created", name));
			} else {
				Log.d(TAG, String.format("can't create calendar '%s'", name));
				return false;
			}
		}
		return true;
	}

	public static boolean createCalendarsIfNecessary(Context context,
			Account account) {
		boolean calendarCreated = true;
		if (!createCalendarIfNecessary(context, account, ARG_CALENDAR_EXAM,
				COLOR_DEFAULT_EXAM)) {
			calendarCreated = false;
		}
		if (!createCalendarIfNecessary(context, account, ARG_CALENDAR_LVA,
				COLOR_DEFAULT_LVA)) {
			calendarCreated = false;
		}

		return calendarCreated;
	}

	private static Map<String, String> getCalIDs(Context context,
			Account account) {
		// get map with calendar ids and names for specific account
		Map<String, String> ids = new HashMap<String, String>();

		ContentResolver cr = context.getContentResolver();
		// todo: add selection
		Cursor c = cr.query(CalendarContractWrapper.Calendars.CONTENT_URI(),
				CALENDAR_PROJECTION, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                if (account.name.equals(c.getString(COLUMN_CAL_ACCOUNT_NAME))
                        && account.type
                        .equals(c.getString(COLUMN_CAL_ACCOUNT_TYPE))) {
                    ids.put(c.getString(COLUMN_CAL_NAME),
                            c.getString(COLUMN_CAL_ID));
                }
            }
            c.close();
        }

		return ids;
	}

	public static String getCalIDByName(Context context, Account account,
			String name) {
		String id = getCalIDs(context, account).get(name);
		if (id == null) {
			Log.w(TAG, String.format("no id for '%s' found", name));
		}
		return id;
	}

	public static String getCalendarName(Context context, String name) {
		switch (name) {
		case CalendarUtils.ARG_CALENDAR_EXAM:
			return context.getString(R.string.calendar_title_exam);
		case CalendarUtils.ARG_CALENDAR_LVA:
			return context.getString(R.string.calendar_title_lva);
		default:
			return context.getString(R.string.calendar_title_unknown);
		}
	}
}
