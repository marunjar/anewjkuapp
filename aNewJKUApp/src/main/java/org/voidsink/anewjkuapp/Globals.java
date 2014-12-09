package org.voidsink.anewjkuapp;

import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger.LogLevel;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class Globals extends Application {

    public enum TrackerName {
        APP_TRACKER
    }

    private static final String TAG = Globals.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault("fonts/Roboto-Regular.ttf", R.attr.fontPath);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .build();
        ImageLoader.getInstance().init(config);

        // initialize analytics
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        if (BuildConfig.DEBUG) {
            //analytics.setDryRun(true);
            analytics.setAppOptOut(false);
            analytics.getLogger().setLogLevel(LogLevel.VERBOSE);
            Log.i(TAG, "debug enabled");
        } else {
            analytics.enableAutoActivityReports(this);
            analytics.setAppOptOut(false); // TODO: get option from shared preferences
            Log.i(TAG, "debug disabled");
        }
    }

    private static String PROPERTY_ID = "UA-51633871-1";

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
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

                    // disable auto activity tracking
                    t.enableAutoActivityTracking(false);

                    // try to initialize screen size
                    try {
                        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                        Display display = wm.getDefaultDisplay();

                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            Point size = new Point();
                            display.getSize(size);

                            t.setScreenResolution(size.x, size.y);
                        } else {
                            t.setScreenResolution(display.getWidth(), display.getHeight());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "get sceen size", e);
                    }

                    break;
            }
        }
        return mTrackers.get(trackerId);
    }

}
