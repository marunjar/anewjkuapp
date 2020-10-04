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

package org.voidsink.anewjkuapp.fragment;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.alamkanak.weekview.WeekViewLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.anewjkuapp.utils.UIUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarFragment2 extends CalendarPermissionFragment implements
        WeekView.EventClickListener, WeekView.EventLongPressListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final Logger logger = LoggerFactory.getLogger(CalendarFragment2.class);

    private static final String ARG_CAL_LOAD_NOW = "CLN";
    private static final String ARG_CAL_LOAD_THEN = "CLT";
    private WeekView mWeekView;
    private final MyWeekViewLoader mWeekViewLoader = new MyWeekViewLoader();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_2, container,
                false);


        mWeekView = view.findViewById(R.id.weekView);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        mWeekView.setMinDate(cal);

        cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 9);
        mWeekView.setMaxDate(cal);

        mWeekView.setOnEventClickListener(this);
        mWeekView.setEventLongPressListener(this);
        // Set long press listener for events.
        //mWeekView.setEventLongPressListener(this);
        // Set formatter for Date/Time
        mWeekView.setDateTimeInterpreter(new CalendarDateTimeInterpreter(getContext()));

        mWeekViewLoader.setDaysInPeriod(mWeekView.getNumberOfVisibleDays() * 4);
        mWeekView.setWeekViewLoader(mWeekViewLoader);

        mWeekView.setScrollListener(mWeekViewLoader);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.calendar, menu);

        MenuItem menuItem = menu.findItem(R.id.action_cal_goto_today);
        // replace the default top layer drawable of the today icon with a
        // custom drawable that shows the day of the month of today
        LayerDrawable icon = (LayerDrawable) menuItem.getIcon();
        UIUtils.setTodayIcon(icon, getContext(), "");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_calendar:
                AppUtils.triggerSync(getContext(), true, Consts.ARG_WORKER_CAL_COURSES, Consts.ARG_WORKER_CAL_EXAM);
                return true;
            case R.id.action_cal_goto_today:
                goToDate(System.currentTimeMillis());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_CALENDAR_2;
    }

    @Override
    public void onEventClick(WeekViewEvent weekViewEvent, RectF rectF) {
        AppUtils.showEventLocation(getContext(), weekViewEvent.getLocation());
    }

    @Override
    public void onContentChanged(boolean selfChange) {
        loadData(mWeekView.getFirstVisibleDay());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mWeekView != null) {
            Calendar day = mWeekView.getFirstVisibleDay();
            if (day != null) {
                double hour = mWeekView.getFirstVisibleHour();

                // check range
                if (hour < 0) hour = 0;
                else if (hour >= 23.99) hour = 23.99; //~23:59

                int iHour = (int) hour;
                int iMinute = (int) ((hour - iHour) * 60);

                day.set(Calendar.HOUR_OF_DAY, iHour);
                day.set(Calendar.MINUTE, iMinute);

                outState.putLong(Consts.ARG_CALENDAR_NOW, day.getTimeInMillis());

                logger.debug("saveDateTime: {}", day.getTimeInMillis());
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        long date = System.currentTimeMillis();
        if (savedInstanceState != null && savedInstanceState.containsKey(Consts.ARG_CALENDAR_NOW)) {
            date = savedInstanceState.getLong(Consts.ARG_CALENDAR_NOW);
        }
        goToDate(date);
    }

    @Override
    public void onStart() {
        super.onStart();

        loadData(mWeekView.getFirstVisibleDay());
    }

    @Override
    public void onPause() {
        super.onPause();

        mWeekViewLoader.stopLoading();
    }

    private void loadData(Calendar date) {
        if (date != null) {
            int periodIndex = (int) mWeekViewLoader.toWeekViewPeriodIndex(date);

            mWeekViewLoader.loadPeriod(periodIndex - 1, true);
            mWeekViewLoader.loadPeriod(periodIndex, true);
            mWeekViewLoader.loadPeriod(periodIndex + 1, true);
        }
    }

    private void goToDate(long time) {
        logger.debug("goToDate: {}", time);

        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(time);
        double hour = day.get(Calendar.HOUR_OF_DAY) + (day.get(Calendar.MINUTE) / 60.0);

        mWeekView.goToDate(day);
        mWeekView.goToHour(hour);
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        AppUtils.showEventInCalendar(getContext(), event.getId(), event.getStartTime().getTimeInMillis());
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Account mAccount = AppUtils.getAccount(getContext());

        String calIDLva = CalendarUtils.getCalIDByName(getContext(),
                mAccount, CalendarUtils.ARG_CALENDAR_COURSE, true);
        String calIDExam = CalendarUtils.getCalIDByName(getContext(),
                mAccount, CalendarUtils.ARG_CALENDAR_EXAM, true);

        if (calIDLva == null) {
            logger.warn("cannot load courses, calendar not found");
            calIDLva = "";
        }
        if (calIDExam == null) {
            logger.warn("cannot load exams, calendar not found");
            calIDExam = "";
        }

        return new CursorLoader(getContext(), CalendarContract.Events.CONTENT_URI,
                CalendarUtils.EVENT_PROJECTION,
                "("
                        + CalendarContract.Events
                        .CALENDAR_ID
                        + " = ? or "
                        + CalendarContract.Events
                        .CALENDAR_ID + " = ? ) and "
                        + CalendarContract.Events.DTSTART
                        + " >= ? and "
                        + CalendarContract.Events.DTSTART
                        + " <= ? and "
                        + CalendarContract.Events.DELETED
                        + " != 1",
                new String[]{calIDExam, calIDLva,
                        Long.toString(args.getLong(ARG_CAL_LOAD_NOW)), Long.toString(args.getLong(ARG_CAL_LOAD_THEN))},
                CalendarContract.Events.DTSTART + " ASC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        ArrayList<WeekViewEvent> events = mWeekViewLoader.getEvents(loader.getId());
        events.clear();

        Account mAccount = AppUtils.getAccount(getContext());
        if (mAccount != null) {
            // fetch calendar colors
            final SparseIntArray mColors = new SparseIntArray();
            ContentResolver cr = getContext().getContentResolver();
            try (Cursor cursor = cr.query(CalendarContract.Calendars.CONTENT_URI,
                    new String[]{
                            CalendarContract.Calendars._ID,
                            CalendarContract.Calendars
                                    .CALENDAR_COLOR}, null, null, null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        int color = cursor.getInt(1);

                        double lastContrast = ColorUtils.calculateContrast(color, mWeekView.getEventTextColor());

                        while (lastContrast < 1.6) {
                            float[] hsv = new float[3];

                            Color.colorToHSV(color, hsv);
                            hsv[2] = Math.max(0f, hsv[2] - 0.033f); // darken
                            color = Color.HSVToColor(hsv);

                            lastContrast = ColorUtils.calculateContrast(color, mWeekView.getEventTextColor());

                            if (hsv[2] == 0) break;
                        }

                        mColors.put(cursor.getInt(0), color);
                    }
                    cursor.close();
                }
            }


            if (data != null) {
                data.moveToFirst();
                data.moveToPrevious();
                while (data.moveToNext()) {

                    boolean allDay = data.getInt(CalendarUtils.COLUMN_EVENT_ALL_DAY) == 1;

                    Calendar startTime = Calendar.getInstance();
                    if (allDay) {
                        startTime.setTimeZone(TimeZone.getTimeZone("UTC"));
                    }
                    startTime.setTimeInMillis(data.getLong(CalendarUtils.COLUMN_EVENT_DTSTART));

                    Calendar endTime = Calendar.getInstance();
                    if (allDay) {
                        endTime.setTimeZone(TimeZone.getTimeZone("UTC"));
                    }
                    endTime.setTimeInMillis(data.getLong(CalendarUtils.COLUMN_EVENT_DTEND));
                    if (allDay && endTime.getTimeInMillis() % DateUtils.DAY_IN_MILLIS == 0) {
                        endTime.add(Calendar.MILLISECOND, -1);
                    }

                    WeekViewEvent event = new WeekViewEvent(data.getString(CalendarUtils.COLUMN_EVENT_ID),
                            data.getString(CalendarUtils.COLUMN_EVENT_TITLE),
                            data.getString(CalendarUtils.COLUMN_EVENT_LOCATION),
                            startTime,
                            endTime,
                            allDay);

                    event.setColor(mColors.get(data.getInt(CalendarUtils.COLUMN_EVENT_CAL_ID)));

                    events.add(event);
                }
            }
        }

        mWeekView.notifyDatasetChanged();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mWeekViewLoader.removeEvents(loader.getId());
    }

    private class MyWeekViewLoader implements WeekViewLoader, WeekView.ScrollListener {

        private int mDaysInPeriod = 7;
        private final SparseArray<ArrayList<WeekViewEvent>> mEvents;
        private final ArrayList<Integer> mLastLoadedPeriods;
        private int mLastPeriodIndex = 0;

        public MyWeekViewLoader() {
            mLastLoadedPeriods = new ArrayList<>();
            mEvents = new SparseArray<>();
        }

        public void setDaysInPeriod(int daysInPeriod) {
            mDaysInPeriod = daysInPeriod;
        }

        public ArrayList<WeekViewEvent> getEvents(int periodIndex) {
            ArrayList<WeekViewEvent> events = mEvents.get(periodIndex);
            if (events == null) {
                events = new ArrayList<>();
                mEvents.put(periodIndex, events);
            }
            return mEvents.get(periodIndex);
        }

        public void removeEvents(int periodIndex) {
            mEvents.remove(periodIndex);
        }

        @Override
        public double toWeekViewPeriodIndex(Calendar instance) {

            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);

            long days = (instance.getTimeInMillis() - now.getTimeInMillis()) / DateUtils.DAY_IN_MILLIS;

            int halfDaysInPeriod = mDaysInPeriod / 2;
            int periodIndex = 0;
            if (days > halfDaysInPeriod) {
                periodIndex = 1 + (int) (days - halfDaysInPeriod - 1) / mDaysInPeriod;
            } else if (days < -halfDaysInPeriod) {
                periodIndex = -1 + (int) (days + halfDaysInPeriod + 1) / mDaysInPeriod;
            }

            return periodIndex;
        }

        @Override
        public List<WeekViewEvent> onLoad(int periodIndex) {
            return getEvents(periodIndex);
        }

        public void loadPeriod(final int periodIndex, final boolean forceIt) {

            ((Runnable) () -> {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                int index = mLastLoadedPeriods.indexOf(periodIndex);
                if (index < 0 || forceIt) {
                    mLastLoadedPeriods.add(0, periodIndex);

                    int halfDaysInPeriod = mDaysInPeriod / 2;

                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.DATE, periodIndex * mDaysInPeriod - halfDaysInPeriod);
                    now.set(Calendar.HOUR_OF_DAY, 0);
                    now.set(Calendar.MINUTE, 0);
                    now.set(Calendar.SECOND, 0);
                    now.set(Calendar.MILLISECOND, 0);

                    Calendar then = Calendar.getInstance();
                    then.add(Calendar.DATE, periodIndex * mDaysInPeriod + halfDaysInPeriod - 1);
                    then.set(Calendar.HOUR_OF_DAY, 23);
                    then.set(Calendar.MINUTE, 59);
                    then.set(Calendar.SECOND, 59);
                    then.set(Calendar.MILLISECOND, 999);

                    Bundle args = new Bundle();
                    args.putLong(ARG_CAL_LOAD_NOW, now.getTimeInMillis());
                    args.putLong(ARG_CAL_LOAD_THEN, then.getTimeInMillis());

//                    logger.debug("loadPeriod {}({}) {} - {}", periodIndex, index, SimpleDateFormat.getDateTimeInstance().format(now.getTime()), SimpleDateFormat.getDateTimeInstance().format(then.getTime()));
                    if (index >= 0) {
                        mLastLoadedPeriods.remove(index);
                        if (hasCalendarReadPermission()) {
                            LoaderManager.getInstance(CalendarFragment2.this).restartLoader(periodIndex, args, CalendarFragment2.this);
                        }
                    } else {
                        if (hasCalendarReadPermission()) {
                            LoaderManager.getInstance(CalendarFragment2.this).initLoader(periodIndex, args, CalendarFragment2.this);
                        }
                    }

                    while (mLastLoadedPeriods.size() > 3) {
                        int removed = mLastLoadedPeriods.remove(mLastLoadedPeriods.size() - 1);

                        //logger.debug("period removed {}", removed);

                        LoaderManager.getInstance(CalendarFragment2.this).destroyLoader(removed);
                    }
                }
            }).run();
        }

        @Override
        public void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay) {
            int periodIndex = (int) toWeekViewPeriodIndex(newFirstVisibleDay);

            if (oldFirstVisibleDay == null || mLastPeriodIndex != periodIndex) {
                mLastPeriodIndex = periodIndex;

                loadPeriod(periodIndex - 1, false);
                loadPeriod(periodIndex, false);
                loadPeriod(periodIndex + 1, false);
            }
        }

        public void stopLoading() {
            if (LoaderManager.getInstance(CalendarFragment2.this).hasRunningLoaders()) {
                logger.debug("stop loading events");

                for (int id : mLastLoadedPeriods) {
                    LoaderManager.getInstance(CalendarFragment2.this).destroyLoader(id);
                }
                LoaderManager.getInstance(CalendarFragment2.this).destroyLoader(mLastPeriodIndex);
            }
        }
    }

    private static class CalendarDateTimeInterpreter implements DateTimeInterpreter {

        private final DateFormat mDateFormat;
        private final DateFormat mTimeFormat;

        CalendarDateTimeInterpreter(Context context) {
            Locale locale = AppUtils.getLocale(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mDateFormat = new SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "EEEMMdd"), locale);
            } else {
                mDateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, locale);
            }
            mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
        }

        @Override
        public String interpretDate(Calendar calendar) {
            return mDateFormat.format(calendar.getTime());
        }

        @Override
        public String interpretTime(int hour, int minute) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return mTimeFormat.format(calendar.getTime());
        }
    }
}
