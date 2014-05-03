package org.voidsink.anewjkuapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.voidsink.anewjkuapp.ExamListAdapter;
import org.voidsink.anewjkuapp.ExamListExam;
import org.voidsink.anewjkuapp.ExamListItem;
import org.voidsink.anewjkuapp.ImportExamTask;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.LvaMap;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.base.BaseFragment;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class NewExamFragment extends BaseFragment {

	private ListView mListView;
	private ExamListAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_exam, container, false);

		mListView = (ListView) view.findViewById(R.id.exam_list);
		mAdapter = new ExamListAdapter(mContext);
		mListView.setAdapter(mAdapter);

		new ExamLoadTask().execute();
		
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	private class ExamLoadTask extends AsyncTask<String, Void, Void> {
		private ProgressDialog progressDialog;
		private List<ExamListItem>  mExams;

		@Override
		protected Void doInBackground(String... urls) {
			Account mAccount = MainActivity.getAccount(mContext);
			if (mAccount != null) {
				LvaMap map = new LvaMap(mContext);
				
				ContentResolver cr = mContext.getContentResolver();
				Cursor c = cr.query(KusssContentContract.Exam.CONTENT_URI,
						ImportExamTask.EXAM_PROJECTION, null, null,
						KusssContentContract.Exam.EXAM_COL_DATE + " ASC");

				if (c != null) {
					while (c.moveToNext()) {
						mExams.add(new ExamListExam(c, map));
					}
					c.close();
				}
			}

			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mExams = new ArrayList<ExamListItem>();
			progressDialog = ProgressDialog.show(mContext,
					getString(R.string.progress_title),
					getString(R.string.progress_load_exam), true);
		}

		@Override
		protected void onPostExecute(Void result) {
			mAdapter.clear();
			mAdapter.addAll(mExams);
			progressDialog.dismiss();
			super.onPostExecute(result);
		}
	}
	
}
