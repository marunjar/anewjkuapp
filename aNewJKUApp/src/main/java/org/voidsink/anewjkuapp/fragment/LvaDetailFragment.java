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

package org.voidsink.anewjkuapp.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.voidsink.anewjkuapp.CourseListAdapter;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.base.TermFragment;
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.update.ImportAssessmentTask;
import org.voidsink.anewjkuapp.update.ImportCourseTask;
import org.voidsink.anewjkuapp.update.UpdateService;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.List;

public class LvaDetailFragment extends TermFragment implements
        ContentObserverListener, LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private BaseContentObserver mLvaObserver;
    private CourseListAdapter mAdapter;

    private Cursor mAssessmentCursor = null;
    private Cursor mCourseCursor = null;
    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new CourseListAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(mAdapter));

        getLoaderManager().initLoader(Consts.LOADER_ID_COURSES, null, this);
        getLoaderManager().initLoader(Consts.LOADER_ID_ASSESSMENTS, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.lva, menu);

        MenuItem item = menu.findItem(R.id.action_toggle_visible_lvas);
        if (item != null) {
            item.setChecked(item.isCheckable() && PreferenceWrapper.getShowCoursesWithAssessmentOnly(getContext()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_lvas:
                Intent mUpdateService = new Intent(getActivity(), UpdateService.class);
                mUpdateService.putExtra(Consts.ARG_UPDATE_KUSSS_COURSES, true);
                mUpdateService.putExtra(Consts.ARG_UPDATE_KUSSS_ASSESSMENTS, true);
                getActivity().startService(mUpdateService);

                return true;
            case R.id.action_toggle_visible_lvas:
                item.setChecked(item.isCheckable() && !item.isChecked());
                PreferenceWrapper.setShowCoursesWithAssessmentOnly(getContext(), item.isChecked());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Course.PATH_CONTENT_CHANGED, 0);
        uriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Assessment.PATH_CONTENT_CHANGED, 1);

        mLvaObserver = new BaseContentObserver(uriMatcher, this);
        getActivity().getContentResolver().registerContentObserver(
                KusssContentContract.Course.CONTENT_CHANGED_URI, false,
                mLvaObserver);
        getActivity().getContentResolver().registerContentObserver(
                KusssContentContract.Assessment.CONTENT_CHANGED_URI, false,
                mLvaObserver);

        PreferenceManager.getDefaultSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);

        getActivity().getContentResolver().unregisterContentObserver(
                mLvaObserver);
    }

    @Override
    public void onContentChanged(boolean selfChange) {
        getLoaderManager().restartLoader(Consts.LOADER_ID_COURSES, null, this);
        getLoaderManager().restartLoader(Consts.LOADER_ID_ASSESSMENTS, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case Consts.LOADER_ID_COURSES: {
                return new CursorLoader(getContext(), KusssContentContract.Course.CONTENT_URI,
                        ImportCourseTask.COURSE_PROJECTION, null, null,
                        KusssContentContract.Course.COL_TERM + " DESC");
            }
            case Consts.LOADER_ID_ASSESSMENTS: {
                return new CursorLoader(getContext(), KusssContentContract.Assessment.CONTENT_URI,
                        ImportAssessmentTask.ASSESSMENT_PROJECTION, null, null,
                        KusssContentContract.Assessment.TABLE_NAME + "."
                                + KusssContentContract.Assessment.COL_TYPE
                                + " ASC,"
                                + KusssContentContract.Assessment.TABLE_NAME + "."
                                + KusssContentContract.Assessment.COL_DATE
                                + " DESC");
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case Consts.LOADER_ID_COURSES: {
                mCourseCursor = data;
                break;
            }
            case Consts.LOADER_ID_ASSESSMENTS: {
                mAssessmentCursor = data;
                break;
            }
        }
        // fill adapter
        setData(mAdapter, mCourseCursor, mAssessmentCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case Consts.LOADER_ID_COURSES: {
                mCourseCursor = null;
                break;
            }
            case Consts.LOADER_ID_ASSESSMENTS: {
                mAssessmentCursor = null;
                break;
            }
        }
        setData(mAdapter, mCourseCursor, mAssessmentCursor);
    }

    private void setData(CourseListAdapter adapter, Cursor courseCursor, Cursor assessmentCursor) {
        adapter.clear();

        if (courseCursor != null) {
            boolean withAssessmentOnly = PreferenceWrapper.getShowCoursesWithAssessmentOnly(getContext());
            // load courses
            List<Course> courses = KusssContentProvider.getCoursesFromCursor(getContext(), courseCursor);

            // sort courses
            AppUtils.sortCourses(courses);

            // load assessments
            List<Assessment> assessments = KusssContentProvider.getAssessmentsFromCursor(getContext(), assessmentCursor);

            // generate data
            List<LvaWithGrade> lvasWithGrades = AppUtils.getLvasWithGrades(getTerms(), courses, assessments, withAssessmentOnly, KusssContentProvider.getLastTerm(getContext()));

            // set data
            adapter.addAll(lvasWithGrades);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferenceWrapper.PREF_COURSES_SHOW_WITH_ASSESSMENT_ONLY)) {
            getLoaderManager().restartLoader(Consts.LOADER_ID_COURSES, null, this);
            getLoaderManager().restartLoader(Consts.LOADER_ID_ASSESSMENTS, null, this);
        }
    }
}
