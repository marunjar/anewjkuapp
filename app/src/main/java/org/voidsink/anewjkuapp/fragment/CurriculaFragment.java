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

import org.voidsink.anewjkuapp.CurriculaAdapter;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.kusss.Curriculum;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.sectionedrecycleradapter.SectionedRecyclerViewAdapter;

import java.util.List;

public class CurriculaFragment extends BaseFragment implements
        ContentObserverListener, LoaderManager.LoaderCallbacks<Cursor> {

    private CurriculaAdapter mAdapter;
    private BaseContentObserver mObserver;
    private RecyclerView mRecyclerView;

    @Override
    public void onStart() {
        super.onStart();

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Curricula.PATH_CONTENT_CHANGED, 0);

        mObserver = new BaseContentObserver(uriMatcher, this);
        requireContext().getContentResolver().registerContentObserver(
                KusssContentContract.Curricula.CONTENT_CHANGED_URI, false,
                mObserver);
    }

    @Override
    public void onStop() {
        super.onStop();

        requireContext().getContentResolver().unregisterContentObserver(
                mObserver);
        mObserver = null;
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mRecyclerView = view.findViewById(R.id.recyclerView);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mAdapter = new CurriculaAdapter(getContext());
        mRecyclerView.setAdapter(new SectionedRecyclerViewAdapter<>(mRecyclerView, mAdapter));
        mRecyclerView.setContentDescription(getTitle(getContext()));

        LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.curricula, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh_curricula) {
            AppUtils.triggerSync(getContext(), true, Consts.ARG_WORKER_KUSSS_CURRICULA);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onContentChanged(boolean selfChange) {
        LoaderManager.getInstance(this).restartLoader(0, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(requireContext(), KusssContentContract.Curricula.CONTENT_URI,
                KusssContentContract.Curricula.DB.getProjection(), null, null,
                KusssContentContract.Curricula.COL_DT_START + " DESC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mAdapter.clear();

        if (data != null && AppUtils.getAccount(getContext()) != null) {
            List<Curriculum> mCurriculum = KusssContentProvider.getCurriculaFromCursor(data);
            mAdapter.addAll(mCurriculum);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    @Nullable
    protected String getScreenName() {
        return Consts.SCREEN_CURRICULA;
    }
}
