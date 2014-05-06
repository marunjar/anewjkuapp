package org.voidsink.anewjkuapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.voidsink.anewjkuapp.ImportLvaTask;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.LvaListAdapter;
import org.voidsink.anewjkuapp.LvaListItem;
import org.voidsink.anewjkuapp.LvaListLva;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.base.BaseFragment;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class LvaFragment extends BaseFragment {

	private static final String TAG = LvaFragment.class.getSimpleName();
	private ListView mListView;
	private LvaListAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_lva, container, false);

		mListView = (ListView) view.findViewById(R.id.lva_list);
		mAdapter = new LvaListAdapter(mContext);
		mListView.setAdapter(mAdapter);

		new LvaLoadTask().execute();

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

//	@Override
//	protected boolean onRefreshSelected(MenuItem item) {
//		Looper.prepare();
//
//		Log.d(TAG, "importing LVAs");
//
//		ImportLvaTask lvaTask = new ImportLvaTask(
//				MainActivity.getAccount(mContext), mContext);
//		lvaTask.execute();
//		while (!lvaTask.isDone()) {
//			try {
//				Thread.sleep(600);
//			} catch (Exception e) {
//			}
//		}
//		return true;
//	}

	private class LvaLoadTask extends AsyncTask<String, Void, Void> {
		private ProgressDialog progressDialog;
		private List<LvaListItem> mLvas;

		@Override
		protected Void doInBackground(String... urls) {
			Account mAccount = MainActivity.getAccount(mContext);
			if (mAccount != null) {
				ContentResolver cr = mContext.getContentResolver();
				Cursor c = cr.query(KusssContentContract.Lva.CONTENT_URI,
						ImportLvaTask.LVA_PROJECTION, null, null,
						KusssContentContract.Lva.LVA_COL_TERM + " DESC");

				if (c != null) {
					while (c.moveToNext()) {
						mLvas.add(new LvaListLva(c
								.getString(ImportLvaTask.COLUMN_LVA_TERM), c
								.getInt(ImportLvaTask.COLUMN_LVA_LVANR), c
								.getString(ImportLvaTask.COLUMN_LVA_TITLE), c
								.getInt(ImportLvaTask.COLUMN_LVA_SKZ), c
								.getString(ImportLvaTask.COLUMN_LVA_TYPE)));

					}
					c.close();
				}
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLvas = new ArrayList<LvaListItem>();
			progressDialog = ProgressDialog.show(mContext,
					getString(R.string.progress_title),
					getString(R.string.progress_load_lva), true);
		}

		@Override
		protected void onPostExecute(Void result) {
			mAdapter.clear();
			mAdapter.addAll(LvaListAdapter.insertSections(mLvas));
			mAdapter.notifyDataSetChanged();
			progressDialog.dismiss();

			super.onPostExecute(result);
		}
	}

}
