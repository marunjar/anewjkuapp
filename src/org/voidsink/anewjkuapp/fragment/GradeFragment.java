package org.voidsink.anewjkuapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.voidsink.anewjkuapp.GradeListAdapter;
import org.voidsink.anewjkuapp.GradeListItem;
import org.voidsink.anewjkuapp.ImportGradeTask;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class GradeFragment extends BaseFragment {

	private ListView mListView;
	private GradeListAdapter mAdapter;
	private ContentObserver mGradeObserver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_grade, container, false);

		mListView = (ListView) view.findViewById(R.id.grade_list);
		mAdapter = new GradeListAdapter(mContext);
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGradeObserver = new GradeContentObserver(new Handler());
		getActivity().getContentResolver().registerContentObserver(
				KusssContentContract.Grade.CONTENT_URI, false, mGradeObserver);
	}

	@Override
	public void onDestroy() {
		getActivity().getContentResolver().unregisterContentObserver(
				mGradeObserver);

		super.onDestroy();
	}

	private class GradeLoadTask extends AsyncTask<String, Void, Void> {
		private ProgressDialog progressDialog;
		private List<GradeListItem> mGrades;

		@Override
		protected Void doInBackground(String... urls) {
			List<ExamGrade> examGrades = KusssContentProvider.getGrades(mContext);
			for (ExamGrade examGrade : examGrades) {
				mGrades.add(examGrade);
			}
			examGrades = null;
			
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mGrades = new ArrayList<GradeListItem>();
			progressDialog = ProgressDialog.show(mContext,
					getString(R.string.progress_title),
					getString(R.string.progress_load_exam), true);
		}

		@Override
		protected void onPostExecute(Void result) {
			mAdapter.clear();
			mAdapter.addAll(GradeListAdapter.insertSections(mGrades));
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
