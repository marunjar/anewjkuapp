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

import org.voidsink.anewjkuapp.GradeCard;
import org.voidsink.anewjkuapp.GradeCardArrayAdapter;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.view.StickyCardListView;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;

@SuppressLint("ValidFragment")
public class GradeDetailFragment extends BaseFragment implements
        ContentObserverListener {

    public static final String TAG = GradeDetailFragment.class.getSimpleName();
    private final List<String> mTerms;

    private BaseContentObserver mGradeObserver;
    private GradeCardArrayAdapter mAdapter;

    public GradeDetailFragment() {
        this(null);
    }

    public GradeDetailFragment(List<String> terms) {
        super();

        this.mTerms = terms;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_grade_detail, container,
                false);

        final StickyCardListView mListView = (StickyCardListView) view.findViewById(R.id.grade_card_list);

        mAdapter = new GradeCardArrayAdapter(getContext(), new ArrayList<Card>());
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.grade, menu);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadData();
    }

    private void loadData() {

        new AsyncTask<Void, Void, Void>() {

            //            private ProgressDialog progressDialog;
            private List<ExamGrade> grades;
            private Context mContext = getContext();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

//                progressDialog = ProgressDialog.show(context,
//                        context.getString(R.string.progress_title),
//                        context.getString(R.string.progress_load_grade), true);

                grades = new ArrayList<>();
            }

            @Override
            protected Void doInBackground(Void... params) {
                this.grades = AppUtils.filterGrades(mTerms, KusssContentProvider.getGrades(mContext));

                AppUtils.sortGrades(grades);

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
//                progressDialog.dismiss();

                List<Card> mGradeCards = new ArrayList<>();
                for (ExamGrade grade : this.grades) {
                    mGradeCards.add(new GradeCard(mContext, grade));
                }

                mAdapter.clear();
                mAdapter.addAll(mGradeCards);
                mAdapter.notifyDataSetChanged();

                super.onPostExecute(result);
            }
        }.execute();


    }

    @Override
    public void onContentChanged(boolean selfChange) {
        loadData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(KusssContentContract.AUTHORITY,
                KusssContentContract.Grade.PATH_CONTENT_CHANGED, 0);

        mGradeObserver = new BaseContentObserver(uriMatcher, this);
        getActivity().getContentResolver().registerContentObserver(
                KusssContentContract.Grade.CONTENT_CHANGED_URI, false,
                mGradeObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().getContentResolver().unregisterContentObserver(mGradeObserver);
    }
}
