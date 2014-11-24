package org.voidsink.anewjkuapp.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.UriMatcher;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.StatCardGrade;
import org.voidsink.anewjkuapp.StatCardLva;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.List;

import it.gmariotti.cardslib.library.view.CardView;

@SuppressLint("ValidFragment")
public class StatFragmentDetail extends BaseFragment implements
        ContentObserverListener {

    private BaseContentObserver mDataObserver;

    private StatCardLva mStatCardLva;
    private StatCardGrade mStatCardGrade;
    private StatCardGrade mStatCardGradeWeighted;

    private final List<String> mTerms;

    public StatFragmentDetail() {
        this(null);
    }

    public StatFragmentDetail(List<String> terms) {
        super();

        this.mTerms = terms;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_stats, container, false);

        boolean mPositiveOnly = PreferenceWrapper.getPositiveGradesOnly(getContext());

        CardView scl = (CardView) mView.findViewById(R.id.stat_card_lva);
        mStatCardLva = new StatCardLva(getContext());
        mStatCardLva.init();
        scl.setCard(mStatCardLva);

        CardView scg = (CardView) mView.findViewById(R.id.stat_card_grade);
        mStatCardGrade = new StatCardGrade(getContext(), false, mPositiveOnly);
        mStatCardGrade.init();
        scg.setCard(mStatCardGrade);

        CardView scgw = (CardView) mView.findViewById(R.id.stat_card_grade_weighted);
        mStatCardGradeWeighted = new StatCardGrade(getContext(), true, mPositiveOnly);
        mStatCardGradeWeighted.init();
        scgw.setCard(mStatCardGradeWeighted);



        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadData();
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

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

//                progressDialog = ProgressDialog.show(context,
//                        context.getString(R.string.progress_title),
//                        context.getString(R.string.progress_load_lva), true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                this.lvas = KusssContentProvider.getLvas(mContext);
                this.grades = AppUtils.filterGrades(mTerms, KusssContentProvider.getGrades(mContext));
                AppUtils.sortLVAs(this.lvas);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                mStatCardLva.setValues(mTerms, this.lvas, this.grades);
                mStatCardGrade.setValues(mTerms, this.grades);
                mStatCardGradeWeighted.setValues(mTerms, this.grades);

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
