package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.service.KusssService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;

public class KusssServiceFragment extends Fragment {

	private KusssService mKusssService;
	private boolean mIsBound = false;
	protected Context mContext = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.mContext = this.getActivity();
	}

	@Override
	public void onStart() {
		super.onStart();

		bindKusssService();
	}
	

	@Override
	public void onDestroy() {
		unBindKusssService();

		super.onDestroy();
	}
	

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mKusssService = ((KusssService.KusssServiceBinder) service)
					.getService();
			mIsBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mKusssService = null;
			mIsBound = false;
		}
	};

	protected void bindKusssService() {
		mContext.getApplicationContext().bindService(
				new Intent(mContext, KusssService.class),
				mConnection, Context.BIND_AUTO_CREATE);
	}

	protected void unBindKusssService() {
		if (mIsBound) {
			mContext.unbindService(mConnection);
			mIsBound = false;
		}
	}

	protected KusssService getKusssService() {
		if (mIsBound) {
			return mKusssService;
		}
		return null;
	}

}
