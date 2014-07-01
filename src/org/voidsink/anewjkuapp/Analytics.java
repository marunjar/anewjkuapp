package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.GlobalStats.TrackerName;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import android.content.Context;

public class Analytics {

	private static final int GA_DIM_EXCEPTION = 1;

	// private static final int GA_METRIC_SYNC_INTERVAL = 1;
	// private static final int GA_METRIC_LOAD_EXAM_BY_LVANR = 2;
	// private static final int GA_METRIC_USE_LIGHT_THEME = 3;
	// private static final int GA_METRIC_USE_BARCHART_FOR_LVA = 4;

	private static final String GA_EVENT_CATEGORY_UI = "ui_action";

	// private static final String GA_EVENT_CATEGORY_SERVICE = "service_action";

	private static GlobalStats getGlobalStats(Context c) {
		if (c instanceof GlobalStats) {
			return (GlobalStats) c;
		} else if (c.getApplicationContext() instanceof GlobalStats) {
			return (GlobalStats) c.getApplicationContext();
		}
		return null;
	}

	private static Tracker getAppTracker(Context c) {
		GlobalStats gs = getGlobalStats(c);
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

	// {{ Views

	public static void viewPreferences(Context c) {
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
		Tracker t = getAppTracker(c);
		if (t != null && e != null) {
			System.out.println("1: " + e.getClass().getSimpleName());
			System.out.println("2: "
					+ new StandardExceptionParser(c, null).getDescription(
							Thread.currentThread().getName(), e));

			t.send(new HitBuilders.ExceptionBuilder()
					.setFatal(fatal)
					.setCustomDimension(GA_DIM_EXCEPTION,
							e.getClass().getSimpleName())
					.setDescription(
							new StandardExceptionParser(c, null)
									.getDescription(Thread.currentThread()
											.getName(), e)

					).build());
		}
	}

	// }}

}
