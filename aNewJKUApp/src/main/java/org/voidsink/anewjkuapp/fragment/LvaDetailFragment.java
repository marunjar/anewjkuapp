package org.voidsink.anewjkuapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.LvaListAdapter;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.base.TermFragment;
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.update.UpdateService;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.List;

public class LvaDetailFragment extends TermFragment implements
        ContentObserverListener {

    private BaseContentObserver mLvaObserver;
    private LvaListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        final RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new LvaListAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(mAdapter));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadData();
    }

    private void loadData() {

        new AsyncTask<Void, Void, Void>() {

            private Context mContext = getContext();
//            private ProgressDialog progressDialog;

            List<Course> courses;
            List<Assessment> grades;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

//                progressDialog = ProgressDialog.show(mContext,
//                        mContext.getString(R.string.progress_title),
//                        mContext.getString(R.string.progress_load_lva), true);

                this.courses = new ArrayList<>();
                this.grades = new ArrayList<>();
            }

            @Override
            protected Void doInBackground(Void... params) {
                this.courses = KusssContentProvider.getLvas(mContext);
                this.grades = KusssContentProvider.getGrades(mContext);
                AppUtils.sortLVAs(this.courses);

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                // Log.i(TAG, "loadLvas" + this.terms);

                List<LvaWithGrade> mLvasWithGrades = AppUtils.getLvasWithGrades(getTerms(), courses, grades);

                mAdapter.clear();
                mAdapter.addAll(mLvasWithGrades);
                mAdapter.notifyDataSetChanged();

//                progressDialog.dismiss();

                super.onPostExecute(result);
            }
        }.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.lva, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_lvas:
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Lva.PATH_CONTENT_CHANGED, 0);
        uriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Grade.PATH_CONTENT_CHANGED, 1);

        mLvaObserver = new BaseContentObserver(uriMatcher, this);
        getActivity().getContentResolver().registerContentObserver(
                KusssContentContract.Lva.CONTENT_CHANGED_URI, false,
                mLvaObserver);
        getActivity().getContentResolver().registerContentObserver(
                KusssContentContract.Grade.CONTENT_CHANGED_URI, false,
                mLvaObserver);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().getContentResolver().unregisterContentObserver(
                mLvaObserver);
    }

    @Override
    public void onContentChanged(boolean selfChange) {
        loadData();
    }
}
