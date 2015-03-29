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

package org.voidsink.anewjkuapp.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.jsoup.HttpStatusException;
import org.voidsink.anewjkuapp.BuildConfig;
import org.voidsink.anewjkuapp.Globals;
import org.voidsink.anewjkuapp.Globals.TrackerName;

public class Analytics {

    private static final int GA_DIM_EXCEPTION_NAME = 1;
    private static final int GA_DIM_ADDITIONAL_DATA = 2;
    private static final int GA_DIM_EXCEPTION_MESSAGE = 3;

    // private static final int GA_METRIC_SYNC_INTERVAL = 1;
    // private static final int GA_METRIC_LOAD_EXAM_BY_COURSEID = 2;
    // private static final int GA_METRIC_USE_LIGHT_THEME = 3;
    // private static final int GA_METRIC_USE_BARCHART_FOR_COURSES = 4;

    private static final String GA_EVENT_CATEGORY_UI = "ui_action";

    private static final String TAG = Analytics.class.getSimpleName();

    // private static final String GA_EVENT_CATEGORY_SERVICE = "service_action";

    private static Globals getGlobalStats(Context c) {
        if (c == null)
            return null;

        if (c instanceof Globals) {
            return (Globals) c;
        } else if (c.getApplicationContext() instanceof Globals) {
            return (Globals) c.getApplicationContext();
        }

        return null;
    }

    private static Tracker getAppTracker(Context c) {
        Globals gs = getGlobalStats(c);
        if (gs != null) {
            return gs.getTracker(TrackerName.APP_TRACKER);
        }
        return null;
    }

    // {{ Dimensions

    // }}

    // {{ Metrics

    public static void sendSyncInterval(Context context, long count) {
        // GA_METRIC_SYNC_INTERVAL
    }

    public static void sendLoadExamByCourseId(Context context, boolean loadByCourseId) {
        // GA_METRIC_LOAD_EXAM_BY_COURSEID
    }

    public static void sendUseLightTheme(Context context, boolean useLightTheme) {
        // GA_METRIC_USE_LIGHT_THEME
    }

    public static void sendUseBarchartForCourses(Context context,
                                                 boolean useBarchartForCourses) {
        // GA_METRIC_USE_BARCHART_FOR_COURSES
    }

    // }}

    // {{ Screens

    public static void sendScreen(Context c, String screenName) {
        Tracker t = getAppTracker(c);
        if (t != null) {
            // output some debug info
            if (BuildConfig.DEBUG) {
                Log.d(TAG, String.format("sendScreen: %s", screenName));
            }

            t.setScreenName(screenName);
            t.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    public static void clearScreen(Context c) {
        sendScreen(c, null);
    }

    // }}

    // {{ Events

    public static void eventReloadCourses(Context c) {
        Tracker t = getAppTracker(c);
        if (t != null) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(GA_EVENT_CATEGORY_UI)
                    .setAction("button_press").setLabel("reload_courses").build());
        }
    }

    public static void eventReloadEventsCourse(Context c) {
        Tracker t = getAppTracker(c);
        if (t != null) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(GA_EVENT_CATEGORY_UI)
                    .setAction("button_press").setLabel("reload_events_exam")
                    .build());
        }
    }

    public static void eventReloadEventsExam(Context c) {
        Tracker t = getAppTracker(c);
        if (t != null) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(GA_EVENT_CATEGORY_UI)
                    .setAction("button_press").setLabel("reload_events_course")
                    .build());
        }
    }

    public static void eventLoadMoreEvents(Context c, long value) {
        Tracker t = getAppTracker(c);
        if (t != null) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(GA_EVENT_CATEGORY_UI)
                    .setAction("button_press").setLabel("more_events")
                    .setValue(value).build());
        }
    }

    public static void eventReloadExams(Context c) {
        Tracker t = getAppTracker(c);
        if (t != null) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(GA_EVENT_CATEGORY_UI)
                    .setAction("button_press").setLabel("reload_exams").build());
        }
    }

    public static void eventReloadAssessments(Context c) {
        Tracker t = getAppTracker(c);
        if (t != null) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(GA_EVENT_CATEGORY_UI)
                    .setAction("button_press").setLabel("reload_assessments")
                    .build());
        }
    }

    public static void eventReloadCurricula(Context c) {
        Tracker t = getAppTracker(c);
        if (t != null) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(GA_EVENT_CATEGORY_UI)
                    .setAction("button_press").setLabel("reload_curricula")
                    .build());
        }
    }

    // }}

    // {{ Exceptions

    public static void sendException(Context c, Exception e, boolean fatal) {
        sendException(c, e, fatal, null);
        Log.e(TAG, "sendException", e);
    }

    public static void sendException(Context c, Exception e, boolean fatal, String additionalData) {
        try {
            Tracker t = getAppTracker(c);
            if (t != null && e != null) {
                HitBuilders.ExceptionBuilder eb = new HitBuilders.ExceptionBuilder()
                        .setFatal(fatal)
                        .setCustomDimension(GA_DIM_EXCEPTION_NAME,
                                e.getClass().getCanonicalName())
                        .setDescription(
                                new AnalyticsExceptionParser(c, null)
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
    }

    // }}

}
