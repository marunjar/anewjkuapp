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

package org.voidsink.anewjkuapp.worker;

import android.Manifest;
import android.accounts.Account;
import android.app.SearchManager;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.WorkerParameters;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Summary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.Poi;
import org.voidsink.anewjkuapp.PoiContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;
import org.voidsink.anewjkuapp.base.BaseWorker;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.kusss.KusssCalendar;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.notification.CalendarChangedNotification;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportCalendarWorker extends BaseWorker {

    private static final Logger logger = LoggerFactory.getLogger(ImportCalendarWorker.class);

    private final CalendarBuilder mCalendarBuilder;
    private static final Pattern courseIdTermPattern = Pattern
            .compile(KusssHandler.PATTERN_LVA_NR_SLASH_TERM);
    private static final Pattern lecturerPattern = Pattern
            .compile("Lva-LeiterIn:\\s+");

    public ImportCalendarWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.mCalendarBuilder = CalendarUtils.newCalendarBuilder(); // must create in main
    }

    @NonNull
    @Override
    public Result doWork() {
        if (getTags().contains(Consts.ARG_WORKER_CAL_COURSES)) {
            return importCalendar(CalendarUtils.ARG_CALENDAR_COURSE);
        } else if (getTags().contains(Consts.ARG_WORKER_CAL_EXAM)) {
            return importCalendar(CalendarUtils.ARG_CALENDAR_EXAM);
        }
        return getFailure();
    }

    private Result importCalendar(String calendarName) {
        if (CalendarUtils.ARG_CALENDAR_COURSE.equals(calendarName)) {
            AnalyticsHelper.eventReloadEventsCourse();
        } else if (CalendarUtils.ARG_CALENDAR_EXAM.equals(calendarName)) {
            AnalyticsHelper.eventReloadEventsExam();
        }

        final Account mAccount = AppUtils.getAccount(getApplicationContext());
        if (mAccount == null) {
            return getSuccess();
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return getFailure();
        }

        final ContentResolver mResolver = getApplicationContext().getContentResolver();
        if (mResolver == null) {
            return getFailure();
        }

        final ContentProviderClient mProvider = mResolver.acquireContentProviderClient(CalendarContract.Events.CONTENT_URI);

        if (mProvider == null) {
            return getFailure();
        }

        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)) {
            return getFailure();
        }

        if (!CalendarUtils.getSyncCalendar(getApplicationContext(), calendarName)) {
            return getSuccess();
        }

        final long mSyncFromNow = System.currentTimeMillis() / DateUtils.DAY_IN_MILLIS * DateUtils.DAY_IN_MILLIS;

        showUpdateNotification(R.string.notification_sync_calendar, CalendarUtils.getCalendarName(getApplicationContext(), calendarName));

        CalendarChangedNotification mChangedNotification = new CalendarChangedNotification(getApplicationContext(),
                CalendarUtils.getCalendarName(getApplicationContext(), calendarName));

        try {
            logger.debug("setup connection");

            updateNotification(getApplicationContext().getString(R.string.notification_sync_connect));

            if (KusssHandler.getInstance().isAvailable(getApplicationContext(),
                    AppUtils.getAccountAuthToken(getApplicationContext(), mAccount),
                    AppUtils.getAccountName(mAccount),
                    AppUtils.getAccountPassword(getApplicationContext(), mAccount))) {

                updateNotification(getApplicationContext().getString(R.string.notification_sync_calendar_loading, CalendarUtils.getCalendarName(getApplicationContext(), calendarName)));

                logger.debug("loading calendar");

                ArrayList<ContentProviderOperation> batch = new ArrayList<>();
                Uri calUri = CalendarContract.Events.CONTENT_URI;

                String calendarId = CalendarUtils.getCalIDByName(getApplicationContext(), mAccount, calendarName, true);

                if (calendarId == null) {
                    logger.warn("calendarId not found");
                    return getFailure();
                }

                List<KusssCalendar> calendars = KusssHandler.getInstance().getIcal(getApplicationContext(), mCalendarBuilder, calendarName, new Date(mSyncFromNow), false);

                for (KusssCalendar calendar : calendars) {
                    if (calendar.getCalendar() == null) {
                        if (calendar.isMandatory()) {
                            logger.warn("calendar not loaded: {}", calendar.getName());
                            return getRetry();
                        }
                    } else {
                        List<?> events = calendar.getCalendar().getComponents(Component.VEVENT);

                        logger.debug("got {} events", events.size());

                        updateNotification(getApplicationContext().getString(R.string.notification_sync_calendar_updating, calendar.getName()));

                        // modify events: move courseId/term and lecturer to description
                        String lineSeparator = System.getProperty("line.separator");
                        if (lineSeparator == null) lineSeparator = ", ";

                        for (Object e : events) {
                            if (e instanceof VEvent) {
                                VEvent ev = ((VEvent) e);

                                enforceFieldsNotNull(ev);

                                if (!splitSummaryV2(ev, lineSeparator)) {
                                    splitSummaryV1(ev, lineSeparator);
                                }
                            }
                        }

                        // Build hash table of incoming entries
                        Map<String, VEvent> eventsMap = new HashMap<>();
                        for (Object e : events) {
                            if (e instanceof VEvent) {
                                VEvent ev = ((VEvent) e);

                                if ((getStartDate(ev) >= calendar.getTerm().getStart().getTime()) &&
                                        (getStartDate(ev) <= calendar.getTerm().getEnd().getTime())) {
                                    String uid = getUid(ev);
                                    // compense DST
                                    eventsMap.put(uid, ev);
                                }
                            }
                        }

                        logger.debug("Fetching local entries for merge with: {}", calendarId);

                        // calc date for notifiying only future changes
                        // max update interval is 1 week
                        long notifyFrom = System.currentTimeMillis() / DateUtils.DAY_IN_MILLIS * DateUtils.DAY_IN_MILLIS - (DateUtils.DAY_IN_MILLIS * 7);

                        try (Cursor c = CalendarUtils.loadEventsBetween(mProvider, calUri, calendarId, calendar.getTerm().getStart(), calendar.getTerm().getEnd())) {
                            if (c == null) {
                                logger.warn("selection failed");
                            } else {
                                logger.debug("Found {} local entries. Computing merge solution...", c.getCount());

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

                                while (c.moveToNext()) {
                                    eventId = c.getString(CalendarUtils.COLUMN_EVENT_ID);

                                    eventKusssId = null;

                                    // get kusssId from extended properties
                                    try (Cursor c2 = mProvider.query(CalendarContract.ExtendedProperties.CONTENT_URI, CalendarUtils.getExtendedPropertiesProjection(),
                                            CalendarContract.ExtendedProperties.EVENT_ID + " = ?",
                                            new String[]{eventId},
                                            null)) {
                                        if (c2 != null) {
                                            while (c2.moveToNext()) {
                                                if (c2.getString(1).contains(CalendarUtils.EXTENDED_PROPERTY_NAME_KUSSS_ID)) {
                                                    eventKusssId = c2.getString(2);
                                                }
                                            }
                                        }
                                    }

                                    if (TextUtils.isEmpty(eventKusssId)) {
                                        eventKusssId = c.getString(CalendarUtils.COLUMN_EVENT_KUSSS_ID_LEGACY);
                                    }

                                    eventTitle = c.getString(CalendarUtils.COLUMN_EVENT_TITLE);
                                    logger.debug("Title: {}", eventTitle);

                                    eventLocation = c
                                            .getString(CalendarUtils.COLUMN_EVENT_LOCATION);
                                    eventDescription = c
                                            .getString(CalendarUtils.COLUMN_EVENT_DESCRIPTION);
                                    eventDTStart = c.getLong(CalendarUtils.COLUMN_EVENT_DTSTART);
                                    eventDTEnd = c.getLong(CalendarUtils.COLUMN_EVENT_DTEND);
                                    eventDirty = "1".equals(c
                                            .getString(CalendarUtils.COLUMN_EVENT_DIRTY));
                                    eventDeleted = "1".equals(c
                                            .getString(CalendarUtils.COLUMN_EVENT_DELETED));

                                    if (eventKusssId != null && eventKusssId.startsWith(calendar.getUidPrefix())) {
                                        VEvent match = eventsMap.get(eventKusssId);
                                        if (match != null && !eventDeleted) {
                                            // Entry exists. Remove from entry
                                            // map to prevent insert later
                                            eventsMap.remove(eventKusssId);

                                            // update only changes after notifiyFrom
                                            if ((getStartDate(match) > notifyFrom || eventDTStart > notifyFrom) &&
                                                    // check to see if the entry needs to be updated
                                                    ((getStartDate(match) != eventDTStart) ||
                                                            (getEndDate(match) != eventDTEnd) ||
                                                            !getSummary(match).equals(eventTitle.trim()) ||
                                                            !getLocation(match).equals(eventLocation.trim()) ||
                                                            !getDescription(match).equals(eventDescription.trim())
                                                    )) {
                                                Uri existingUri = calUri.buildUpon()
                                                        .appendPath(eventId).build();

                                                // Update existing record
                                                logger.debug("Scheduling update: {} dirty={}", existingUri, eventDirty);

                                                batch.add(ContentProviderOperation
                                                        .newUpdate(existingUri)
                                                        .withValues(getContentValuesFromEvent(match))
                                                        .build());

                                                mChangedNotification.addUpdate(getEventString(getApplicationContext(), match));
                                            }
                                        } else {
                                            if ((eventDTStart >= mSyncFromNow) &&
                                                    (eventDTStart >= calendar.getTerm().getStart().getTime()) &&
                                                    (eventDTStart <= calendar.getTerm().getEnd().getTime())) {
                                                // Entry doesn't exist. Remove only newer events from the database.
                                                Uri deleteUri = calUri.buildUpon()
                                                        .appendPath(eventId)
                                                        .build();
                                                logger.debug("Scheduling delete: {}", deleteUri);
                                                // notify only future changes
                                                if (eventDTStart > notifyFrom && !eventDeleted) {
                                                    mChangedNotification
                                                            .addDelete(AppUtils.getEventString(
                                                                    getApplicationContext(),
                                                                    eventDTStart,
                                                                    eventDTEnd,
                                                                    eventTitle, false));
                                                }

                                                batch.add(ContentProviderOperation
                                                        .newDelete(deleteUri)
                                                        .build());
                                            }
                                        }
                                    } else {
                                        logger.info("Event UID not set, ignore event: uid={} dirty={} title={}" + eventKusssId, eventDirty, eventTitle);
                                    }
                                }
                            }

                            logger.debug("Cursor closed, {} events left", eventsMap.size());

                            updateNotification(getApplicationContext().getString(R.string.notification_sync_calendar_adding, calendar.getName()));

                            // Add new items
                            for (VEvent v : eventsMap.values()) {
                                String uid = getUid(v);
                                if ((uid.startsWith(calendar.getUidPrefix()) &&
                                        (getStartDate(v) >= calendar.getTerm().getStart().getTime()) &&
                                        (getStartDate(v) <= calendar.getTerm().getEnd().getTime()))) {
                                    // notify only future changes
                                    if (getStartDate(v) > notifyFrom) {
                                        mChangedNotification.addInsert(getEventString(getApplicationContext(), v));
                                    }

                                    ContentProviderOperation.Builder builder = ContentProviderOperation
                                            .newInsert(CalendarContract.Events.CONTENT_URI);

                                    builder.withValue(
                                            CalendarContract.Events
                                                    .CALENDAR_ID,
                                            calendarId)
                                            .withValues(getContentValuesFromEvent(v))
                                            .withValue(
                                                    CalendarContract.Events
                                                            .EVENT_TIMEZONE,
                                                    TimeZone.getDefault().getID());

                                    if (calendarName.equals(CalendarUtils.ARG_CALENDAR_EXAM)) {
                                        builder.withValue(
                                                CalendarContract.Events
                                                        .AVAILABILITY,
                                                CalendarContract.Events
                                                        .AVAILABILITY_BUSY);
                                    } else {
                                        builder.withValue(
                                                CalendarContract.Events
                                                        .AVAILABILITY,
                                                CalendarContract.Events
                                                        .AVAILABILITY_FREE);
                                    }

                                    builder.withValue(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_TENTATIVE);
                                    builder.withValue(CalendarContract.Events.HAS_ALARM, "0");
                                    builder.withValue(CalendarContract.Events.HAS_ATTENDEE_DATA, "0");
                                    builder.withValue(CalendarContract.Events.HAS_EXTENDED_PROPERTIES, "1");

                                    ContentProviderOperation op = builder.build();
                                    logger.debug("Scheduling insert: {}", uid);
                                    batch.add(op);

                                    int eventIndex = batch.size() - 1;

                                    // add kusssid as extendet property
                                    batch.add(ContentProviderOperation
                                            .newInsert(
                                                    KusssContentContract
                                                            .asEventSyncAdapter(
                                                                    CalendarContract.ExtendedProperties.CONTENT_URI,
                                                                    mAccount.name,
                                                                    mAccount.type))
                                            .withValueBackReference(CalendarContract.ExtendedProperties.EVENT_ID, eventIndex)
                                            .withValue(CalendarContract.ExtendedProperties.NAME, CalendarUtils.EXTENDED_PROPERTY_NAME_KUSSS_ID)
                                            .withValue(CalendarContract.ExtendedProperties.VALUE, uid).build());
                                    // add location extra for google maps
                                    batch.add(ContentProviderOperation
                                            .newInsert(
                                                    KusssContentContract
                                                            .asEventSyncAdapter(
                                                                    CalendarContract.ExtendedProperties.CONTENT_URI,
                                                                    mAccount.name,
                                                                    mAccount.type))
                                            .withValueBackReference(CalendarContract.ExtendedProperties.EVENT_ID, eventIndex)
                                            .withValue(CalendarContract.ExtendedProperties.NAME, CalendarUtils.EXTENDED_PROPERTY_LOCATION_EXTRA)
                                            .withValue(CalendarContract.ExtendedProperties.VALUE, getLocationExtra(v)).build());
                                }
                            }
                        }
                    }
                }

                updateNotification(getApplicationContext().getString(R.string.notification_sync_calendar_saving, CalendarUtils.getCalendarName(getApplicationContext(), calendarName)));

                if (batch.size() > 0) {
                    logger.debug("Applying batch update");
                    mProvider.applyBatch(batch);
                    logger.debug("Notify resolver");
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
                    logger.warn("No batch operations found! Do nothing");
                }

                KusssHandler.getInstance().logout(getApplicationContext());
            } else {
                return getRetry();
            }

            mChangedNotification.show();
            return getSuccess();
        } catch (Exception e) {
            AnalyticsHelper.sendException(getApplicationContext(), e, true);

            return getRetry();
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mProvider.close();
            } else {
                mProvider.release();
            }
            cancelUpdateNotification();
        }
    }

    private void enforceFieldsNotNull(VEvent ev) {
        if (ev.getDescription() == null) {
            ev.getProperties().add(new Description());
        }
        if (ev.getDescription().getValue() == null) {
            ev.getDescription().setValue("");
        }
        if (ev.getSummary() == null) {
            ev.getProperties().add(new Summary(""));
        }
        if (ev.getSummary().getValue() == null) {
            ev.getSummary().setValue("");
        }
        if (ev.getLocation() == null) {
            ev.getProperties().add(new Location());
        }
        if (ev.getLocation().getValue() == null) {
            ev.getLocation().setValue("");
        }
    }

    private boolean splitSummaryV1(VEvent ev, String lineSeparator) {
        String summary = getSummary(ev);
        String description = getDescription(ev);

        Matcher courseIdTermMatcher = courseIdTermPattern
                .matcher(summary); // (courseId/term)
        if (courseIdTermMatcher.find()) {
            description = appendWithNewLines(
                    description,
                    summary.substring(courseIdTermMatcher.start()),
                    lineSeparator);
            summary = summary.substring(0,
                    courseIdTermMatcher.start());
        } else {
            Matcher lecturerMatcher = lecturerPattern
                    .matcher(summary);
            if (lecturerMatcher.find()) {
                description = appendWithNewLines(
                        description,
                        summary.substring(lecturerMatcher.start()),
                        lineSeparator);
                summary = summary.substring(0,
                        lecturerMatcher.start());
            }
        }

        summary = summary.trim().replaceAll("([\\r\\n]|\\\\n)+", ", ").trim();
        description = description.trim();

        ev.getSummary().setValue(summary);
        ev.getDescription().setValue(description);

        return true;
    }

    private boolean splitSummaryV2(VEvent ev, String lineSeparator) {
        String summary = getSummary(ev)
                .replaceAll("([\\r\\n]|\\\\n)+", ", ").trim();
        String description = getDescription(ev);
        // Summary: Lecture / Lecturer / courseId
        List<String> values = new ArrayList<>(Arrays.asList(summary.split("\\s+/\\s+", -1)));
        if (values.size() >= 3) {
            description = appendWithNewLines(description, values.get(1), lineSeparator);
            values.remove(1);

            Matcher courseIdTermMatcher = courseIdTermPattern
                    .matcher(values.get(1)); // (courseId/term)
            if (courseIdTermMatcher.find()) {
                description = appendWithNewLines(description, values.get(1), lineSeparator, 1);
                values.remove(1);
            }

            summary = TextUtils.join(" / ", values);

            ev.getSummary().setValue(summary.trim());
            ev.getDescription().setValue(description.trim());

            return true;
        }
        return false;
    }

    private String appendWithNewLines(String content, String append, String lineSeparator) {
        return appendWithNewLines(content, append, lineSeparator, 2);
    }

    private String appendWithNewLines(String content, String append, String lineSeparator, int lineCount) {
        if (TextUtils.isEmpty(content)) {
            return append;
        } else {
            StringBuilder sb = new StringBuilder(content);
            for (int i = 0; i < lineCount; i++) {
                sb.append(lineSeparator);
            }
            sb.append(append);
            return sb.toString();
        }
    }

    private String getEventString(Context c, VEvent v) {
        return AppUtils.getEventString(c, getStartDate(v), getEndDate(v), getSummary(v), false);
    }

    private ContentValues getContentValuesFromEvent(VEvent v) {
        ContentValues cv = new ContentValues();

        cv.put(CalendarContract.Events.EVENT_LOCATION, getLocation(v));
        cv.put(CalendarContract.Events.TITLE, getSummary(v));
        cv.put(CalendarContract.Events.DESCRIPTION, getDescription(v));
        cv.put(CalendarContract.Events.DTSTART, getStartDate(v));
        cv.put(CalendarContract.Events.DTEND, getEndDate(v));

        return cv;
    }

    private String getLocationExtra(VEvent event) {
        try {
            String name = getLocation(event);
            String upperCaseName = name.toUpperCase(Locale.getDefault());

            String formattedAddress = "Altenbergerstraße 69, 4040 Linz, Österreich";
            double latitude = 48.33706;
            double longitude = 14.31960;
            String mapsClusterId = "CmRSAAAAEgnjqopJd0JVC22GrUK5G1fgukG3Q8gxwJ_4D-NdV1OZMP8oB3v_lA8GImeDVdqUR25xFAXHrRvR3QzA3U9i_OPDMh84Q0YFRX2IUXPhUTPfu1jp17f3APBlagpU-TNEEhAo0CzFCYccX9h60fY53upEGhROUkNAKVsKbGO2faMKyGvmc_26Ig";

            if (upperCaseName.startsWith("PE 00")) {
                formattedAddress = "Petrinumstraße 12, 4040 Linz, Österreich";
                latitude = 48.319757;
                longitude = 14.275298;
                mapsClusterId = "CmRRAAAAVSgRGVv3PnjX7nWhyjLYOPA98MmrhhorKQHiTpKIALBSYkMMxTKTtvDr2KS3l6IKqhDqLicgeIwPl_uwmEN0aRokUojJa7Pryg-K7rLJ9ohiWXJow68suju9NfYzfJ3tEhDPlEQoguNvjwLjC8dXva7jGhTFyeDxDdfdZ8JY-dYjpPHqv_TXuQ";
            } else if (upperCaseName.startsWith("KEP ")) {
                formattedAddress = "Altenbergerstraße 74, 4040 Linz, Österreich";
                latitude = 48.3374066;
                longitude = 14.324123;
                mapsClusterId = "CmRSAAAA2F4LeVYCcAwT4VAT6mP3xqyDEZ40xdCIlUJZjJI0HRDrZYUTsCTrAQu0uXwdgE_Q2Yx-8kYiTg2XfA2pDpU5BkKgHfDKYPfh8_Zv6AiMgf9nxoAth1aUHlbp3iGMugauEhCUsVrMJImZyNojXWN_Nm8tGhQFqVEHQz2b5RCXc7cHik17JV1DCA";
            } else if (upperCaseName.startsWith("KHG ")) {
                formattedAddress = "Mengerstraße 23, 4040 Linz, Österreich";
                latitude = 48.33565830000001;
                longitude = 14.3171069;
                mapsClusterId = "CmRRAAAAzFi-w_zcsubNwMXG9-wpVfq6tFlTl2wxfR59QcybAQuF7k4kwNwTlFQWluOgyKKEtfi5-fP-zzJM_Jwv837jI-QTFQaDXfEpdaXKgHas9VNtHDjMbbTrh2YG5-8NZQz_EhAh0qirheebQ6QJROK39fNOGhQKPJEmyjv_S8iLlpIRtbskq_dThg";
            } else if (upperCaseName.startsWith("ESH ")) {
                formattedAddress = "Julius-Raab-Straße 1-3, 4040 Linz, Österreich";
                latitude = 48.32893189999999;
                longitude = 14.3220179;
                mapsClusterId = "CmRRAAAAztw2Q-pFchJnT32wqealtHgsRyNlzebFxGqFb_PZIRsqujQKfTNKYn0zA6mdGYelwDtmm-SIKH5srpkIGrZkwhckuYQhFo3UkpLsnFYV73hScFdrSvMJLmGuKLwRHW1bEhBTuKPtU_mvcMQplpxK-h6PGhSnVtoLUH37vZBXvWna051K_nC5PA";
            }

            ContentResolver cr = getApplicationContext().getContentResolver();
            Uri searchUri = PoiContentContract.CONTENT_URI.buildUpon()
                    .appendPath(SearchManager.SUGGEST_URI_PATH_QUERY)
                    .appendPath(name).build();
            Poi p = null;

            try (Cursor c = cr.query(searchUri, PoiContentContract.Poi.DB.getProjection(), null,
                    null, null)) {
                if (c != null) {
                    while (c.moveToNext()) {
                        p = new Poi(c);

                        if (p.getName().equalsIgnoreCase(name)) {
                            break;
                        }
                        p = null;
                    }
                }
            }

            if (p != null) {
                latitude = p.getLat();
                longitude = p.getLon();
                name = p.getName();
            }

            DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
            dfs.setDecimalSeparator('.');
            DecimalFormat df = new DecimalFormat("##0.0#############", dfs);

            return String.format("{\"locations\":[{\"address\":{\"formattedAddress\":\"%s\"},\"geo\":{\"latitude\":%s,\"longitude\":%s},\"mapsClusterId\":\"%s\",\"name\":\"%s\",\"url\":\"http://maps.google.com/maps?q=loc:%s,%s+(%s)&z=19\n\"}]}", formattedAddress, df.format(latitude), df.format(longitude), mapsClusterId, name, df.format(latitude), df.format(longitude), name);
        } catch (Exception e) {
            AnalyticsHelper.sendException(getApplicationContext(), e, true);
            return "";
        }
    }

    private long getStartDate(VEvent event) {
        return event.getStartDate().getDate().getTime();
    }

    private long getEndDate(VEvent event) {
        return event.getEndDate().getDate().getTime();
    }

    private String getSummary(VEvent event) {
        return event.getSummary().getValue().trim();
    }

    private String getDescription(VEvent event) {
        return event.getDescription().getValue().trim();
    }

    private String getLocation(VEvent event) {
        return event.getLocation().getValue().trim();
    }

    private String getUid(VEvent event) {
        return event.getUid().getValue();
    }

}
