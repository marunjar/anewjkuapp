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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.voidsink.anewjkuapp.AssessmentListAdapter;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.base.TermFragment;
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.sectionedrecycleradapter.SectionedRecyclerViewAdapter;

import java.util.List;

public class AssessmentDetailFragment extends TermFragment implements
        ContentObserverListener, LoaderManager.LoaderCallbacks<Cursor> {

    private BaseContentObserver mObserver;
    private AssessmentListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container,
                false);

        mRecyclerView = view.findViewById(R.id.recyclerView);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.assessment, menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mAdapter = new AssessmentListAdapter(getContext());
        mRecyclerView.setAdapter(new SectionedRecyclerViewAdapter<>(mRecyclerView, mAdapter));
        mRecyclerView.setContentDescription(getTitle(getContext()));

        LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_assessments:
                AppUtils.triggerSync(getContext(), true, Consts.ARG_WORKER_KUSSS_ASSESSMENTS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onContentChanged(boolean selfChange) {
        LoaderManager.getInstance(this).restartLoader(0, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Assessment.PATH_CONTENT_CHANGED, 0);

        mObserver = new BaseContentObserver(uriMatcher, this);
        requireContext().getContentResolver().registerContentObserver(
                KusssContentContract.Assessment.CONTENT_CHANGED_URI, false,
                mObserver);
    }

    @Override
    public void onStop() {
        super.onStop();

        requireContext().getContentResolver().unregisterContentObserver(mObserver);
        mObserver = null;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(requireContext(), KusssContentContract.Assessment.CONTENT_URI,
                KusssContentContract.Assessment.DB.PROJECTION, null, null,
                KusssContentContract.Assessment.TABLE_NAME + "."
                        + KusssContentContract.Assessment.COL_TYPE
                        + " ASC,"
                        + KusssContentContract.Assessment.TABLE_NAME + "."
                        + KusssContentContract.Assessment.COL_DATE
                        + " DESC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mAdapter.clear();

        List<Assessment> assessments = AppUtils.filterAssessments(getTerms(), KusssContentProvider.getAssessmentsFromCursor(getContext(), data));

        AppUtils.sortAssessments(assessments);

        mAdapter.addAll(assessments);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }
}
