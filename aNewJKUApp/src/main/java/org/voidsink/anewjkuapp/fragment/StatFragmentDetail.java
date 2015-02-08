package org.voidsink.anewjkuapp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.StatCard;
import org.voidsink.anewjkuapp.StatCardAdapter;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.update.UpdateService;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.List;

@SuppressLint("ValidFragment")
public class StatFragmentDetail extends BaseFragment implements
        ContentObserverListener {

    private BaseContentObserver mDataObserver;
    private final List<String> mTerms;
    private StatCardAdapter mAdapter;

    public StatFragmentDetail() {
        this(null);
    }

    public StatFragmentDetail(List<String> terms) {
        super();

        this.mTerms = terms;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        final GridView mGridView = (GridView) view.findViewById(R.id.stat_cards);
        mAdapter = new StatCardAdapter(getContext());
        mGridView.setAdapter(mAdapter);

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
        inflater.inflate(R.menu.stats, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_stats:
                Intent mUpdateService = new Intent(getActivity(), UpdateService.class);
                mUpdateService.putExtra(Consts.ARG_UPDATE_KUSSS_LVAS, true);
                mUpdateService.putExtra(Consts.ARG_UPDATE_KUSSS_GRADES, true);
                getActivity().startService(mUpdateService);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onContentChanged(boolean selfChange) {
        loadData();
    }

    private void loadData() {

        new AsyncTask<Void, Void, Void>() {

            //            private ProgressDialog progressDialog;
            private List<Lva> lvas;
            private List<ExamGrade> grades;
            private Context mContext = getContext();
            public boolean positiveOnly;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

//                progressDialog = ProgressDialog.show(context,
//                        context.getString(R.string.progress_title),
//                        context.getString(R.string.progress_load_lva), true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                this.positiveOnly = PreferenceWrapper.getPositiveGradesOnly(getContext());
                this.lvas = KusssContentProvider.getLvas(mContext);
                this.grades = AppUtils.filterGrades(mTerms, KusssContentProvider.getGrades(mContext));
                AppUtils.sortLVAs(this.lvas);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (mAdapter != null) {
                    mAdapter.clear();

                    mAdapter.add(StatCard.getGradeInstance(mTerms, this.grades, true, this.positiveOnly));
                    mAdapter.add(StatCard.getGradeInstance(mTerms, this.grades, false, this.positiveOnly));
                    mAdapter.add(StatCard.getLvaInstance(mTerms, this.lvas, this.grades));

                    mAdapter.notifyDataSetChanged();
                }
//                progressDialog.dismiss();

                super.onPostExecute(result);
            }
        }.execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Lva.PATH_CONTENT_CHANGED, 0);
        uriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Grade.PATH_CONTENT_CHANGED, 1);

        mDataObserver = new BaseContentObserver(uriMatcher, this);
        getActivity().getContentResolver().registerContentObserver(
                KusssContentContract.Lva.CONTENT_CHANGED_URI, false,
                mDataObserver);
        getActivity().getContentResolver().registerContentObserver(
                KusssContentContract.Grade.CONTENT_CHANGED_URI, false,
                mDataObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().getContentResolver().unregisterContentObserver(
                mDataObserver);
    }

}
