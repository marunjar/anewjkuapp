/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2018 Paul "Marunjar" Pretsch
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
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.kusss.Curriculum;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.update.ImportCurriculaTask;
import org.voidsink.anewjkuapp.update.UpdateService;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.sectionedrecycleradapter.SectionedAdapter;
import org.voidsink.sectionedrecycleradapter.SectionedRecyclerViewAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class CurriculaFragment extends BaseFragment implements
        ContentObserverListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
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
        getActivity().getContentResolver().registerContentObserver(
                KusssContentContract.Curricula.CONTENT_CHANGED_URI, false,
                mObserver);
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().getContentResolver().unregisterContentObserver(
                mObserver);
        mObserver = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mRecyclerView = view.findViewById(R.id.recyclerView);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new CurriculaAdapter(getContext());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mRecyclerView.setAdapter(new SectionedRecyclerViewAdapter(mRecyclerView, mAdapter));

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.curricula, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_curricula: {
                Intent mUpdateService = new Intent(getActivity(), UpdateService.class);
                mUpdateService.putExtra(Consts.ARG_UPDATE_KUSSS_CURRICULA, true);
                getActivity().startService(mUpdateService);

                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onContentChanged(boolean selfChange) {
        getLoaderManager().restartLoader(0, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), KusssContentContract.Curricula.CONTENT_URI,
                ImportCurriculaTask.CURRICULA_PROJECTION, null, null,
                KusssContentContract.Curricula.COL_DT_START + " DESC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mAdapter.clear();

        List<Curriculum> mCurriculum = new ArrayList<>();
        if (data != null) {
            Account mAccount = AppUtils.getAccount(getContext());
            if (mAccount != null) {
                mCurriculum = KusssContentProvider.getCurriculaFromCursor(data);
            }
        }
        mAdapter.addAll(mCurriculum);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    private static class CurriculaAdapter extends RecyclerArrayAdapter<Curriculum, CurriculumViewHolder> implements SectionedAdapter<CurriculumHeaderHolder> {

        public CurriculaAdapter(Context context) {
            super(context);
        }

        @NonNull
        @Override
        public CurriculumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.curricula_list_item, parent, false);
            return new CurriculumViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CurriculumViewHolder holder, int position) {
            Curriculum item = getItem(position);

            holder.isStandard.setText(item.isStandard() ? getContext().getString(R.string.curriculum_is_standard_yes) : getContext().getString(R.string.curriculum_is_standard_no));
            holder.cid.setText(item.getCid());
            holder.title.setText(item.getTitle());
            holder.steopDone.setText(item.isSteopDone() ? getContext().getString(R.string.curriculum_steop_done_yes) : getContext().getString(R.string.curriculum_steop_done_no));
            holder.activeStatus.setText(item.isActive() ? getContext().getString(R.string.curriculum_active_status_yes) : getContext().getString(R.string.curriculum_active_status_no));
            if (item.getDtStart() != null) {
                holder.dtStart.setText(dateFormat.format(item.getDtStart()));
            }
            if (item.getDtEnd() != null) {
                holder.dtEnd.setText(dateFormat.format(item.getDtEnd()));
            }
        }

        @Override
        public long getHeaderId(int i) {
            Curriculum curriculum = getItem(i);
            if (curriculum != null) {
                return (long) curriculum.getUni().hashCode() + (long) Integer.MAX_VALUE; // header id has to be > 0???
            }
            return 0;
        }

        @Override
        public CurriculumHeaderHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_header, viewGroup, false);
            return new CurriculumHeaderHolder(v);
        }

        @Override
        public void onBindHeaderViewHolder(CurriculumHeaderHolder curriculumHeaderHolder, int position) {
            Curriculum curriculum = getItem(position);
            curriculumHeaderHolder.mText.setText(curriculum.getUni());
        }
    }

    public static class CurriculumViewHolder extends RecyclerView.ViewHolder {
        final TextView isStandard;
        final TextView cid;
        final TextView title;
        final TextView steopDone;
        final TextView activeStatus;
        final TextView dtStart;
        final TextView dtEnd;

        CurriculumViewHolder(View itemView) {
            super(itemView);

            isStandard = itemView.findViewById(R.id.curriculum_is_standard);
            cid = itemView.findViewById(R.id.curriculum_id);
            title = itemView.findViewById(R.id.curriculum_title);
            steopDone = itemView.findViewById(R.id.curriculum_steop_done);
            activeStatus = itemView.findViewById(R.id.curriculum_active_status);
            dtStart = itemView.findViewById(R.id.curriculum_dt_start);
            dtEnd = itemView.findViewById(R.id.curriculum_dt_end);
        }
    }

    public static class CurriculumHeaderHolder extends RecyclerView.ViewHolder {
        final TextView mText;

        CurriculumHeaderHolder(View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.list_header_text);
        }
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_CURRICULA;
    }
}
