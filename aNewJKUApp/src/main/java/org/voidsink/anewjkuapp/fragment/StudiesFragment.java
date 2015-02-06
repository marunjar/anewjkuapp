package org.voidsink.anewjkuapp.fragment;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.update.ImportStudiesTask;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.base.ListWithHeaderAdapter;
import org.voidsink.anewjkuapp.kusss.Studies;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.update.UpdateService;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.view.ListViewWithHeader;

import java.util.List;

/**
 * Created by paul on 24.11.2014.
 */
public class StudiesFragment extends BaseFragment implements
        ContentObserverListener {

    private ListWithHeaderAdapter mAdapter;
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_with_header, container, false);

        final ListViewWithHeader mListView = (ListViewWithHeader) view.findViewById(R.id.list_with_header);
        mAdapter = new StudiesAdapter(getContext());
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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
                mUpdateService.putExtra(UpdateService.UPDATE_TYPE, UpdateService.UPDATE_STUDIES);
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
                if (mStudies != null) {
                    mAdapter.addAll(mStudies);
                }
                mAdapter.notifyDataSetChanged();

                super.onPostExecute(aVoid);
            }
        }.execute();

    }

    @Override
    public void onContentChanged(boolean selfChange) {
        loadData();
    }

    private class StudiesAdapter extends ListWithHeaderAdapter<Studies> {

        private final LayoutInflater mInflater;

        public StudiesAdapter(Context context) {
            super(context, R.layout.studies_list_item);

            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getHeaderView(int i, View view, ViewGroup viewGroup) {
            Studies studies = getItem(i);
            if (studies != null && !TextUtils.isEmpty(studies.getUni())) {
                final View mHeaderView = mInflater.inflate(R.layout.list_header, null);
                final TextView mTitle = (TextView) mHeaderView.findViewById(R.id.list_header_text);

                mTitle.setText(studies.getUni());

                return mHeaderView;
            }
            return null;
        }

        @Override
        public long getHeaderId(int i) {
            Studies studies = getItem(i);
            if (studies != null && !TextUtils.isEmpty(studies.getUni())) {
                return studies.getUni().hashCode();
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            Studies item = this.getItem(position);
            if (item != null) {
                view = getStudiesView(convertView, parent, item);
            }
            return view;
        }

        private View getStudiesView(View convertView, ViewGroup parent, Studies item) {
            StudiesHolder holder = null;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.studies_list_item, parent, false);
                holder = new StudiesHolder();

                holder.isStandard = (TextView) convertView.findViewById(R.id.studies_is_standard);
                holder.skz = (TextView) convertView.findViewById(R.id.studies_skz);
                holder.title = (TextView) convertView.findViewById(R.id.studies_title);
                holder.steopDone = (TextView) convertView.findViewById(R.id.studies_steop_done);
                holder.activeStatus = (TextView) convertView.findViewById(R.id.studies_active_status);
                holder.dtStart = (TextView) convertView.findViewById(R.id.studies_dt_start);
                holder.dtEnd = (TextView) convertView.findViewById(R.id.studies_dt_end);

                convertView.setTag(holder);
            }
            if (holder == null) {
                holder = (StudiesHolder) convertView.getTag();
            }

            holder.isStandard.setText(item.isStandard() ? getString(R.string.studies_is_standard_yes) : getString(R.string.studies_is_standard_no));
            holder.skz.setText(item.getSkz());
            holder.title.setText(item.getTitle());
            holder.steopDone.setText(item.isSteopDone() ? getString(R.string.studies_steop_done_yes) : getString(R.string.studies_steop_done_no));
            holder.activeStatus.setText(item.isActive() ? getString(R.string.studies_active_status_yes) : getString(R.string.studies_active_status_no));
            if (item.getDtStart() != null) {
                holder.dtStart.setText(item.dateFormat.format(item.getDtStart()));
            }
            if (item.getDtEnd() != null) {
                holder.dtEnd.setText(item.dateFormat.format(item.getDtEnd()));
            }

            return convertView;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        private class StudiesHolder {
            public TextView isStandard;
            public TextView skz;
            public TextView title;
            public TextView steopDone;
            public TextView activeStatus;
            public TextView dtStart;
            public TextView dtEnd;
        }
    }
}
