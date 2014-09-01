package org.voidsink.anewjkuapp.fragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.voidsink.anewjkuapp.AppUtils;
import org.voidsink.anewjkuapp.GradeDetailPageAdapter;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.base.IndicatedViewPagerFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;

import edu.emory.mathcs.backport.java.util.Collections;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.UriMatcher;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GradeFragment extends IndicatedViewPagerFragment implements
		ContentObserverListener {

	private static final String TAG = GradeFragment.class.getSimpleName();
	private BaseContentObserver mGradeObserver;

	@Override
	protected PagerAdapter createPagerAdapter(FragmentManager fm) {
		return new GradeDetailPageAdapter(fm, getActivity());
	}

	@Override
	protected boolean useTabHost() {
		return false;
	}

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
	public void onDestroy() {
		super.onDestroy();

		getActivity().getContentResolver().unregisterContentObserver(
				mGradeObserver);
	}

	private void loadGrades(final Context context) {

		new AsyncTask<Void, Void, Void>() {

			private ProgressDialog progressDialog;
			private List<ExamGrade> grades;
			private ArrayList<String> terms;

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

				((GradeDetailPageAdapter) getPagerAdapter()).setData(
						this.grades, this.terms);

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
