package org.voidsink.anewjkuapp.calendar;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.voidsink.anewjkuapp.KusssAuthenticator;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CalendarUtils {

    public static final int COLOR_DEFAULT_EXAM = Consts.COLOR_DEFAULT_EXAM;
    public static final int COLOR_DEFAULT_LVA = Consts.COLOR_DEFAULT_LVA;
    public static final String ARG_CALENDAR_EXAM = "ARG_EXAM_CALENDAR";
    public static final String ARG_CALENDAR_LVA = "ARG_LVA_CALENDAR";
    // Constants representing column positions from PROJECTION.
    public static final String[] CALENDAR_PROJECTION = new String[]{
            CalendarContractWrapper.Calendars._ID(),
            CalendarContractWrapper.Calendars.NAME(),
            CalendarContractWrapper.Calendars.CALENDAR_DISPLAY_NAME(),
            CalendarContractWrapper.Calendars.ACCOUNT_NAME(),
            CalendarContractWrapper.Calendars.ACCOUNT_TYPE(),
            CalendarContractWrapper.Calendars.CALENDAR_ACCESS_LEVEL()};
    public static final int COLUMN_CAL_ID = 0;
    public static final int COLUMN_CAL_NAME = 1;
    public static final int COLUMN_CAL_DISPLAY_NAME = 2;
    public static final int COLUMN_CAL_ACCOUNT_NAME = 3;
    public static final int COLUMN_CAL_ACCOUNT_TYPE = 4;
    public static final int COLUMN_CAL_ACCESS_LEVEL = 5;
    private static final String TAG = CalendarUtils.class.getSimpleName();

    private CalendarUtils() {
    }

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                values.put(CalendarContractWrapper.Calendars.CALENDAR_ACCESS_LEVEL(),
                        CalendarContractWrapper.Calendars.CAL_ACCESS_OWNER());
            } else {
                values.put(CalendarContractWrapper.Calendars.CALENDAR_ACCESS_LEVEL(),
                        CalendarContractWrapper.Calendars.CAL_ACCESS_READ());
            }

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

    public static boolean removeCalendar(Context context, String name) {
        Account account = AppUtils.getAccount(context);
        if (account == null) {
            return true;
        }

        String id = getCalIDByName(context, account, name, false);
        if (id == null) {
            return true;
        }

        final ContentResolver resolver = context.getContentResolver();

        resolver.delete(
                KusssAuthenticator.asCalendarSyncAdapter(CalendarContractWrapper.Calendars.CONTENT_URI(),
                        account.name,
                        account.type),
                CalendarContractWrapper.Calendars._ID() + "=?", new String[]{id});

        Log.i(TAG, String.format("calendar %s (id=%s) removed", name, id));

        return true;
    }

    private static boolean createCalendarIfNecessary(Context context,
                                                     Account account, String name, int color) {
        String calId = getCalIDByName(context, account, name, false);
        if (calId == null) {
            createCalendar(context, account, name, color);
            if (getCalIDByName(context, account, name, false) != null) {
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

        // nothing to do if there's no account
        if (context == null || account == null) {
            return ids;
        }

        ContentResolver cr = context.getContentResolver();
        // todo: add selection
        Cursor c = cr.query(CalendarContractWrapper.Calendars.CONTENT_URI(),
                CALENDAR_PROJECTION, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                if (account.name.equals(c.getString(COLUMN_CAL_ACCOUNT_NAME))
                        && account.type.equals(c.getString(COLUMN_CAL_ACCOUNT_TYPE))) {
                    ids.put(c.getString(COLUMN_CAL_NAME),
                            c.getString(COLUMN_CAL_ID));
                }
            }
            c.close();
        }

        return ids;
    }

    public static String getCalIDByName(Context context, Account account,
                                        String name, boolean usePreferences) {
        String id = null;
        // get id from preferences
        if (usePreferences) {
            switch (name) {
                case ARG_CALENDAR_EXAM: {
                    id = PreferenceWrapper.getExamCalendarId(context);
                    break;
                }
                case ARG_CALENDAR_LVA: {
                    id = PreferenceWrapper.getLvaCalendarId(context);
                    break;
                }
            }
            // check id from preferences
            if (id != null) {
                CalendarList calendars = getCalendars(context, false);
                if (!calendars.getIds().contains(Integer.parseInt(id))) {
                    id = null;
                }
            }
        }
        // get default calendar ids
        if (id == null) {
            id = getCalIDs(context, account).get(name);
        }

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
            default: {
                CalendarList calendars = getCalendars(context, false);
                String displayName = calendars.getDisplayName(name);
                if (TextUtils.isEmpty(displayName)) {
                    displayName = context.getString(R.string.calendar_title_unknown);
                }
                return displayName;
            }
        }
    }

    public static CalendarList getCalendars(Context context, boolean onlyWritable) {
        List<Integer> ids = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> displayNames = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();

        Cursor c = null;
        try {
            c = cr.query(CalendarContractWrapper.Calendars.CONTENT_URI(),
                    CALENDAR_PROJECTION, null, null, null);
            if (c != null) {
                while (c.moveToNext()) {
                    if (!onlyWritable || CalendarUtils.isWriteable(c.getInt(COLUMN_CAL_ACCESS_LEVEL))) {
                        int id = c.getInt(COLUMN_CAL_ID);
                        String name = c.getString(COLUMN_CAL_NAME);
                        String displayName = c.getString(COLUMN_CAL_DISPLAY_NAME);

                        ids.add(id);
                        names.add(name);
                        displayNames.add(displayName);
                    }
                }
            }
        } catch (Exception e) {
            Analytics.sendException(context, e, false);
        } finally {
            if (c != null) c.close();
        }

        return new CalendarList(ids, names, displayNames);
    }

    private static boolean isReadable(int accessLevel) {
        return accessLevel == CalendarContractWrapper.Calendars.CAL_ACCESS_CONTRIBUTOR() ||
                accessLevel == CalendarContractWrapper.Calendars.CAL_ACCESS_EDITOR() ||
                accessLevel == CalendarContractWrapper.Calendars.CAL_ACCESS_OWNER() ||
                accessLevel == CalendarContractWrapper.Calendars.CAL_ACCESS_READ() ||
                accessLevel == CalendarContractWrapper.Calendars.CAL_ACCESS_ROOT();
    }

    private static boolean isWriteable(int accessLevel) {
        return accessLevel == CalendarContractWrapper.Calendars.CAL_ACCESS_CONTRIBUTOR() ||
                accessLevel == CalendarContractWrapper.Calendars.CAL_ACCESS_EDITOR() ||
                accessLevel == CalendarContractWrapper.Calendars.CAL_ACCESS_OWNER() ||
                accessLevel == CalendarContractWrapper.Calendars.CAL_ACCESS_ROOT();
    }

    public static boolean getSyncCalendar(Context context, String name) {
        if (context == null) return false;

        switch (name) {
            case ARG_CALENDAR_EXAM:
                return PreferenceWrapper.getSyncCalendarExam(context);
            case ARG_CALENDAR_LVA:
                return PreferenceWrapper.getSyncCalendarLva(context);
            default:
                return true;
        }
    }

    public static class CalendarList {
        private final List<Integer> mIds;
        private final List<String> mNames;
        private final List<String> mDisplayNames;

        public CalendarList(List<Integer> ids, ArrayList<String> names, List<String> displayNames) {
            this.mIds = ids;
            this.mNames = names;
            this.mDisplayNames = displayNames;
        }

        public List<String> getNames() {
            return mNames;
        }

        public List<String> getDisplayNames() {
            return mDisplayNames;
        }

        public List<Integer> getIds() {
            return mIds;
        }

        public String getDisplayName(String name) {
            for (int i = 0; i < mNames.size(); i++) {
                if (mNames.get(i).equals(name)) {
                    return mDisplayNames.get(i);
                }
            }
            return null;
        }

        public String[] getIdsAsStrings() {
            String[] ret = new String[mIds.size()];
            for (int i = 0; i < mIds.size(); i++)
                ret[i] = mIds.get(i).toString();
            return ret;
        }
    }
}
