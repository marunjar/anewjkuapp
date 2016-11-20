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
 */

package org.voidsink.anewjkuapp.update;

import android.Manifest;
import android.accounts.Account;
import android.app.SearchManager;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.extensions.groupwise.ShowAs;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;

import org.voidsink.anewjkuapp.ImportPoiTask;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.Poi;
import org.voidsink.anewjkuapp.PoiContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.notification.CalendarChangedNotification;
import org.voidsink.anewjkuapp.notification.SyncNotification;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportCalendarTask implements Callable<Void> {

    private static final String TAG = ImportCalendarTask.class.getSimpleName();

    private static final Object sync_lock = new Object();

    private final CalendarBuilder mCalendarBuilder;

    private static final Pattern courseIdTermPattern = Pattern
            .compile(KusssHandler.PATTERN_LVA_NR_SLASH_TERM);
    private static final Pattern lecturerPattern = Pattern
            .compile("Lva-LeiterIn:\\s+");
    private static final String EXTENDED_PROPERTY_NAME_KUSSS_ID = "kusssId";
    private static final String EXTENDED_PROPERTY_LOCATION_EXTRA = "locationExtra";

    private ContentProviderClient mProvider;
    private boolean mReleaseProvider = false;
    private final Account mAccount;
    private final SyncResult mSyncResult;
    private final Context mContext;
    private final String mCalendarName;
    private final ContentResolver mResolver;

    private final long mSyncFromNow;

    private boolean mShowProgress;
    private SyncNotification mUpdateNotification;

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

    public static final String[] EXTENDED_PROPERTIES_PROJECTION = new String[]{
            CalendarContract.ExtendedProperties.EVENT_ID,
            CalendarContract.ExtendedProperties.NAME,
            CalendarContract.ExtendedProperties.VALUE
    };

    // Constants representing column positions from PROJECTION.
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

    public ImportCalendarTask(Account account, Context context,
                              String getTypeID, CalendarBuilder calendarBuilder) {
        this(account, null, null, null,
                new SyncResult(), context, getTypeID, calendarBuilder);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            this.mProvider = context.getContentResolver()
                    .acquireContentProviderClient(
                            CalendarContractWrapper.Events.CONTENT_URI());
        }
        this.mReleaseProvider = true;
        this.mShowProgress = true;
    }

    public ImportCalendarTask(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult,
                              Context context, String calendarName,
                              CalendarBuilder calendarBuilder) {
        this.mAccount = account;
        this.mProvider = provider;
        this.mResolver = context.getContentResolver();
        this.mSyncResult = syncResult;
        this.mContext = context;
        this.mCalendarName = calendarName;
        this.mCalendarBuilder = calendarBuilder;
        this.mSyncFromNow = System.currentTimeMillis();
        this.mShowProgress = (extras != null && extras.getBoolean(Consts.SYNC_SHOW_PROGRESS, false));
    }

    private ContentValues getContentValuesFromEvent(VEvent v) {
        ContentValues cv = new ContentValues();

        cv.put(CalendarContractWrapper.Events.EVENT_LOCATION(), v.getLocation().getValue().trim());
        cv.put(CalendarContractWrapper.Events.TITLE(), v.getSummary().getValue().trim());
        cv.put(CalendarContractWrapper.Events.DESCRIPTION(), v.getDescription().getValue().trim());
        cv.put(CalendarContractWrapper.Events.DTSTART(), v.getStartDate().getDate().getTime());
        cv.put(CalendarContractWrapper.Events.DTEND(), v.getEndDate().getDate().getTime());

        return cv;
    }

    private void updateNotify(String string) {
        if (mUpdateNotification != null) {
            mUpdateNotification.update(string);
        }
    }

    private String getEventString(VEvent v) {
        return AppUtils.getEventString(v.getStartDate().getDate().getTime(), v
                .getEndDate().getDate().getTime(), v.getSummary().getValue()
                .trim());
    }

    @Override
    public Void call() throws Exception {
        if (mProvider == null) {
            return null;
        }

        if ((ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)) {
            return null;
        }

        if (!CalendarUtils.getSyncCalendar(mContext, this.mCalendarName)) {
            return null;
        }

        if (mShowProgress) {
            mUpdateNotification = new SyncNotification(mContext,
                    R.string.notification_sync_calendar);
            mUpdateNotification.show(
                    mContext.getString(R.string.notification_sync_calendar_loading, CalendarUtils.getCalendarName(mContext, this.mCalendarName)));
        }
        CalendarChangedNotification mNotification = new CalendarChangedNotification(mContext,
                CalendarUtils.getCalendarName(mContext, this.mCalendarName));


        try {
            Log.d(TAG, "setup connection");

            updateNotify(mContext.getString(R.string.notification_sync_connect));

            if (KusssHandler.getInstance().isAvailable(mContext,
                    AppUtils.getAccountAuthToken(mContext, mAccount),
                    AppUtils.getAccountName(mContext, mAccount),
                    AppUtils.getAccountPassword(mContext, mAccount))) {

                updateNotify(mContext.getString(R.string.notification_sync_calendar_loading, CalendarUtils.getCalendarName(mContext, this.mCalendarName)));

                Log.d(TAG, "loading calendar");

                Calendar iCal = null;
                String kusssIdPrefix = null;
                // {{ Load calendar events from resource
                switch (this.mCalendarName) {
                    case CalendarUtils.ARG_CALENDAR_EXAM:
                        iCal = KusssHandler.getInstance().getExamIcal(mContext,
                                mCalendarBuilder);
                        kusssIdPrefix = "at-jku-kusss-exam-";
                        break;
                    case CalendarUtils.ARG_CALENDAR_COURSE:
                        iCal = KusssHandler.getInstance().getLVAIcal(mContext,
                                mCalendarBuilder);
                        kusssIdPrefix = "at-jku-kusss-coursedate-";
                        break;
                    default: {
                        Log.w(TAG, "calendar not found: " + this.mCalendarName);
                        return null;
                    }
                }
                if (iCal == null) {
                    Log.w(TAG, "calendar not loaded: " + this.mCalendarName);
                    mSyncResult.stats.numParseExceptions++;
                    return null;
                }

                List<?> events = iCal.getComponents(Component.VEVENT);

                Log.d(TAG, String.format("got %d events", events.size()));

                updateNotify(mContext.getString(R.string.notification_sync_calendar_updating, CalendarUtils.getCalendarName(mContext, this.mCalendarName)));

                ArrayList<ContentProviderOperation> batch = new ArrayList<>();

                // modify events: move courseId/term and lecturer to description
                String lineSeparator = System.getProperty("line.separator");
                if (lineSeparator == null) lineSeparator = ", ";

                Map<String, VEvent> eventsMap = new HashMap<>();
                for (Object e : events) {
                    if (VEvent.class.isInstance(e)) {
                        VEvent ev = ((VEvent) e);

                        String summary = ev.getSummary().getValue()
                                .trim();
                        String description = ev.getDescription()
                                .getValue().trim();

                        Matcher courseIdTermMatcher = courseIdTermPattern
                                .matcher(summary); // (courseId/term)
                        if (courseIdTermMatcher.find()) {
                            if (!description.isEmpty()) {
                                description += lineSeparator;
                                description += lineSeparator;
                            }
                            description += summary
                                    .substring(courseIdTermMatcher.start());
                            summary = summary.substring(0,
                                    courseIdTermMatcher.start());
                        } else {
                            Matcher lecturerMatcher = lecturerPattern
                                    .matcher(summary);
                            if (lecturerMatcher.find()) {
                                if (!description.isEmpty()) {
                                    description += lineSeparator;
                                    description += lineSeparator;
                                }
                                description += summary
                                        .substring(lecturerMatcher
                                                .start());
                                summary = summary.substring(0,
                                        lecturerMatcher.start());
                            }
                        }

                        summary = summary.trim().replaceAll("([\\r\\n]|\\\\n)+", ", ").trim();
                        description = description.trim();

                        ev.getProperty(Property.SUMMARY).setValue(
                                summary);
                        ev.getProperty(Property.DESCRIPTION).setValue(
                                description);
                    }
                }

                // Build hash table of incoming entries
                for (Object e : events) {
                    if (VEvent.class.isInstance(e)) {
                        VEvent ev = ((VEvent) e);

                        String uid = ev.getUid().getValue();
                        // compense DST
                        eventsMap.put(uid, ev);
                    }
                }

                String calendarId = CalendarUtils.getCalIDByName(
                        mContext, mAccount, mCalendarName, true);

                if (calendarId == null) {
                    Log.w(TAG, "calendarId not found");
                    return null;
                }

                String mCalendarAccountName = mAccount.name;
                String mCalendarAccountType = mAccount.type;

                try {
                    Cursor c = mProvider.query(CalendarContractWrapper.Calendars.CONTENT_URI(),
                            CalendarUtils.CALENDAR_PROJECTION, null, null, null);
                    if (c != null) {
                        while (c.moveToNext()) {
                            if (calendarId.equals(c.getString(CalendarUtils.COLUMN_CAL_ID))) {
                                mCalendarAccountName = c.getString(CalendarUtils.COLUMN_CAL_ACCOUNT_NAME);
                                mCalendarAccountType = c.getString(CalendarUtils.COLUMN_CAL_ACCOUNT_TYPE);
                                break;
                            }
                        }
                        c.close();
                    }
                } catch (Exception e) {
                    return null;
                }

                Log.d(TAG, "Fetching local entries for merge with: " + calendarId);

                Uri calUri = CalendarContractWrapper.Events
                        .CONTENT_URI();

                // The ID of the recurring event whose instances you are
                // searching
                // for in the Instances table
                String selection = CalendarContractWrapper.Events
                        .CALENDAR_ID() + " = ?";
                String[] selectionArgs = new String[]{calendarId};

                Cursor c = mProvider.query(calUri, EVENT_PROJECTION,
                        selection, selectionArgs, null);

                if (c == null) {
                    Log.w(TAG, "selection failed");
                } else {
                    Log.d(TAG, String.format("Found %d local entries. Computing merge solution...", c.getCount()));

                    // find stale data
                    String eventId;
                    String eventKusssId;
                    String eventLocation;
                    String eventTitle;
                    String eventDescription;
                    long eventDTStart;
                    long eventDTEnd;
                    boolean eventDirty;
                    boolean eventDeleted;

                    // calc date for notifiying only future changes
                    // max update interval is 1 week
                    long notifyFrom = new Date().getTime()
                            - (DateUtils.DAY_IN_MILLIS * 7);

                    while (c.moveToNext()) {
                        mSyncResult.stats.numEntries++;
                        eventId = c.getString(COLUMN_EVENT_ID);

                        eventKusssId = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

                            // get kusssId from extended properties
                            Cursor c2 = mProvider.query(CalendarContract.ExtendedProperties.CONTENT_URI, EXTENDED_PROPERTIES_PROJECTION,
                                    CalendarContract.ExtendedProperties.EVENT_ID + " = ?",
                                    new String[]{eventId},
                                    null);

                            if (c2 != null) {
                                while (c2.moveToNext()) {

//                                    String extra = "";
//                                    for (int i = 0; i < c2.getColumnCount(); i++) {
//                                        extra = extra + i + "=" + c2.getString(i) + ";";
//                                    }
//                                    Log.d(TAG, "Extended: " + extra);

                                    if (c2.getString(1).contains(EXTENDED_PROPERTY_NAME_KUSSS_ID)) {
                                        eventKusssId = c2.getString(2);
                                    }
                                }
                                c2.close();
                            }
                        } else {
                            eventKusssId = c.getString(COLUMN_EVENT_KUSSS_ID);
                        }
                        if (TextUtils.isEmpty(eventKusssId)) {
                            eventKusssId = c.getString(COLUMN_EVENT_KUSSS_ID_LEGACY);
                        }

                        eventTitle = c.getString(COLUMN_EVENT_TITLE);

                        eventLocation = c
                                .getString(COLUMN_EVENT_LOCATION);
                        eventDescription = c
                                .getString(COLUMN_EVENT_DESCRIPTION);
                        eventDTStart = c.getLong(COLUMN_EVENT_DTSTART);
                        eventDTEnd = c.getLong(COLUMN_EVENT_DTEND);
                        eventDirty = "1".equals(c
                                .getString(COLUMN_EVENT_DIRTY));
                        eventDeleted = "1".equals(c
                                .getString(COLUMN_EVENT_DELETED));

                        if (eventKusssId != null && kusssIdPrefix != null && eventKusssId.startsWith(kusssIdPrefix)) {
                            VEvent match = eventsMap.get(eventKusssId);
                            if (match != null && !eventDeleted) {
                                // Entry exists. Remove from entry
                                // map to prevent insert later
                                eventsMap.remove(eventKusssId);

                                // update only changes after notifiyFrom
                                if ((match.getStartDate().getDate().getTime() > notifyFrom || eventDTStart > notifyFrom) &&
                                        // check to see if the entry needs to be updated
                                        ((match.getStartDate().getDate().getTime() != eventDTStart) ||
                                                (match.getEndDate().getDate().getTime() != eventDTEnd) ||
                                                (!match.getSummary().getValue().trim().equals(eventTitle.trim())) ||
                                                (!match.getSummary().getValue().trim().equals(eventTitle.trim())) ||
                                                (!match.getLocation().getValue().trim().equals(eventLocation.trim())) ||
                                                (!match.getDescription().getValue().trim().equals(eventDescription.trim()))
                                        )) {
                                    Uri existingUri = calUri.buildUpon()
                                            .appendPath(eventId).build();

                                    // Update existing record
                                    Log.d(TAG, "Scheduling update: " + existingUri
                                            + " dirty=" + eventDirty);

                                    batch.add(ContentProviderOperation
                                            .newUpdate(existingUri)
                                            .withValues(getContentValuesFromEvent(match))
                                            .build());
                                    mSyncResult.stats.numUpdates++;

                                    mNotification.addUpdate(getEventString(match));
                                } else {
                                    mSyncResult.stats.numSkippedEntries++;
                                }
                            } else {
                                if (eventDTStart > (mSyncFromNow - DateUtils.DAY_IN_MILLIS)) {
                                    // Entry doesn't exist. Remove only newer events from the database.
                                    Uri deleteUri = calUri.buildUpon()
                                            .appendPath(eventId)
                                            .build();
                                    Log.d(TAG, "Scheduling delete: " + deleteUri);
                                    // notify only future changes
                                    if (eventDTStart > notifyFrom && !eventDeleted) {
                                        mNotification
                                                .addDelete(AppUtils.getEventString(
                                                        eventDTStart,
                                                        eventDTEnd,
                                                        eventTitle));
                                    }

                                    batch.add(ContentProviderOperation
                                            .newDelete(deleteUri)
                                            .build());
                                    mSyncResult.stats.numDeletes++;
                                } else {
                                    mSyncResult.stats.numSkippedEntries++;
                                }
                            }
                        } else {
                            Log.i(TAG,
                                    "Event UID not set, ignore event: uid=" + eventKusssId
                                            + " dirty=" + eventDirty
                                            + " title=" + eventTitle);
                        }
                    }
                    c.close();

                    Log.d(TAG, String.format("Cursor closed, %d events left", eventsMap.size()));

                    updateNotify(mContext.getString(R.string.notification_sync_calendar_adding, CalendarUtils.getCalendarName(mContext, this.mCalendarName)));

                    // Add new items
                    for (VEvent v : eventsMap.values()) {

                        if (v.getUid().getValue().startsWith(kusssIdPrefix)) {
                            // notify only future changes
                            if (v.getStartDate().getDate().getTime() > notifyFrom) {
                                mNotification.addInsert(getEventString(v));
                            }

                            Builder builder = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                builder = ContentProviderOperation
                                        .newInsert(CalendarContractWrapper.Events.CONTENT_URI());
                            } else {
                                builder = ContentProviderOperation
                                        .newInsert(
                                                KusssContentContract
                                                        .asEventSyncAdapter(
                                                                CalendarContractWrapper.Events
                                                                        .CONTENT_URI(),
                                                                mCalendarAccountName,
                                                                mCalendarAccountType))
                                        .withValue(
                                                CalendarContractWrapper.Events.SYNC_ID_CUSTOM(),
                                                v.getUid().getValue());
                            }

                            builder
                                    .withValue(
                                            CalendarContractWrapper.Events
                                                    .CALENDAR_ID(),
                                            calendarId)
                                    .withValues(getContentValuesFromEvent(v))
                                    .withValue(
                                            CalendarContractWrapper.Events
                                                    .EVENT_TIMEZONE(),
                                            TimeZone.getDefault().getID());

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                boolean busy = false;

                                Property p = v.getProperty(ShowAs.PROPERTY_NAME);
                                if (p != null) {
                                    busy = p.getValue().equals(ShowAs.BUSY.getValue());
                                }
                                if (!busy) {
                                    busy = mCalendarName.equals(CalendarUtils.ARG_CALENDAR_EXAM);
                                }

                                if (busy) {
                                    builder.withValue(
                                            CalendarContractWrapper.Events
                                                    .AVAILABILITY(),
                                            CalendarContractWrapper.Events
                                                    .AVAILABILITY_BUSY());
                                } else {
                                    builder.withValue(
                                            CalendarContractWrapper.Events
                                                    .AVAILABILITY(),
                                            CalendarContractWrapper.Events
                                                    .AVAILABILITY_FREE());
                                }

                                builder.withValue(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_TENTATIVE);
                                builder.withValue(CalendarContract.Events.HAS_ALARM, "0");
                                builder.withValue(CalendarContract.Events.HAS_ATTENDEE_DATA, "0");
                                builder.withValue(CalendarContract.Events.HAS_EXTENDED_PROPERTIES, "1");
                            }

                            ContentProviderOperation op = builder.build();
                            Log.d(TAG, "Scheduling insert: " + v.getUid().getValue());
                            batch.add(op);

                            int eventIndex = batch.size() - 1;

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                // add kusssid as extendet property
                                batch.add(ContentProviderOperation
                                        .newInsert(
                                                KusssContentContract
                                                        .asEventSyncAdapter(
                                                                CalendarContract.ExtendedProperties.CONTENT_URI,
                                                                mAccount.name,
                                                                mAccount.type))
                                        .withValueBackReference(CalendarContract.ExtendedProperties.EVENT_ID, eventIndex)
                                        .withValue(CalendarContract.ExtendedProperties.NAME, EXTENDED_PROPERTY_NAME_KUSSS_ID)
                                        .withValue(CalendarContract.ExtendedProperties.VALUE, v.getUid().getValue()).build());
                                // add location extra for google maps
                                batch.add(ContentProviderOperation
                                        .newInsert(
                                                KusssContentContract
                                                        .asEventSyncAdapter(
                                                                CalendarContract.ExtendedProperties.CONTENT_URI,
                                                                mAccount.name,
                                                                mAccount.type))
                                        .withValueBackReference(CalendarContract.ExtendedProperties.EVENT_ID, eventIndex)
                                        .withValue(CalendarContract.ExtendedProperties.NAME, EXTENDED_PROPERTY_LOCATION_EXTRA)
                                        .withValue(CalendarContract.ExtendedProperties.VALUE, getLocationExtra(v)).build());
                            }
                            mSyncResult.stats.numInserts++;
                        } else {
                            mSyncResult.stats.numSkippedEntries++;
                        }
                    }

                    if (batch.size() > 0) {
                        updateNotify(mContext.getString(R.string.notification_sync_calendar_saving, CalendarUtils.getCalendarName(mContext, this.mCalendarName)));

                        Log.d(TAG, "Applying batch update");
                        mProvider.applyBatch(batch);
                        Log.d(TAG, "Notify resolver");
                        mResolver.notifyChange(calUri.buildUpon()
                                        .appendPath(calendarId).build(), // URI
                                // where
                                // data
                                // was
                                // modified
                                null, // No local observer
                                false); // IMPORTANT: Do not sync to
                        // network
                    } else {
                        Log.w(TAG,
                                "No batch operations found! Do nothing");
                    }
                }
                KusssHandler.getInstance().logout(mContext);
            } else {
                mSyncResult.stats.numAuthExceptions++;
            }
        } catch (Exception e) {
            Analytics.sendException(mContext, e, true);
            Log.e(TAG, "import calendar failed", e);
        }

        if (mUpdateNotification != null) {
            mUpdateNotification.cancel();
        }
        mNotification.show();

        if (mReleaseProvider) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mProvider.close();
            } else {
                mProvider.release();
            }
        }

        return null;
    }

    private String getLocationExtra(VEvent event) {
        try {
            String name = event.getLocation().getValue().trim();

            String formattedAddress = "Altenbergerstraße 69, 4040 Linz, Österreich";
            double latitude = 48.33706;
            double longitude = 14.31960;
            String mapsClusterId = "CmRSAAAAEgnjqopJd0JVC22GrUK5G1fgukG3Q8gxwJ_4D-NdV1OZMP8oB3v_lA8GImeDVdqUR25xFAXHrRvR3QzA3U9i_OPDMh84Q0YFRX2IUXPhUTPfu1jp17f3APBlagpU-TNEEhAo0CzFCYccX9h60fY53upEGhROUkNAKVsKbGO2faMKyGvmc_26Ig";

            if (name != null) {
                if (name.startsWith("PE00")) {
                    formattedAddress = "Petrinumstraße 12, 4040 Linz, Österreich";
                    latitude = 48.319757;
                    longitude = 14.275298;
                    mapsClusterId = "CmRRAAAAVSgRGVv3PnjX7nWhyjLYOPA98MmrhhorKQHiTpKIALBSYkMMxTKTtvDr2KS3l6IKqhDqLicgeIwPl_uwmEN0aRokUojJa7Pryg-K7rLJ9ohiWXJow68suju9NfYzfJ3tEhDPlEQoguNvjwLjC8dXva7jGhTFyeDxDdfdZ8JY-dYjpPHqv_TXuQ";
                }

                ContentResolver cr = mContext.getContentResolver();
                Uri searchUri = PoiContentContract.CONTENT_URI.buildUpon()
                        .appendPath(SearchManager.SUGGEST_URI_PATH_QUERY)
                        .appendPath(name).build();
                Cursor c = cr.query(searchUri, ImportPoiTask.POI_PROJECTION, null,
                        null, null);
                Poi p = null;

                if (c != null) {
                    while (c.moveToNext()) {
                        p = new Poi(c);

                        if (p.getName().equalsIgnoreCase(name)) {
                            break;
                        }
                        p = null;
                    }
                    c.close();
                }

                if (p != null) {
                    latitude = p.getLat();
                    longitude = p.getLon();
                    name = p.getName();
                }
            } else {
                name = "";
            }

            DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
            dfs.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("##0.0#############", dfs);

            return String.format("{\"locations\":[{\"address\":{\"formattedAddress\":\"%s\"},\"geo\":{\"latitude\":%s,\"longitude\":%s},\"mapsClusterId\":\"%s\",\"name\":\"%s\",\"url\":\"http://maps.google.com/maps?q=loc:%s,%s+(%s)&z=19\n\"}]}", formattedAddress, df.format(latitude), df.format(longitude), mapsClusterId, name, df.format(latitude), df.format(longitude), name);
        } catch (Exception e) {
            Analytics.sendException(mContext, e, true);
            return "";
        }
    }
}
