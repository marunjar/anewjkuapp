package org.voidsink.anewjkuapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.voidsink.anewjkuapp.ImportLvaTask;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.LvaListAdapter;
import org.voidsink.anewjkuapp.LvaListItem;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;
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

public class LvaFragment extends BaseFragment {

	// private static final String TAG = LvaFragment.class.getSimpleName();
	private ListView mListView;
	private LvaListAdapter mAdapter;
	private ContentObserver mLvaObserver;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLvaObserver = new LvaContentObserver(new Handler());
		getActivity().getContentResolver().registerContentObserver(
				KusssContentContract.Exam.CONTENT_URI, false, mLvaObserver);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.lva, menu);
	}

	@Override
	public void onDestroy() {
		getActivity().getContentResolver().unregisterContentObserver(
				mLvaObserver);

		super.onDestroy();
	}

	private class LvaLoadTask extends AsyncTask<String, Void, Void> {
		private ProgressDialog progressDialog;
		private List<LvaListItem> mLvas;

		@Override
		protected Void doInBackground(String... urls) {
			List<Lva> lvas = KusssContentProvider.getLvas(mContext);
			for (Lva lva : lvas) {
				mLvas.add(lva);
			}
			lvas = null;
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

	private class LvaContentObserver extends ContentObserver {

		public LvaContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			new LvaLoadTask().execute();
		}
	}

}
