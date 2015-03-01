package org.voidsink.anewjkuapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.kusss.Studies;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.update.UpdateService;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.List;

public class StudiesFragment extends BaseFragment implements
        ContentObserverListener {

    private StudiesAdapter mAdapter;
    private BaseContentObserver mStudiesObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Studies.PATH_CONTENT_CHANGED, 0);

        mStudiesObserver = new BaseContentObserver(uriMatcher, this);
        getActivity().getContentResolver().registerContentObserver(
                KusssContentContract.Studies.CONTENT_CHANGED_URI, false,
                mStudiesObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().getContentResolver().unregisterContentObserver(
                mStudiesObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        final RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mAdapter = new StudiesAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(mAdapter));

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.studies, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_studies: {
                Intent mUpdateService = new Intent(getActivity(), UpdateService.class);
                mUpdateService.putExtra(Consts.ARG_UPDATE_KUSSS_STUDIES, true);
                getActivity().startService(mUpdateService);

                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadData() {

        new AsyncTask<Void, Void, Void>() {

            public List<Studies> mStudies;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                mStudies = KusssContentProvider.getStudies(getContext());

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                mAdapter.clear();
                mAdapter.addAll(mStudies);
                mAdapter.notifyDataSetChanged();

                super.onPostExecute(aVoid);
            }
        }.execute();

    }

    @Override
    public void onContentChanged(boolean selfChange) {
        loadData();
    }

    private static class StudiesAdapter extends RecyclerArrayAdapter<Studies, StudiesFragment.StudiesViewHolder> implements StickyRecyclerHeadersAdapter<StudiesFragment.StudiesHeaderHolder> {

        private final Context mContext;

        public StudiesAdapter(Context context) {
            super();
            mContext = context;
        }

        @Override
        public StudiesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.studies_list_item, parent, false);
            return new StudiesViewHolder(v);
        }

        @Override
        public void onBindViewHolder(StudiesViewHolder holder, int position) {
            Studies item = getItem(position);

            holder.isStandard.setText(item.isStandard() ? mContext.getString(R.string.studies_is_standard_yes) : mContext.getString(R.string.studies_is_standard_no));
            holder.skz.setText(item.getSkz());
            holder.title.setText(item.getTitle());
            holder.steopDone.setText(item.isSteopDone() ? mContext.getString(R.string.studies_steop_done_yes) : mContext.getString(R.string.studies_steop_done_no));
            holder.activeStatus.setText(item.isActive() ? mContext.getString(R.string.studies_active_status_yes) : mContext.getString(R.string.studies_active_status_no));
            if (item.getDtStart() != null) {
                holder.dtStart.setText(item.dateFormat.format(item.getDtStart()));
            }
            if (item.getDtEnd() != null) {
                holder.dtEnd.setText(item.dateFormat.format(item.getDtEnd()));
            }
        }

        @Override
        public long getHeaderId(int i) {
            Studies studies = getItem(i);
            if (studies != null) {
                return studies.getUni().hashCode();
            }
            return 0;
        }

        @Override
        public StudiesHeaderHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_header, viewGroup, false);
            return new StudiesHeaderHolder(v);
        }

        @Override
        public void onBindHeaderViewHolder(StudiesHeaderHolder studiesHeaderHolder, int position) {
            Studies studies = getItem(position);
            studiesHeaderHolder.mText.setText(studies.getUni());
        }
    }

    public static class StudiesViewHolder extends RecyclerView.ViewHolder {
        public TextView isStandard;
        public TextView skz;
        public TextView title;
        public TextView steopDone;
        public TextView activeStatus;
        public TextView dtStart;
        public TextView dtEnd;

        public StudiesViewHolder(View itemView) {
            super(itemView);

            isStandard = (TextView) itemView.findViewById(R.id.studies_is_standard);
            skz = (TextView) itemView.findViewById(R.id.studies_skz);
            title = (TextView) itemView.findViewById(R.id.studies_title);
            steopDone = (TextView) itemView.findViewById(R.id.studies_steop_done);
            activeStatus = (TextView) itemView.findViewById(R.id.studies_active_status);
            dtStart = (TextView) itemView.findViewById(R.id.studies_dt_start);
            dtEnd = (TextView) itemView.findViewById(R.id.studies_dt_end);
        }
    }

    public static class StudiesHeaderHolder extends RecyclerView.ViewHolder {
        public TextView mText;

        public StudiesHeaderHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView.findViewById(R.id.list_header_text);
        }
    }
}
