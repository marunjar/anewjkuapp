package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.MensaMenuAdapter;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.kusss.mensa.Mensa;
import org.voidsink.anewjkuapp.kusss.mensa.MenuLoader;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public abstract class MensaFragmentDetail extends BaseFragment {

	public static final String TAG = MensaFragmentDetail.class.getSimpleName();
	private ListView mListView;
	private MensaMenuAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mensa_detail, container,
				false);

		mListView = (ListView) view.findViewById(R.id.menu_list);
		mAdapter = new MensaMenuAdapter(getContext(),
				android.R.layout.simple_list_item_1);
		mListView.setAdapter(mAdapter);

		new MenuLoadTask().execute();

		return view;
	}

	private class MenuLoadTask extends AsyncTask<String, Void, Void> {
		private Mensa mensa;
		private ProgressDialog progressDialog;
		private Context mContext;

		@Override
		protected Void doInBackground(String... urls) {
			mensa = createLoader().getMensa(mContext);

			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mContext = MensaFragmentDetail.this.getContext();
			if (mContext == null) {
				Log.e(TAG, "context is null");
			}
			
			progressDialog = ProgressDialog.show(mContext, mContext
					.getString(R.string.progress_title), mContext
					.getString(R.string.progress_load_menu), true);
		}

		@Override
		protected void onPostExecute(Void result) {
			mAdapter.clear();
			mAdapter.addMensa(mensa);
			mAdapter.notifyDataSetChanged();
			progressDialog.dismiss();

			super.onPostExecute(result);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	protected abstract MenuLoader createLoader();

}
