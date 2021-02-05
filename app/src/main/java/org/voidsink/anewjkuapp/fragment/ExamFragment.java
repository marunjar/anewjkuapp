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

import android.content.UriMatcher;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.voidsink.anewjkuapp.CourseMap;
import org.voidsink.anewjkuapp.ExamListAdapter;
import org.voidsink.anewjkuapp.ExamListExam;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.sectionedrecycleradapter.SectionedRecyclerViewAdapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ExamFragment extends BaseFragment implements ContentObserverListener, LoaderManager.LoaderCallbacks<Cursor> {

    private ExamListAdapter mAdapter;
    private BaseContentObserver mDataObserver;
    private RecyclerView mRecyclerView;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mRecyclerView = view.findViewById(R.id.recyclerView);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new ExamListAdapter(getContext());
        mRecyclerView.setAdapter(new SectionedRecyclerViewAdapter<>(mRecyclerView, mAdapter));
        mRecyclerView.setContentDescription(getTitle(getContext()));

        LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.exam, menu);
    }

    @Override
    public void onStart() {
        super.onStart();

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Exam.PATH_CONTENT_CHANGED, 0);

        mDataObserver = new BaseContentObserver(uriMatcher, this);
        requireContext().getContentResolver().registerContentObserver(
                KusssContentContract.Exam.CONTENT_CHANGED_URI, false,
                mDataObserver);
    }

    @Override
    public void onStop() {
        super.onStop();

        requireContext().getContentResolver().unregisterContentObserver(
                mDataObserver);
        mDataObserver = null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_exams: {
                AppUtils.triggerSync(getContext(), true, Consts.ARG_WORKER_KUSSS_EXAMS);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    @Nullable
    protected String getScreenName() {
        return Consts.SCREEN_EXAMS;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        startProgressIndeterminate();

        return new CursorLoader(requireContext(), KusssContentContract.Exam.CONTENT_URI,
                KusssContentContract.Exam.DB.getProjection(), null, null,
                KusssContentContract.Exam.COL_DTSTART + " ASC");

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mAdapter.clear();

        if (data != null && AppUtils.getAccount(getContext()) != null) {
            CourseMap map = new CourseMap(getContext());
            List<ExamListExam> mExams = new ArrayList<>();

            data.moveToFirst();
            data.moveToPrevious();
            while (data.moveToNext()) {
                try {
                    mExams.add(new ExamListExam(data, map));
                } catch (ParseException e) {
                    AnalyticsHelper.sendException(getContext(), e, false);
                }
            }
            mAdapter.addAll(mExams);
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
        LoaderManager.getInstance(this).restartLoader(0, null, this);
    }
}
