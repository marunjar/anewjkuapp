package org.voidsink.anewjkuapp.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.UriMatcher;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.AppUtils;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.LvaTabItem;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.StatTabItem;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

public class StatFragment extends SlidingTabsFragment implements
		ContentObserverListener {

	private static final String TAG = StatFragment.class.getSimpleName();
	private BaseContentObserver mDataObserver;

    private List<Lva> mLvas = new ArrayList<Lva>();
    private List<ExamGrade> mGrades = new ArrayList<ExamGrade>();
    private ArrayList<String> mTerms = new ArrayList<String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		loadData(getContext());

		return v;
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
    protected void fillTabs(List<SlidingTabItem> mTabs) {
        mTabs.add(new StatTabItem(getString(R.string.all_terms), this.mTerms, this.mLvas, this.mGrades));

        for (String term: mTerms) {
            mTabs.add(new StatTabItem(term, this.mLvas, this.mGrades));
        }
    }

    @Override
	public void onDestroy() {
		super.onDestroy();

		getActivity().getContentResolver().unregisterContentObserver(
                mDataObserver);
	}

	private void loadData(final Context context) {
		
		new AsyncTask<Void, Void, Void>() {

			private ProgressDialog progressDialog;
			private List<Lva> lvas;
			private List<ExamGrade> grades;
			private ArrayList<String> terms;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();

				progressDialog = ProgressDialog.show(context,
						context.getString(R.string.progress_title),
						context.getString(R.string.progress_load_lva), true);
			}

			@Override
			protected Void doInBackground(Void... params) {
				this.lvas = KusssContentProvider.getLvas(context);
				this.grades = KusssContentProvider.getGrades(context);
				this.terms = new ArrayList<String>();
				for (Lva lva : this.lvas) {
					if (this.terms.indexOf(lva.getTerm()) < 0) {
						this.terms.add(lva.getTerm());
					}
				}
				Collections.sort(this.terms, new Comparator<String>() {

					@Override
					public int compare(String lhs, String rhs) {
						return rhs.compareTo(lhs);
					}
				});
				AppUtils.sortLVAs(this.lvas);

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				progressDialog.dismiss();

                mLvas = this.lvas;
                mGrades = this.grades;
                mTerms = this.terms;

                updateData();

				super.onPostExecute(result);
			}
		}.execute();
	}

	@Override
	public void onContentChanged(boolean selfChange) {
		Log.i(TAG, "onContentChanged(" + selfChange + ")");
		loadData(getActivity());
	}
}
