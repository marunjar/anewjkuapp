/*******************************************************************************
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 ******************************************************************************/

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

import org.voidsink.anewjkuapp.utils.AppUtils;

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

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

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

        AppUtils.updateSyncAlarm(this, false);
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
