package org.voidsink.anewjkuapp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.UriMatcher;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.LvaCard;
import org.voidsink.anewjkuapp.LvaCardArrayAdapter;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.view.StickyCardListView;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;

@SuppressLint("ValidFragment")
public class LvaDetailFragment extends BaseFragment implements
        ContentObserverListener {

    private BaseContentObserver mLvaObserver;
    private List<String> mTerms;
    private LvaCardArrayAdapter mAdapter;

    public LvaDetailFragment() {
        this(null);
    }

    public LvaDetailFragment(List<String> terms) {
        super();

        this.mTerms = terms;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_lva_detail, container,
                false);

        final StickyCardListView mListView = (StickyCardListView) view.findViewById(R.id.lva_card_list);

        mAdapter = new LvaCardArrayAdapter(getContext(), new ArrayList<Card>());
        mListView.setAdapter(mAdapter);

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

            List<Lva> lvas;
            List<ExamGrade> grades;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

//                progressDialog = ProgressDialog.show(mContext,
//                        mContext.getString(R.string.progress_title),
//                        mContext.getString(R.string.progress_load_lva), true);

                this.lvas = new ArrayList<>();
                this.grades = new ArrayList<>();
            }

            @Override
            protected Void doInBackground(Void... params) {
                this.lvas = KusssContentProvider.getLvas(mContext);
                this.grades = KusssContentProvider.getGrades(mContext);
                AppUtils.sortLVAs(this.lvas);

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                // Log.i(TAG, "loadLvas" + this.terms);

                List<LvaWithGrade> mLvasWithGrades = AppUtils.getLvasWithGrades(mTerms, lvas, grades);
                List<Card> mLvaCards = new ArrayList<>();

                for (LvaWithGrade lvaWithGrade : mLvasWithGrades) {
                    mLvaCards.add(new LvaCard(mContext, lvaWithGrade));
                }

                if (mAdapter != null) {
                    mAdapter.clear();
                    mAdapter.addAll(mLvaCards);
                    mAdapter.notifyDataSetChanged();
                }
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
