package org.voidsink.anewjkuapp;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;

import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.analytics.Logger.LogLevel;

import android.app.Application;
import android.util.Log;

public class Globals extends Application {

	public enum TrackerName {
		APP_TRACKER
	}

	private static final String TAG = Globals.class.getSimpleName();
	
	@Override
	public void onCreate() {
		super.onCreate();

		GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
		if (BuildConfig.DEBUG) {
			analytics.setDryRun(true);
			analytics.setAppOptOut(false);
			analytics.getLogger().setLogLevel(LogLevel.VERBOSE);
			Log.i(TAG, "debug enabled");
		} else {
			analytics.setAppOptOut(false); // TODO: get option from shared preferences
			Log.i(TAG, "debug disabled");
		}
	}
	
	private static String PROPERTY_ID = "UA-51633871-1";

	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

	synchronized Tracker getTracker(TrackerName trackerId) {
		if (!mTrackers.containsKey(trackerId)) {

			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			switch (trackerId) {
			case APP_TRACKER:
				Tracker t = analytics.newTracker(PROPERTY_ID);
				mTrackers.put(trackerId, t);

				UncaughtExceptionHandler myHandler = new ExceptionReporter(t,
						Thread.getDefaultUncaughtExceptionHandler(), this);
				// Make myHandler the new default uncaught exception handler.
				Thread.setDefaultUncaughtExceptionHandler(myHandler);
				
				break;
			}
		}
		return mTrackers.get(trackerId);
	}

}
