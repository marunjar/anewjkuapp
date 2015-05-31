/*******************************************************************************
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
 ******************************************************************************/

package org.voidsink.anewjkuapp.fragment;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.calendar.CalendarEventAdapter;
import org.voidsink.anewjkuapp.calendar.CalendarListEvent;
import org.voidsink.anewjkuapp.calendar.CalendarListItem;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.update.ImportCalendarTask;
import org.voidsink.anewjkuapp.update.UpdateService;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = CalendarFragment.class.getSimpleName();
    long now = 0, then = 0;

    private CalendarEventAdapter mAdapter;
    private ContentObserver mCalendarObserver;
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
        mLoadMoreButton.setText(String.format(getContext().getString(R.string.listview_footer_button), SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(then)));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new CalendarEventAdapter(getContext());

//		mListView.addFooterView(loadMore);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(mAdapter));

        mAdapter.setOnItemClickListener(new CalendarEventAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int viewType, int position) {
                CalendarListItem item = mAdapter.getItem(position);
                if (item instanceof CalendarListEvent) {
                    ((CalendarListEvent) item).showOnMap(getContext());
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

        mCalendarObserver = new CalendarContentObserver(new Handler());

        Account account = AppUtils.getAccount(getContext());

        String lvaCalId = CalendarUtils.getCalIDByName(getContext(), account, CalendarUtils.ARG_CALENDAR_COURSE, true);
        String examCalId = CalendarUtils.getCalIDByName(getContext(), account, CalendarUtils.ARG_CALENDAR_EXAM, true);

        if (lvaCalId == null || examCalId == null) {
            // listen to all changes
            getActivity().getContentResolver().registerContentObserver(
                    CalendarContractWrapper.Events.CONTENT_URI().buildUpon()
                            .appendPath("#").build(), false, mCalendarObserver);
        } else {
            getActivity().getContentResolver().registerContentObserver(
                    CalendarContractWrapper.Events.CONTENT_URI().buildUpon()
                            .appendPath(lvaCalId).build(), false, mCalendarObserver);
            getActivity().getContentResolver().registerContentObserver(
                    CalendarContractWrapper.Events.CONTENT_URI().buildUpon()
                            .appendPath(examCalId).build(), false, mCalendarObserver);
        }

    }

    @Override
    public void onStop() {
        getActivity().getContentResolver().unregisterContentObserver(
                mCalendarObserver);

        super.onStop();
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_CALENDAR;
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
                ImportCalendarTask.EVENT_PROJECTION,
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
            final Map<String, Integer> mColors = new HashMap<>();
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
                    mColors.put(cursor.getString(0), cursor.getInt(1));
                }
                cursor.close();
            }


            List<CalendarListEvent> mEvents = new ArrayList<>();

            if (data != null) {
                while (data.moveToNext()) {
                    mEvents.add(new CalendarListEvent(
                            data.getLong(ImportCalendarTask.COLUMN_EVENT_ID),
                            mColors.get(data
                                    .getString(ImportCalendarTask.COLUMN_EVENT_CAL_ID)),
                            data.getString(ImportCalendarTask.COLUMN_EVENT_TITLE),
                            data.getString(ImportCalendarTask.COLUMN_EVENT_DESCRIPTION),
                            data.getString(ImportCalendarTask.COLUMN_EVENT_LOCATION),
                            data.getLong(ImportCalendarTask.COLUMN_EVENT_DTSTART),
                            data.getLong(ImportCalendarTask.COLUMN_EVENT_DTEND)));
                }
            }

            mAdapter.addAll(mEvents);
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    private class CalendarContentObserver extends ContentObserver {

        public CalendarContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            loadData();
        }
    }
}
