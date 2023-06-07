/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2023 Paul "Marunjar" Pretsch
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

import android.content.SharedPreferences;
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
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.voidsink.anewjkuapp.CourseListAdapter;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PreferenceHelper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.base.TermFragment;
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.sectionedrecycleradapter.SectionedRecyclerViewAdapter;

import java.util.List;

public class LvaDetailFragment extends TermFragment implements
        ContentObserverListener, LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private BaseContentObserver mLvaObserver;
    private CourseListAdapter mAdapter;

    private Cursor mAssessmentCursor = null;
    private Cursor mCourseCursor = null;
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

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mAdapter = new CourseListAdapter(getContext());
        mRecyclerView.setAdapter(new SectionedRecyclerViewAdapter<>(mRecyclerView, mAdapter));
        mRecyclerView.setContentDescription(getTitle(getContext()));

        LoaderManager.getInstance(this).initLoader(Consts.LOADER_ID_COURSES, null, this);
        LoaderManager.getInstance(this).initLoader(Consts.LOADER_ID_ASSESSMENTS, null, this);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.lva, menu);

        MenuItem item = menu.findItem(R.id.action_toggle_visible_lvas);
        if (item != null) {
            item.setChecked(item.isCheckable() && PreferenceHelper.getShowCoursesWithAssessmentOnly(requireContext()));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_refresh_lvas) {
            AppUtils.triggerSync(getContext(), true, Consts.ARG_WORKER_KUSSS_COURSES, Consts.ARG_WORKER_KUSSS_ASSESSMENTS);

            return true;
        } else if (itemId == R.id.action_toggle_visible_lvas) {
            item.setChecked(item.isCheckable() && !item.isChecked());
            PreferenceHelper.setShowCoursesWithAssessmentOnly(requireContext(), item.isChecked());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Course.PATH_CONTENT_CHANGED, 0);
        uriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Assessment.PATH_CONTENT_CHANGED, 1);

        mLvaObserver = new BaseContentObserver(uriMatcher, this);
        requireContext().getContentResolver().registerContentObserver(
                KusssContentContract.Course.CONTENT_CHANGED_URI, false,
                mLvaObserver);
        requireContext().getContentResolver().registerContentObserver(
                KusssContentContract.Assessment.CONTENT_CHANGED_URI, false,
                mLvaObserver);

        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .unregisterOnSharedPreferenceChangeListener(this);

        requireContext().getContentResolver().unregisterContentObserver(
                mLvaObserver);
        mLvaObserver = null;
    }

    @Override
    public void onContentChanged(boolean selfChange) {
        LoaderManager.getInstance(this).restartLoader(Consts.LOADER_ID_COURSES, null, this);
        LoaderManager.getInstance(this).restartLoader(Consts.LOADER_ID_ASSESSMENTS, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case Consts.LOADER_ID_COURSES: {
                return new CursorLoader(requireContext(), KusssContentContract.Course.CONTENT_URI,
                        KusssContentContract.Course.DB.getProjection(), null, null,
                        KusssContentContract.Course.COL_TERM + " DESC");
            }
            case Consts.LOADER_ID_ASSESSMENTS: {
                return new CursorLoader(requireContext(), KusssContentContract.Assessment.CONTENT_URI,
                        KusssContentContract.Assessment.DB.getProjection(), null, null,
                        KusssContentContract.Assessment.TABLE_NAME + "."
                                + KusssContentContract.Assessment.COL_TYPE
                                + " ASC,"
                                + KusssContentContract.Assessment.TABLE_NAME + "."
                                + KusssContentContract.Assessment.COL_DATE
                                + " DESC");
            }
            default:
                return new CursorLoader(requireContext());
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case Consts.LOADER_ID_COURSES: {
                mCourseCursor = data;
                break;
            }
            case Consts.LOADER_ID_ASSESSMENTS: {
                mAssessmentCursor = data;
                break;
            }
            default:
                break;
        }
        // fill adapter
        setData(mAdapter, mCourseCursor, mAssessmentCursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case Consts.LOADER_ID_COURSES: {
                mCourseCursor = null;
                break;
            }
            case Consts.LOADER_ID_ASSESSMENTS: {
                mAssessmentCursor = null;
                break;
            }
            default:
                break;
        }
        setData(mAdapter, mCourseCursor, mAssessmentCursor);
    }

    private void setData(CourseListAdapter adapter, Cursor courseCursor, Cursor assessmentCursor) {
        adapter.clear();

        if (courseCursor != null) {
            boolean withAssessmentOnly = PreferenceHelper.getShowCoursesWithAssessmentOnly(requireContext());
            // load courses
            List<Course> courses = KusssContentProvider.getCoursesFromCursor(getContext(), courseCursor);

            // sort courses
            AppUtils.sortCourses(courses);

            // load assessments
            List<Assessment> assessments = KusssContentProvider.getAssessmentsFromCursor(getContext(), assessmentCursor);

            // generate data
            List<LvaWithGrade> lvasWithGrades = AppUtils.getGradesWithLva(getTerms(), courses, assessments, withAssessmentOnly, KusssContentProvider.getLastTerm(getContext()));

            // set data
            adapter.addAll(lvasWithGrades);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferenceHelper.PREF_COURSES_SHOW_WITH_ASSESSMENT_ONLY)) {
            onContentChanged(false);
        }
    }
}
