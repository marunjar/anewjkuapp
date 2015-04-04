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
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends BaseFragment {

    private static final String TAG = CalendarFragment.class.getSimpleName();
    long now = 0, then = 0;

    private CalendarEventAdapter mAdapter;
    private ContentObserver mCalendarObserver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container,
                false);

        final RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.calendar_card_events);

        Button loadMore = (Button) view.findViewById(R.id.calendar_card_load);

        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreData();
            }
        });
        loadMore.setClickable(true);

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


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadData();
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
        then += 31 * DateUtils.DAY_IN_MILLIS;

        Analytics.eventLoadMoreEvents(getContext(), then - now);

        loadData();
    }

    private void loadData() {
//        Log.d(TAG, "loadData");
        new CalendarLoadTask().execute();
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

    private class CalendarLoadTask extends AsyncTask<String, Void, Void> {
        private ProgressDialog progressDialog;
        private List<CalendarListEvent> mEvents;
        private Context mContext;

        @Override
        protected Void doInBackground(String... urls) {
            Account mAccount = AppUtils.getAccount(mContext);
            if (mAccount != null) {
                // fetch calendar colors
                final Map<String, Integer> mColors = new HashMap<>();
                ContentResolver cr = mContext.getContentResolver();
                Cursor c = cr
                        .query(CalendarContractWrapper.Calendars.CONTENT_URI(),
                                new String[]{
                                        CalendarContractWrapper.Calendars._ID(),
                                        CalendarContractWrapper.Calendars
                                                .CALENDAR_COLOR()}, null, null,
                                null);
                if (c != null) {
                    while (c.moveToNext()) {
                        mColors.put(c.getString(0), c.getInt(1));
                    }
                    c.close();
                }

                String calIDLva = CalendarUtils.getCalIDByName(mContext,
                        mAccount, CalendarUtils.ARG_CALENDAR_COURSE, true);
                String calIDExam = CalendarUtils.getCalIDByName(mContext,
                        mAccount, CalendarUtils.ARG_CALENDAR_EXAM, true);

                if (calIDLva == null || calIDExam == null) {
                    Log.w(TAG, "no events loaded, calendars not found");
                    return null;
                }

                // load events
                boolean eventsFound = false;
                cr = mContext.getContentResolver();
                do {
                    c = cr.query(
                            CalendarContractWrapper.Events.CONTENT_URI(),
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

                    if (c != null) {
                        // check if c is empty
                        eventsFound = c.moveToNext();
                        if (!eventsFound) {
                            // if empty then increase "then" for max 1 year
                            if (then - now < DateUtils.YEAR_IN_MILLIS) {
                                then += 31 * DateUtils.DAY_IN_MILLIS;
                            } else {
                                eventsFound = true;
                            }
                        }
                        // restore cursor position
                        c.moveToPrevious();
                        if (!eventsFound) {
                            // close cursor before loading next events
                            c.close();
                        }
                    }
                } while (c != null && !eventsFound);
                if (c != null && !c.isClosed()) {
                    while (c.moveToNext()) {
                        mEvents.add(new CalendarListEvent(
                                c.getLong(ImportCalendarTask.COLUMN_EVENT_ID),
                                mColors.get(c
                                        .getString(ImportCalendarTask.COLUMN_EVENT_CAL_ID)),
                                c.getString(ImportCalendarTask.COLUMN_EVENT_TITLE),
                                c.getString(ImportCalendarTask.COLUMN_EVENT_DESCRIPTION),
                                c.getString(ImportCalendarTask.COLUMN_EVENT_LOCATION),
                                c.getLong(ImportCalendarTask.COLUMN_EVENT_DTSTART),
                                c.getLong(ImportCalendarTask.COLUMN_EVENT_DTEND)));
                    }
                    c.close();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mContext = CalendarFragment.this.getContext();
            if (mContext == null) {
                Log.e(TAG, "context is null");
            }
            mEvents = new ArrayList<>();
            progressDialog = ProgressDialog.show(mContext,
                    mContext.getString(R.string.progress_title),
                    mContext.getString(R.string.progress_load_calendar), true);
        }

        @Override
        protected void onPostExecute(Void result) {
            mAdapter.clear();
            mAdapter.addAll(mEvents);
            mAdapter.notifyDataSetChanged();

            progressDialog.dismiss();

            super.onPostExecute(result);
        }
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
