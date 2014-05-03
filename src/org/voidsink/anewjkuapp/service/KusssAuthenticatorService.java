package org.voidsink.anewjkuapp.service;

import org.voidsink.anewjkuapp.KusssAuthenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KusssAuthenticatorService extends Service {

	private KusssAuthenticator mAuthenticator;

	@Override
	public void onCreate() {
		// Create a new authenticator object
		mAuthenticator = new KusssAuthenticator(this);
	}

	/*
	 * When the system binds to this Service to make the RPC call return the
	 * authenticator's IBinder.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mAuthenticator.getIBinder();
	}

}
