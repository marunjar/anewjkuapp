package org.voidsink.anewjkuapp.service;

import org.voidsink.anewjkuapp.kusss.KusssHandler;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class KusssService extends Service {

	private final IBinder mBinder = new KusssServiceBinder();
	public KusssHandler mKusssHandler;
	
	public class KusssServiceBinder extends Binder {
		public KusssService getService() {
			return KusssService.this;
		}
	}
	
	@Override
	public void onCreate() {
		this.mKusssHandler = new KusssHandler();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(this.getClass().getCanonicalName(), "Received start id "
				+ startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public String getName() {
		return "";//this.mKusssHandler.getName();
	}

	public String getMatrNr() {
		return "";//this.mKusssHandler.getMatrNr();
	}
}
