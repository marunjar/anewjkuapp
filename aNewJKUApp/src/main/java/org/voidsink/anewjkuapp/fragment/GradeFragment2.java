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
import org.voidsink.anewjkuapp.GradeTabItem;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Collections;

public class GradeFragment2 extends SlidingTabsFragment implements
		ContentObserverListener {

	private static final String TAG = GradeFragment2.class.getSimpleName();
	private BaseContentObserver mGradeObserver;

    private List<ExamGrade> mGrades = new ArrayList<ExamGrade>();
    private List<String> mTerms = new ArrayList<String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		loadGrades(getActivity());

		return v;
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
    protected void fillTabs(List<SlidingTabItem> mTabs) {
        mTabs.add(new GradeTabItem("alle Semester", null, this.mGrades));

        for (String term: mTerms) {
            mTabs.add(new GradeTabItem(term, term, this.mGrades));
        }
    }

    @Override
	public void onDestroy() {
		super.onDestroy();

		getActivity().getContentResolver().unregisterContentObserver(
				mGradeObserver);
	}

	private void loadGrades(final Context context) {

		new AsyncTask<Void, Void, Void>() {

			private ProgressDialog progressDialog;
			private List<ExamGrade> grades;
			private List<String> terms;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();

				progressDialog = ProgressDialog.show(context,
						context.getString(R.string.progress_title),
						context.getString(R.string.progress_load_grade), true);
			}

			@Override
			protected Void doInBackground(Void... params) {
				this.grades = KusssContentProvider.getGrades(context);
				this.terms = new ArrayList<String>();
				for (ExamGrade grade : this.grades) {
					if (!grade.getTerm().isEmpty() && this.terms.indexOf(grade.getTerm()) < 0) {
						this.terms.add(grade.getTerm());
					}
				}
				
				Collections.sort(this.terms, new Comparator<String>() {

					@Override
					public int compare(String lhs, String rhs) {
						return lhs.compareTo(rhs) * -1;
					}
				});
				AppUtils.sortGrades(grades);

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				progressDialog.dismiss();

				Log.i(TAG, "loadGrades " + this.terms);

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
		loadGrades(getActivity());
	}

}
