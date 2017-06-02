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

package org.voidsink.anewjkuapp.calendar;

import android.Manifest;
import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.CalendarContract;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarParserFactory;
//import net.fortuna.ical4j.extensions.groupwise.ShowAs;
import net.fortuna.ical4j.model.ParameterFactoryRegistry;
import net.fortuna.ical4j.model.PropertyFactoryRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

import org.voidsink.anewjkuapp.KusssAuthenticator;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CalendarUtils {

    public static final String ARG_CALENDAR_EXAM = "ARG_EXAM_CALENDAR";
    public static final String ARG_CALENDAR_COURSE = "ARG_LVA_CALENDAR";

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

    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContractWrapper.Events._ID(), //
            CalendarContractWrapper.Events.EVENT_LOCATION(), // VEvent.getLocation()
            CalendarContractWrapper.Events.TITLE(), // VEvent.getSummary()
            CalendarContractWrapper.Events.DESCRIPTION(), // VEvent.getDescription()
            CalendarContractWrapper.Events.DTSTART(), // VEvent.getStartDate()
            CalendarContractWrapper.Events.DTEND(), // VEvent.getEndDate()
            CalendarContractWrapper.Events.SYNC_ID_CUSTOM(), // VEvent.getUID()
            CalendarContractWrapper.Events.DIRTY(),
            CalendarContractWrapper.Events.DELETED(),
            CalendarContractWrapper.Events.CALENDAR_ID(),
            CalendarContractWrapper.Events._SYNC_ID(),
            CalendarContractWrapper.Events.ALL_DAY()};
    public static final int COLUMN_EVENT_ID = 0;
    public static final int COLUMN_EVENT_LOCATION = 1;
    public static final int COLUMN_EVENT_TITLE = 2;
    public static final int COLUMN_EVENT_DESCRIPTION = 3;
    public static final int COLUMN_EVENT_DTSTART = 4;
    public static final int COLUMN_EVENT_DTEND = 5;
    public static final int COLUMN_EVENT_KUSSS_ID = 6;
    public static final int COLUMN_EVENT_DIRTY = 7;
    public static final int COLUMN_EVENT_DELETED = 8;
    public static final int COLUMN_EVENT_CAL_ID = 9;
    public static final int COLUMN_EVENT_KUSSS_ID_LEGACY = 10;
    public static final int COLUMN_EVENT_ALL_DAY = 11;

    public static final String[] EXTENDED_PROPERTIES_PROJECTION = new String[]{
            CalendarContract.ExtendedProperties.EVENT_ID,
            CalendarContract.ExtendedProperties.NAME,
            CalendarContract.ExtendedProperties.VALUE
    };
    public static final String EXTENDED_PROPERTY_NAME_KUSSS_ID = "kusssId";
    public static final String EXTENDED_PROPERTY_LOCATION_EXTRA = "locationExtra";

    private static final String TAG = CalendarUtils.class.getSimpleName();

    private CalendarUtils() {
    }

    private static Uri createCalendar(Context context, Account account,
                                      String name, int color) {
        if (context == null || account == null) {
            return null;
        }
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

            values.put(CalendarContractWrapper.Calendars.CALENDAR_ACCESS_LEVEL(),
                    CalendarContractWrapper.Calendars.CAL_ACCESS_OWNER());

            values.put(CalendarContractWrapper.Calendars.SYNC_EVENTS(), 1);

            values.put(CalendarContractWrapper.Calendars.VISIBLE(), 1);

            values.put(
                    CalendarContractWrapper.Calendars.CAN_PARTIALLY_UPDATE(), 0);

            values.put(
                    CalendarContractWrapper.Calendars.ALLOWED_ATTENDEE_TYPES(),
                    CalendarContractWrapper.Attendees.TYPE_NONE());

            return context.getContentResolver().insert(target, values);
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

    public static CalendarBuilder newCalendarBuilder() {
        PropertyFactoryRegistry propertyFactoryRegistry = new PropertyFactoryRegistry();
//        propertyFactoryRegistry.register(ShowAs.PROPERTY_NAME, ShowAs.FACTORY);

        return new CalendarBuilder(
                CalendarParserFactory.getInstance().createParser(),
                propertyFactoryRegistry,
                new ParameterFactoryRegistry(),
                TimeZoneRegistryFactory.getInstance().createRegistry());
    }

    public static boolean createCalendarsIfNecessary(Context context,
                                                     Account account) {

        boolean calendarCreated = true;

        if (!createCalendarIfNecessary(context, account, ARG_CALENDAR_EXAM,
                AppUtils.getRandomColor())) {
            calendarCreated = false;
        }

        if (!createCalendarIfNecessary(context, account, ARG_CALENDAR_COURSE,
                AppUtils.getRandomColor())) {
            calendarCreated = false;
        }

        return calendarCreated;
    }

    private static Map<String, String> getCalIDs(Context context,
                                                 Account account) {
        // get map with calendar ids and names for specific account
        HashMap<String, String> ids = new HashMap<>();

        // nothing to do if there's no account
        if (context == null || account == null) {
            return ids;
        }

        // nothing to do if there's no permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
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
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, String.format("no id for '%s' found, no permission", name));
            return null;
        }

        String id = null;

        // get id from preferences
        if (usePreferences) {
            switch (name) {
                case ARG_CALENDAR_EXAM: {
                    id = PreferenceWrapper.getExamCalendarId(context);
                    break;
                }
                case ARG_CALENDAR_COURSE: {
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
        } else {
            Log.d(TAG, String.format("id for '%s' found: %s", name, id));
        }
        return id;
    }

    public static String getCalendarName(Context context, String name) {
        switch (name) {
            case CalendarUtils.ARG_CALENDAR_EXAM:
                return context.getString(R.string.calendar_title_exam);
            case CalendarUtils.ARG_CALENDAR_COURSE:
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
        List<String> names = new ArrayList<>();
        List<String> displayNames = new ArrayList<>();
        List<String> accountNames = new ArrayList<>();
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
                        String accountName = c.getString(COLUMN_CAL_ACCOUNT_NAME);

                        ids.add(id);
                        names.add(name);
                        displayNames.add(displayName);
                        accountNames.add(accountName);
                    }
                }
            }
        } catch (Exception e) {
            Analytics.sendException(context, e, false);
        } finally {
            if (c != null) c.close();
        }

        return new CalendarList(ids, names, displayNames, accountNames);
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
            case ARG_CALENDAR_COURSE:
                return PreferenceWrapper.getSyncCalendarLva(context);
            default:
                return true;
        }
    }

    public static boolean deleteKusssEvents(Context context, Account account) {
        boolean done = true;
        if (!deleteKusssEvents(context, getCalIDByName(context, account, ARG_CALENDAR_COURSE, false))) {
            done = false;
        }
        if (!deleteKusssEvents(context, PreferenceWrapper.getLvaCalendarId(context))) {
            done = false;
        }
        if (!deleteKusssEvents(context, getCalIDByName(context, account, ARG_CALENDAR_EXAM, false))) {
            done = false;
        }
        if (!deleteKusssEvents(context, PreferenceWrapper.getExamCalendarId(context))) {
            done = false;
        }
        return done;
    }

    private static boolean deleteKusssEvents(Context context, String calId) {
        if (calId != null) {
            ContentProviderClient provider = context.getContentResolver()
                    .acquireContentProviderClient(
                            CalendarContractWrapper.Events.CONTENT_URI());

            if (provider == null) {
                return false;
            }

            try {
                Uri calUri = CalendarContractWrapper.Events
                        .CONTENT_URI();

                Cursor c = loadEvent(provider, calUri, calId);
                if (c != null) {
                    try {
                        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
                        long deleteFrom = new Date().getTime() - DateUtils.DAY_IN_MILLIS;
                        while (c.moveToNext()) {
                            long eventDTStart = c.getLong(CalendarUtils.COLUMN_EVENT_DTSTART);
                            if (eventDTStart > deleteFrom) {
                                String eventId = c.getString(COLUMN_EVENT_ID);
                                //                        Log.d(TAG, "---------");
                                String eventKusssId = null;

                                // get kusssId from extended properties
                                Cursor c2 = provider.query(CalendarContract.ExtendedProperties.CONTENT_URI, CalendarUtils.EXTENDED_PROPERTIES_PROJECTION,
                                        CalendarContract.ExtendedProperties.EVENT_ID + " = ?",
                                        new String[]{eventId},
                                        null);

                                if (c2 != null) {
                                    while (c2.moveToNext()) {
                                        if (c2.getString(1).contains(EXTENDED_PROPERTY_NAME_KUSSS_ID)) {
                                            eventKusssId = c2.getString(2);
                                        }
                                    }
                                    c2.close();
                                }

                                if (TextUtils.isEmpty(eventKusssId)) {
                                    eventKusssId = c.getString(COLUMN_EVENT_KUSSS_ID_LEGACY);
                                }

                                if (!TextUtils.isEmpty(eventKusssId)) {
                                    if (eventKusssId.startsWith("at-jku-kusss-exam-") || eventKusssId.startsWith("at-jku-kusss-coursedate-")) {
                                        Uri deleteUri = calUri.buildUpon()
                                                .appendPath(eventId)
                                                .build();
                                        Log.d(TAG, "Scheduling delete: " + deleteUri);
                                        batch.add(ContentProviderOperation
                                                .newDelete(deleteUri)
                                                .build());
                                    }
                                }
                            }
                        }
                        if (batch.size() > 0) {
                            Log.d(TAG, "Applying batch update");
                            provider.applyBatch(batch);
                            Log.d(TAG, "Notify resolver");
                        } else {
                            Log.w(TAG,
                                    "No batch operations found! Do nothing");
                        }
                    } catch (RemoteException | OperationApplicationException e) {
                        Analytics.sendException(context, e, true);
                        return false;
                    }
                }
            } finally {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    provider.close();
                } else {
                    provider.release();
                }
            }
            return false;
        }
        return true;
    }

    public static Cursor loadEvent(ContentProviderClient mProvider, Uri calUri, String calendarId) {
        // The ID of the recurring event whose instances you are
        // searching
        // for in the Instances table
        String selection = CalendarContractWrapper.Events
                .CALENDAR_ID() + " = ?";
        String[] selectionArgs = new String[]{calendarId};

        try {
            return mProvider.query(calUri, EVENT_PROJECTION,
                    selection, selectionArgs, null);
        } catch (RemoteException e) {
            return null;
        }
    }

    public static class CalendarList {
        private final List<Integer> mIds;
        private final List<String> mNames;
        private final List<String> mDisplayNames;
        private final List<String> mAccountNames;

        public CalendarList(List<Integer> ids, List<String> names, List<String> displayNames, List<String> accountNames) {
            this.mIds = ids;
            this.mNames = names;
            this.mDisplayNames = displayNames;
            this.mAccountNames = accountNames;
        }

        public List<String> getNames() {
            return mNames;
        }

        public List<String> getDisplayNames() {
            return mDisplayNames;
        }

        public List<String> getAccountNames() {
            return mAccountNames;
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

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
}
