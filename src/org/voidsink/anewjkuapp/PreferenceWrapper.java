package org.voidsink.anewjkuapp;

import java.io.File;
import java.util.List;

import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.PeriodicSync;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public final class PreferenceWrapper {
	public static final String TAG = PreferenceWrapper.class.getSimpleName();

	public static final String PREF_SYNC_INTERVAL_KEY = "pref_key_sync_interval";
	public static final int PREF_SYNC_INTERVAL_DEFAULT = 23;

	public static final String PREF_NOTIFY_CALENDAR_KEY = "pref_key_notify_calendar";
	public static final boolean PREF_NOTIFY_CALENDAR_DEFAULT = true;

	public static final String PREF_NOTIFY_EXAM_KEY = "pref_key_notify_exam";
	public static final boolean PREF_NOTIFY_EXAM_DEFAULT = true;

	public static final String PREF_NOTIFY_GRADE_KEY = "pref_key_notify_grade";
	public static final boolean PREF_NOTIFY_GRADE_DEFAULT = true;

	public static final String PREF_USE_LIGHT_THEME = "pref_key_use_light_theme";
	public static final boolean PREF_USE_LIGHT_THEME_DEFAULT = false;

	public static final String PREF_MAP_FILE = "pref_key_map_file";
	public static final String PREF_MAP_FILE_DEFAULT = "";

	private static final String PREF_LAST_FRAGMENT = "pref_key_last_fragment";
	private static final String PREF_LAST_FRAGMENT_DEFAULT = "";

	private static final String PREF_GET_NEW_EXAMS = "pref_key_get_exams_from_lva";
	private static final boolean PREF_GET_NEW_EXAMS_DEFAULT = false;
	
	private static final String PREF_LAST_VERSION = "pref_key_last_version";
	public static final int PREF_LAST_VERSION_NONE = -1;
	
	private PreferenceWrapper() {

	}

	public static int getSyncInterval(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		try {
			return Integer.parseInt(sp.getString(PREF_SYNC_INTERVAL_KEY,
					Integer.toString(PREF_SYNC_INTERVAL_DEFAULT)));
		} catch (Exception e) {
			Log.e(TAG, "Failure", e);
			return PREF_SYNC_INTERVAL_DEFAULT;
		}
	}

	public static void applySyncInterval(Context mContext) {
		int interval = getSyncInterval(mContext);

		Account mAccount = MainActivity.getAccount(mContext);

		if (mAccount != null) {
			List<PeriodicSync> syncs = ContentResolver.getPeriodicSyncs(
					mAccount, CalendarContractWrapper.AUTHORITY());
			for (PeriodicSync sync : syncs) {
				Log.d(TAG, "old sync: " + sync.period);
			}

			// Inform the system that this account supports sync
			// ContentResolver.setIsSyncable(mAccount,
			// CalendarContractWrapper.AUTHORITY(), 1);

			// Remove old sync periode
			ContentResolver.removePeriodicSync(mAccount,
					CalendarContractWrapper.AUTHORITY(), new Bundle());
			ContentResolver.removePeriodicSync(mAccount,
					KusssContentContract.AUTHORITY, new Bundle());
			// Turn on periodic syncing
			ContentResolver.addPeriodicSync(mAccount,
					CalendarContractWrapper.AUTHORITY(), new Bundle(),
					60 * 60 * interval);
			ContentResolver.addPeriodicSync(mAccount,
					KusssContentContract.AUTHORITY, new Bundle(),
					60 * 60 * interval);
		}
	}

	public static boolean getNotifyCalendar(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		try {
			return sp.getBoolean(PREF_NOTIFY_CALENDAR_KEY,
					PREF_NOTIFY_CALENDAR_DEFAULT);
		} catch (Exception e) {
			Log.e(TAG, "Failure", e);
			return PREF_NOTIFY_CALENDAR_DEFAULT;
		}
	}

	public static boolean getNewExamsByLvaNr(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		try {
			return sp.getBoolean(PREF_GET_NEW_EXAMS,
					PREF_GET_NEW_EXAMS_DEFAULT);
		} catch (Exception e) {
			Log.e(TAG, "Failure", e);
			return PREF_GET_NEW_EXAMS_DEFAULT;
		}
	}	
	
	public static boolean getNotifyExam(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		try {
			return sp
					.getBoolean(PREF_NOTIFY_EXAM_KEY, PREF_NOTIFY_EXAM_DEFAULT);
		} catch (Exception e) {
			Log.e(TAG, "Failure", e);
			return PREF_NOTIFY_EXAM_DEFAULT;
		}
	}

	public static boolean getNotifyGrade(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		try {
			return sp.getBoolean(PREF_NOTIFY_GRADE_KEY,
					PREF_NOTIFY_GRADE_DEFAULT);
		} catch (Exception e) {
			Log.e(TAG, "Failure", e);
			return PREF_NOTIFY_GRADE_DEFAULT;
		}
	}

	public static boolean getUseLightDesign(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		try {
			return sp.getBoolean(PREF_USE_LIGHT_THEME,
					PREF_USE_LIGHT_THEME_DEFAULT);
		} catch (Exception e) {
			Log.e(TAG, "Failure", e);
			return PREF_USE_LIGHT_THEME_DEFAULT;
		}
	}

	public static File getMapFile(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		File mapFile = null;
		try {
			mapFile = new File(sp.getString(PREF_MAP_FILE, PREF_MAP_FILE_DEFAULT));
		} catch (Exception e) {
			Log.e(TAG, "Failure", e);
			mapFile = null;
		}
		if (mapFile != null && (!mapFile.exists() || !mapFile.canRead())) {
			mapFile = null;
		}
		return mapFile;
	}

	public static String getLastFragment(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		try {
			return sp.getString(PREF_LAST_FRAGMENT, PREF_LAST_FRAGMENT_DEFAULT);
		} catch (Exception e) {
			Log.e(TAG, "Failure", e);
			return PREF_LAST_FRAGMENT_DEFAULT;
		}
	}

	public static void setLastFragment(Context mContext, String clazzname) {
		if (clazzname != null) {
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			sp.edit().putString(PREF_LAST_FRAGMENT, clazzname).commit();
		}
	}

	public static int getLastVersion(Context mContext) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		try {
			return sp.getInt(PREF_LAST_VERSION, PREF_LAST_VERSION_NONE);
		} catch (Exception e) {
			Log.e(TAG, "Failure", e);
            return PREF_LAST_VERSION_NONE;
		}		
	}
	
	public static int getCurrentVersion(Context mContext) {
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
            		mContext.getPackageName(), 0);

            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Could not get version information from manifest!", e);
            return PREF_LAST_VERSION_NONE;
        }
		
	}

	public static void setLastVersion(Context mContext, int version) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		sp.edit().putInt(PREF_LAST_VERSION, version).commit();
	}
	
}
