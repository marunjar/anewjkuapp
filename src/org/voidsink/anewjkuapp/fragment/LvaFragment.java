package org.voidsink.anewjkuapp.fragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.voidsink.anewjkuapp.AppUtils;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.LvaDetailPageAdapter;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.base.IndicatedViewPagerFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;
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

public class LvaFragment extends IndicatedViewPagerFragment implements
		ContentObserverListener {

	private static final String TAG = LvaFragment.class.getSimpleName();
	private BaseContentObserver mLvaObserver;

	@Override
	protected PagerAdapter createPagerAdapter(FragmentManager fm) {
		return new LvaDetailPageAdapter(fm, getActivity());
	}

	@Override
	protected boolean useTabHost() {
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		loadLvas(getActivity());

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

	private void loadLvas(final Context context) {
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
						return lhs.compareTo(rhs) * -1;
					}
				});
				AppUtils.sortLVAs(this.lvas);

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				progressDialog.dismiss();

				// Log.i(TAG, "loadLvas" + this.terms);

				((LvaDetailPageAdapter) getPagerAdapter()).setData(this.lvas,
						this.grades, this.terms);

				super.onPostExecute(result);
			}
		}.execute();
	}

	@Override
	public void onContentChanged(boolean selfChange) {
		Log.i(TAG, "onContentChanged(" + selfChange + ")");
		loadLvas(getActivity());
	}

}
