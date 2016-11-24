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
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.calendar.CalendarEventAdapter;
import org.voidsink.anewjkuapp.calendar.CalendarListEvent;
import org.voidsink.anewjkuapp.calendar.CalendarListItem;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.update.ImportCalendarTask;
import org.voidsink.anewjkuapp.update.UpdateService;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.sectionedrecycleradapter.SectionedRecyclerViewAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends BaseFragment implements ContentObserverListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = CalendarFragment.class.getSimpleName();
    private long now = 0, then = 0;

    private CalendarEventAdapter mAdapter;
    private BaseContentObserver mDataObserver;
    private RecyclerView mRecyclerView;
    private Button mLoadMoreButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container,
                false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.calendar_card_events);

        mLoadMoreButton = (Button) view.findViewById(R.id.calendar_card_load);
        mLoadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreData();
            }
        });
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

        mAdapter = new CalendarEventAdapter(getContext());

//		mListView.addFooterView(loadMore);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new SectionedRecyclerViewAdapter(mRecyclerView, mAdapter));

        mAdapter.setOnItemClickListener(new CalendarEventAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int viewType, int position) {
                if (position != RecyclerView.NO_POSITION) {
                    CalendarListItem item = mAdapter.getItem(position);
                    if (item != null) {
                        ((CalendarListEvent) item).showOnMap(getContext());
                    }
                }
            }
        });

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init range
        now = System.currentTimeMillis();
        then = now + 14 * DateUtils.DAY_IN_MILLIS;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(Consts.ARG_CALENDAR_NOW, now);
        outState.putLong(Consts.ARG_CALENDAR_THEN, then);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.calendar, menu);
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
                mRecyclerView.smoothScrollToPosition(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadMoreData() {
        now = System.currentTimeMillis(); // if someone changed the time since last click

        // increase in month steps
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(then);

        then += cal.getActualMaximum(Calendar.DAY_OF_MONTH) * DateUtils.DAY_IN_MILLIS;

        // set button text
        setButtonLoadText();

        Analytics.eventLoadMoreEvents(getContext(), then - now);

        loadData();
    }

    private void loadData() {
        getLoaderManager().restartLoader(0, null, this);
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
        return Consts.SCREEN_CALENDAR;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        showProgressIndeterminate();

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
                        + CalendarContractWrapper.Events.DTEND()
                        + " >= ? and "
                        + CalendarContractWrapper.Events.DTSTART()
                        + " <= ? and "
                        + CalendarContractWrapper.Events.DELETED()
                        + " != 1",
                new String[]{calIDExam, calIDLva,
                        Long.toString(now), Long.toString(then)},
                CalendarContractWrapper.Events.DTSTART() + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.clear();

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
                    mColors.put(cursor.getInt(0), cursor.getInt(1));
                }
                cursor.close();
            }


            List<CalendarListEvent> mEvents = new ArrayList<>();

            if (data != null) {
                data.moveToFirst();
                data.moveToPrevious();
                while (data.moveToNext()) {
                    mEvents.add(new CalendarListEvent(
                            data.getLong(CalendarUtils.COLUMN_EVENT_ID),
                            mColors.get(data
                                    .getInt(CalendarUtils.COLUMN_EVENT_CAL_ID)),
                            data.getString(CalendarUtils.COLUMN_EVENT_TITLE),
                            data.getString(CalendarUtils.COLUMN_EVENT_DESCRIPTION),
                            data.getString(CalendarUtils.COLUMN_EVENT_LOCATION),
                            data.getLong(CalendarUtils.COLUMN_EVENT_DTSTART),
                            data.getLong(CalendarUtils.COLUMN_EVENT_DTEND)));
                }
            }

            mAdapter.addAll(mEvents);
        }

        mAdapter.notifyDataSetChanged();

        finishProgress();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();

        finishProgress();
    }

    @Override
    public void onContentChanged(boolean selfChange) {
        loadData();
    }
}
