package org.voidsink.anewjkuapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.voidsink.anewjkuapp.AppUtils;
import org.voidsink.anewjkuapp.GradeListAdapter;
import org.voidsink.anewjkuapp.GradeListItem;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class GradeFragment extends BaseFragment {

	public static final String TAG = GradeFragment.class.getSimpleName();
	
	private ListView mListView;
	private GradeListAdapter mAdapter;
	private ContentObserver mGradeObserver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_grade, container, false);

		mListView = (ListView) view.findViewById(R.id.grade_list);
		mAdapter = new GradeListAdapter(getContext());
		mListView.setAdapter(mAdapter);

		new GradeLoadTask().execute();

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.grade, menu);
	}

	@Override
	public void onStart() {
		super.onStart();

		mGradeObserver = new GradeContentObserver(new Handler());
		getActivity().getContentResolver().registerContentObserver(
				KusssContentContract.Grade.CONTENT_CHANGED_URI, false, mGradeObserver);
	}

	@Override
	public void onStop() {
		getActivity().getContentResolver().unregisterContentObserver(
				mGradeObserver);

		super.onStop();
	}

	private class GradeLoadTask extends AsyncTask<String, Void, Void> {
		private ProgressDialog progressDialog;
		private List<ExamGrade> mGrades;
		private Context mContext;

		@Override
		protected Void doInBackground(String... urls) {
			List<ExamGrade> examGrades = KusssContentProvider
					.getGrades(mContext);
			for (ExamGrade examGrade : examGrades) {
				mGrades.add(examGrade);
			}
			examGrades = null;

			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mContext = GradeFragment.this.getContext();
			if (mContext == null) {
				Log.e(TAG, "context is null");
			}
			
			mGrades = new ArrayList<ExamGrade>();
			progressDialog = ProgressDialog.show(mContext,
					mContext.getString(R.string.progress_title),
					mContext.getString(R.string.progress_load_exam), true);
		}

		@Override
		protected void onPostExecute(Void result) {
			AppUtils.sortGrades(mGrades);
			List<GradeListItem> listItems = new ArrayList<GradeListItem>(
					mGrades);
			mAdapter.clear();
			mAdapter.addAll(GradeListAdapter.insertSections(listItems));
			progressDialog.dismiss();
			super.onPostExecute(result);
		}
	}

	private class GradeContentObserver extends ContentObserver {

		public GradeContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			new GradeLoadTask().execute();
		}
	}
}
