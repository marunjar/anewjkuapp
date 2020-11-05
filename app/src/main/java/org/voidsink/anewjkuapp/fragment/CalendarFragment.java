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
import android.database.Cursor;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.calendar.CalendarEventAdapter;
import org.voidsink.anewjkuapp.calendar.CalendarListEvent;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.anewjkuapp.utils.UIUtils;
import org.voidsink.sectionedrecycleradapter.SectionedRecyclerViewAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends CalendarPermissionFragment implements ContentObserverListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final Logger logger = LoggerFactory.getLogger(CalendarFragment.class);

    private long now = 0;
    private long then = 0;

    private CalendarEventAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Button mLoadMoreButton;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container,
                false);

        mRecyclerView = view.findViewById(R.id.calendar_card_events);

        mLoadMoreButton = view.findViewById(R.id.calendar_card_load);
        mLoadMoreButton.setOnClickListener(v -> loadMoreData());
        mLoadMoreButton.setClickable(true);

        setButtonLoadText();

        return view;
    }

    private void setButtonLoadText() {
        mLoadMoreButton.setText(getContext().getString(R.string.listview_footer_button, SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(then)));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            now = savedInstanceState.getLong(Consts.ARG_CALENDAR_NOW, now);
            then = savedInstanceState.getLong(Consts.ARG_CALENDAR_THEN, then);

            setButtonLoadText();
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new CalendarEventAdapter(getContext());
        mRecyclerView.setAdapter(new SectionedRecyclerViewAdapter<>(mRecyclerView, mAdapter));
        mRecyclerView.setContentDescription(getTitle(getContext()));

        mAdapter.setOnItemClickListener((view, viewType, position) -> {
            if (position != RecyclerView.NO_POSITION) {
                CalendarListEvent item = mAdapter.getItem(position);
                if (item != null) {
                    item.showOnMap(getContext());
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (hasCalendarReadPermission()) {
            LoaderManager.getInstance(this).initLoader(0, null, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (LoaderManager.getInstance(this).hasRunningLoaders()) {
            logger.debug("stop loading events");

            LoaderManager.getInstance(this).destroyLoader(0);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init range
        now = System.currentTimeMillis();
        then = now + 14 * DateUtils.DAY_IN_MILLIS;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(Consts.ARG_CALENDAR_NOW, now);
        outState.putLong(Consts.ARG_CALENDAR_THEN, then);
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
                mRecyclerView.smoothScrollToPosition(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadMoreData() {
        if (this.isVisible() && !LoaderManager.getInstance(this).hasRunningLoaders()) {
            now = System.currentTimeMillis(); // if someone changed the time since last click

            // increase in month steps
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(then);

            then += cal.getActualMaximum(Calendar.DAY_OF_MONTH) * DateUtils.DAY_IN_MILLIS;

            // set button text
            setButtonLoadText();

            AnalyticsHelper.eventLoadMoreEvents();

            loadData();
        }
    }

    private void loadData() {
        if (this.isVisible() && !LoaderManager.getInstance(this).hasRunningLoaders() && hasCalendarReadPermission()) {
            LoaderManager.getInstance(this).restartLoader(0, null, this);
        }
    }

    @Override
    @Nullable
    protected String getScreenName() {
        return Consts.SCREEN_CALENDAR;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        showProgressIndeterminate();

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

        return new CursorLoader(requireContext(), CalendarContract.Events.CONTENT_URI,
                CalendarUtils.EVENT_PROJECTION,
                "("
                        + CalendarContract.Events
                        .CALENDAR_ID
                        + " = ? or "
                        + CalendarContract.Events
                        .CALENDAR_ID + " = ? ) and "
                        + CalendarContract.Events.DTEND
                        + " >= ? and "
                        + CalendarContract.Events.DTSTART
                        + " <= ? and "
                        + CalendarContract.Events.DELETED
                        + " != 1",
                new String[]{calIDExam, calIDLva,
                        Long.toString(now), Long.toString(then)},
                CalendarContract.Events.DTSTART + " ASC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mAdapter.clear();

        Account mAccount = AppUtils.getAccount(getContext());
        if (mAccount != null) {
            // fetch calendar colors
            final SparseIntArray mColors = new SparseIntArray();
            ContentResolver cr = requireContext().getContentResolver();

            try (Cursor cursor = cr.query(CalendarContract.Calendars.CONTENT_URI,
                    new String[]{
                            CalendarContract.Calendars._ID,
                            CalendarContract.Calendars
                                    .CALENDAR_COLOR}, null, null, null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        mColors.put(cursor.getInt(0), cursor.getInt(1));
                    }
                }
            }


            List<CalendarListEvent> mEvents = new ArrayList<>();

            if (data != null) {
                data.moveToFirst();
                data.moveToPrevious();
                while (data.moveToNext()) {
                    mEvents.add(new CalendarListEvent(
                            getContext(),
                            data.getLong(CalendarUtils.COLUMN_EVENT_ID),
                            mColors.get(data
                                    .getInt(CalendarUtils.COLUMN_EVENT_CAL_ID)),
                            data.getString(CalendarUtils.COLUMN_EVENT_TITLE),
                            data.getString(CalendarUtils.COLUMN_EVENT_DESCRIPTION),
                            data.getString(CalendarUtils.COLUMN_EVENT_LOCATION),
                            data.getLong(CalendarUtils.COLUMN_EVENT_DTSTART),
                            data.getLong(CalendarUtils.COLUMN_EVENT_DTEND),
                            data.getInt(CalendarUtils.COLUMN_EVENT_ALL_DAY) == 1));
                }
            }

            mAdapter.addAll(mEvents);
        }

        mAdapter.notifyDataSetChanged();

        finishProgress();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();

        finishProgress();
    }

    @Override
    public void onContentChanged(boolean selfChange) {
        loadData();
    }
}
