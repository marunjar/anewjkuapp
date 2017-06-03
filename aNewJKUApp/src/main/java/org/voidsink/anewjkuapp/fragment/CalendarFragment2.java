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

package org.voidsink.anewjkuapp.fragment;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.ColorUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.alamkanak.weekview.WeekViewLoader;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.update.UpdateService;
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

public class CalendarFragment2 extends BaseFragment implements ContentObserverListener,
        WeekView.EventClickListener, WeekView.EventLongPressListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = CalendarFragment2.class.getSimpleName();
    private static final String ARG_CAL_LOAD_NOW = "CLN";
    private static final String ARG_CAL_LOAD_THEN = "CLT";
    private BaseContentObserver mDataObserver;
    private WeekView mWeekView;
    private final MyWeekViewLoader mWeekViewLoader = new MyWeekViewLoader();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_2, container,
                false);


        mWeekView = (WeekView) view.findViewById(R.id.weekView);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        mWeekView.setMinDate(cal);

        cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 2);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.calendar, menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            MenuItem menuItem = menu.findItem(R.id.action_cal_goto_today);
            // replace the default top layer drawable of the today icon with a
            // custom drawable that shows the day of the month of today
            LayerDrawable icon = (LayerDrawable) menuItem.getIcon();
            UIUtils.setTodayIcon(icon, getContext(), "");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_calendar:
                Intent mUpdateService = new Intent(getActivity(), UpdateService.class);
                mUpdateService.putExtra(Consts.ARG_UPDATE_CAL, true);
                getActivity().startService(mUpdateService);

                return true;
            case R.id.action_cal_goto_today:
                goToDate(System.currentTimeMillis());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CalendarContractWrapper.AUTHORITY(), CalendarContractWrapper.Events.CONTENT_URI().buildUpon().appendPath("#").build().toString(), 0);

        mDataObserver = new BaseContentObserver(uriMatcher, this);

        // listen to all changes
        getActivity().getContentResolver().registerContentObserver(
                CalendarContractWrapper.Events.CONTENT_URI().buildUpon()
                        .appendPath("#").build(), false, mDataObserver);
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().getContentResolver().unregisterContentObserver(
                mDataObserver);
        mDataObserver = null;
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
    public void onSaveInstanceState(Bundle outState) {
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

                Log.d(TAG, String.format("saveDateTime: %d", day.getTimeInMillis()));
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

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);

        loadData(cal);

    }

    private void loadData(Calendar date) {
        if (mWeekViewLoader != null && date != null) {
            int periodIndex = (int) mWeekViewLoader.toWeekViewPeriodIndex(date);

            mWeekViewLoader.loadPeriod(periodIndex - 1, true);
            mWeekViewLoader.loadPeriod(periodIndex, true);
            mWeekViewLoader.loadPeriod(periodIndex + 1, true);
        }
    }

    private void goToDate(long time) {
        Log.d(TAG, String.format("goToDate: %d", time));

        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(time);
        double hour = day.get(Calendar.HOUR_OF_DAY) + (day.get(Calendar.MINUTE) / 60);

        mWeekView.goToDate(day);
        mWeekView.goToHour(hour);
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        AppUtils.showEventInCalendar(getContext(), event.getId(), event.getStartTime().getTimeInMillis());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Account mAccount = AppUtils.getAccount(getContext());

        String calIDLva = CalendarUtils.getCalIDByName(getContext(),
                mAccount, CalendarUtils.ARG_CALENDAR_COURSE, true);
        String calIDExam = CalendarUtils.getCalIDByName(getContext(),
                mAccount, CalendarUtils.ARG_CALENDAR_EXAM, true);

        if (calIDLva == null || calIDExam == null) {
            Log.w(TAG, "no events loaded, calendars not found");
            return null;
        }

        return new CursorLoader(getContext(), CalendarContractWrapper.Events.CONTENT_URI(),
                CalendarUtils.EVENT_PROJECTION,
                "("
                        + CalendarContractWrapper.Events
                        .CALENDAR_ID()
                        + " = ? or "
                        + CalendarContractWrapper.Events
                        .CALENDAR_ID() + " = ? ) and "
                        + CalendarContractWrapper.Events.DTSTART()
                        + " >= ? and "
                        + CalendarContractWrapper.Events.DTSTART()
                        + " <= ? and "
                        + CalendarContractWrapper.Events.DELETED()
                        + " != 1",
                new String[]{calIDExam, calIDLva,
                        Long.toString(args.getLong(ARG_CAL_LOAD_NOW)), Long.toString(args.getLong(ARG_CAL_LOAD_THEN))},
                CalendarContractWrapper.Events.DTSTART() + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<WeekViewEvent> events = mWeekViewLoader.getEvents(loader.getId());
        events.clear();

        Account mAccount = AppUtils.getAccount(getContext());
        if (mAccount != null) {
            // fetch calendar colors
            final SparseIntArray mColors = new SparseIntArray();
            ContentResolver cr = getContext().getContentResolver();
            Cursor cursor = cr
                    .query(CalendarContractWrapper.Calendars.CONTENT_URI(),
                            new String[]{
                                    CalendarContractWrapper.Calendars._ID(),
                                    CalendarContractWrapper.Calendars
                                            .CALENDAR_COLOR()}, null, null,
                            null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int color = cursor.getInt(1);

                    double lastContrast = ColorUtils.calculateContrast(color, mWeekView.getEventTextColor());
                    //Log.d(TAG, String.format("color=%d %d %d, contrast=%f", Color.red(color), Color.green(color), Color.blue(color), lastContrast));

                    while (lastContrast < 1.6) {
                        float[] hsv = new float[3];

                        Color.colorToHSV(color, hsv);
                        hsv[2] = Math.max(0f, hsv[2] - 0.033f); // darken
                        color = Color.HSVToColor(hsv);

                        lastContrast = ColorUtils.calculateContrast(color, mWeekView.getEventTextColor());
                        //Log.d(TAG, String.format("new color=%d %d %d, contrast=%f", Color.red(color), Color.green(color), Color.blue(color), lastContrast));

                        if (hsv[2] == 0) break;
                    }

                    mColors.put(cursor.getInt(0), color);
                }
                cursor.close();
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

                    WeekViewEvent event = new WeekViewEvent(data.getLong(CalendarUtils.COLUMN_EVENT_ID),
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
    public void onLoaderReset(Loader<Cursor> loader) {
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

            //Log.d(TAG, String.format("%s toWeekViewPeriodIndex %d (%d days)", SimpleDateFormat.getDateTimeInstance().format(instance.getTime()), periodIndex, days));

            return periodIndex;
        }

        @Override
        public List<WeekViewEvent> onLoad(int periodIndex) {
            return getEvents(periodIndex);
        }

        public void loadPeriod(final int periodIndex, final boolean forceIt) {

            new Runnable() {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

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

                        //Log.d(TAG, String.format("loadPeriod %d(%d) %s - %s", periodIndex, index, SimpleDateFormat.getDateTimeInstance().format(now.getTime()), SimpleDateFormat.getDateTimeInstance().format(then.getTime())));
                        if (index >= 0) {
                            mLastLoadedPeriods.remove(index);
                            getLoaderManager().restartLoader(periodIndex, args, CalendarFragment2.this);
                        } else {
                            getLoaderManager().initLoader(periodIndex, args, CalendarFragment2.this);
                        }

                        while (mLastLoadedPeriods.size() > 3) {
                            int removed = mLastLoadedPeriods.remove(mLastLoadedPeriods.size() - 1);

                            //Log.d(TAG, String.format("period removed %d", removed));

                            getLoaderManager().destroyLoader(removed);
                        }
                    }
                }
            }.run();
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
    }

    private class CalendarDateTimeInterpreter implements DateTimeInterpreter {

        final DateFormat mDateFormat;
        final DateFormat mTimeFormat;

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
