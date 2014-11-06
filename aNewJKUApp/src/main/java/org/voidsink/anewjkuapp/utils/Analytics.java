package org.voidsink.anewjkuapp.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.voidsink.anewjkuapp.BuildConfig;
import org.voidsink.anewjkuapp.Globals;
import org.voidsink.anewjkuapp.Globals.TrackerName;

public class Analytics {

    private static final int GA_DIM_EXCEPTION = 1;
    private static final int GA_DIM_ADDITIONAL_DATA = 2;

    // private static final int GA_METRIC_SYNC_INTERVAL = 1;
    // private static final int GA_METRIC_LOAD_EXAM_BY_LVANR = 2;
    // private static final int GA_METRIC_USE_LIGHT_THEME = 3;
    // private static final int GA_METRIC_USE_BARCHART_FOR_LVA = 4;

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

    public static void sendLoadExamByLvaNr(Context context, boolean loadByLvaNr) {
        // GA_METRIC_LOAD_EXAM_BY_LVANR
    }

    public static void sendUseLightTheme(Context context, boolean useLightTheme) {
        // GA_METRIC_USE_LIGHT_THEME
    }

    public static void sendUseBarchartForLvas(Context context,
                                              boolean useBarchartForLvas) {
        // GA_METRIC_USE_BARCHART_FOR_LVA
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

    public static void eventReloadLvas(Context c) {
        Tracker t = getAppTracker(c);
        if (t != null) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(GA_EVENT_CATEGORY_UI)
                    .setAction("button_press").setLabel("reload_lvas").build());
        }
    }

    public static void eventReloadEvents(Context c) {
        Tracker t = getAppTracker(c);
        if (t != null) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(GA_EVENT_CATEGORY_UI)
                    .setAction("button_press").setLabel("reload_events")
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

    public static void eventReloadGrades(Context c) {
        Tracker t = getAppTracker(c);
        if (t != null) {
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(GA_EVENT_CATEGORY_UI)
                    .setAction("button_press").setLabel("reload_grades")
                    .build());
        }
    }

    // }}

    // {{ Exceptions

    public static void sendException(Context c, Exception e, boolean fatal) {
        sendException(c, e, fatal, "");
    }

    public static void sendException(Context c, Exception e, boolean fatal, String additionalData) {
        try {
            Tracker t = getAppTracker(c);
            if (t != null && e != null) {
                HitBuilders.ExceptionBuilder eb = new HitBuilders.ExceptionBuilder()
                        .setFatal(fatal)
                        .setCustomDimension(GA_DIM_EXCEPTION,
                                e.getClass().getSimpleName())
                        .setDescription(
                                new AnalyticsExceptionParser(c, null)
                                        .getDescription(Thread.currentThread()
                                                .getName(), e)
                        );
                if (additionalData.length() > 0) {
                    eb.setCustomDimension(GA_DIM_ADDITIONAL_DATA, additionalData);
                }

                t.send(eb.build());
            }
        } catch (Exception e2) {
            Log.e(TAG, "sendException", e2);
        }
    }

    // }}

}
