/*
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
 *
 */

package org.voidsink.anewjkuapp.analytics;

import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import org.jsoup.HttpStatusException;
import org.voidsink.anewjkuapp.BuildConfig;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.HashMap;

public class AnalyticsFlavor implements IAnalytics {

    private static final String TAG = AnalyticsFlavor.class.getSimpleName();
    private Application mApp = null;

    public enum TrackerName {
        APP_TRACKER
    }

    private static final int GA_DIM_EXCEPTION_NAME = 1;
    private static final int GA_DIM_ADDITIONAL_DATA = 2;
    private static final int GA_DIM_EXCEPTION_MESSAGE = 3;

    // private static final int GA_METRIC_SYNC_INTERVAL = 1;
    // private static final int GA_METRIC_LOAD_EXAM_BY_COURSEID = 2;
    // private static final int GA_METRIC_USE_LIGHT_THEME = 3;
    // private static final int GA_METRIC_USE_BARCHART_FOR_COURSES = 4;

    private static final String GA_EVENT_CATEGORY_UI = "ui_action";

    // private static final String GA_EVENT_CATEGORY_SERVICE = "service_action";

    private HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    private synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(mApp);
            switch (trackerId) {
                case APP_TRACKER:
                    Tracker t = analytics.newTracker(Consts.PROPERTY_ID);
                    mTrackers.put(trackerId, t);

                    Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(t,
                            Thread.getDefaultUncaughtExceptionHandler(), mApp);
                    // Make myHandler the new default uncaught exception handler.
                    Thread.setDefaultUncaughtExceptionHandler(myHandler);

                    // disable auto activity tracking
                    t.enableAutoActivityTracking(false);

                    // try to initialize screen size
                    try {
                        WindowManager wm = (WindowManager) mApp.getSystemService(Context.WINDOW_SERVICE);
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

    private Tracker getAppTracker() {
        return getTracker(TrackerName.APP_TRACKER);
    }

    @Override
    public void init(Application app) {
        if (mApp == null) {
            mApp = app;

            final GoogleAnalytics analytics = GoogleAnalytics.getInstance(mApp);
            if (BuildConfig.DEBUG) {
                analytics.setDryRun(true);
                analytics.setAppOptOut(!PreferenceWrapper.trackingErrors(mApp));
                analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
                Log.i(TAG, "debug enabled");
            } else {
                analytics.enableAutoActivityReports(mApp);
                analytics.setAppOptOut(!PreferenceWrapper.trackingErrors(mApp));
                Log.i(TAG, "debug disabled");
            }
        } else {
            throw new UnknownError("Analytics already initialized");
        }
    }

    @Override
    public void sendException(Context c, Exception e, boolean fatal, String additionalData) {
        try {
            Tracker t = getAppTracker();
            if (t != null && e != null) {
                HitBuilders.ExceptionBuilder eb = new HitBuilders.ExceptionBuilder()
                        .setFatal(fatal)
                        .setCustomDimension(GA_DIM_EXCEPTION_NAME,
                                e.getClass().getCanonicalName())
                        .setDescription(
                                new StandardExceptionParser(c, null)
                                        .getDescription(Thread.currentThread()
                                                .getName(), e)
                        );

                if (TextUtils.isEmpty(additionalData) && (e instanceof HttpStatusException)) {
                    additionalData = String.format("%d: %s", ((HttpStatusException) e).getStatusCode(), ((HttpStatusException) e).getUrl());
                }

                if (!TextUtils.isEmpty(additionalData)) {
                    eb.setCustomDimension(GA_DIM_ADDITIONAL_DATA, additionalData);
                }
                eb.setCustomDimension(GA_DIM_EXCEPTION_MESSAGE, e.getMessage());

                t.send(eb.build());
            }
        } catch (Exception e2) {
            Log.e(TAG, "sendException", e2);
        }
        if (BuildConfig.DEBUG) {
            if (e != null) {
                Log.d(TAG, String.format("%s (%s)", e.getMessage(), additionalData));
            }
        }
    }

    @Override
    public void sendScreen(Context c, String screenName) {
        Tracker t = getAppTracker();
        if (t != null) {
            t.setScreenName(screenName);
            t.send(new HitBuilders.ScreenViewBuilder().build());
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("screen: %s", screenName));
        }
    }

    @Override
    public void sendButtonEvent(String label) {
        Tracker t = getAppTracker();
        if (t != null && TextUtils.isEmpty(label)) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(GA_EVENT_CATEGORY_UI)
                    .setAction("button_press")
                    .setLabel(label)
                    .build());
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("buttonEvent: %s", label));
        }
    }

    @Override
    public void sendPreferenceChanged(String key, String value) {
        Tracker t = getAppTracker();
        if (t != null && TextUtils.isEmpty(key) && TextUtils.isEmpty(value)) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(GA_EVENT_CATEGORY_UI)
                    .setAction("preference_changed")
                    .setCategory(key)
                    .setLabel(value)
                    .build());
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("preferenceChanged: %s=%s", key, value));
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        final GoogleAnalytics analytics = GoogleAnalytics.getInstance(mApp);
        analytics.setAppOptOut(!enabled);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("setEnabled: %s", enabled));
        }
    }
}
