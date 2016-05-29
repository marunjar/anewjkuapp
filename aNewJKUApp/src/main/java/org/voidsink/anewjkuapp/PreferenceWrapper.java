/*
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2014-2016 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.PeriodicSync;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.io.File;
import java.util.List;

public final class PreferenceWrapper {
    private static final String TAG = PreferenceWrapper.class.getSimpleName();

    public static final String PREF_SYNC_INTERVAL_KEY = "pref_key_sync_interval";
    public static final int PREF_SYNC_INTERVAL_DEFAULT = 85;

    public static final String PREF_NOTIFY_CALENDAR_KEY = "pref_key_notify_calendar";
    public static final boolean PREF_NOTIFY_CALENDAR_DEFAULT = true;

    public static final String PREF_USE_CALENDAR_VIEW_KEY = "pref_key_use_calendar_view";
    public static final boolean PREF_USE_CALENDAR_VIEW_DEFAULT = false;

    public static final String PREF_NOTIFY_EXAM_KEY = "pref_key_notify_exam";
    public static final boolean PREF_NOTIFY_EXAM_DEFAULT = true;

    public static final String PREF_NOTIFY_GRADE_KEY = "pref_key_notify_grade";
    public static final boolean PREF_NOTIFY_GRADE_DEFAULT = true;

    public static final String PREF_USE_LIGHT_THEME = "pref_key_use_light_theme";
    public static final boolean PREF_USE_LIGHT_THEME_DEFAULT = true;

    public static final String PREF_MAP_FILE = "pref_key_map_file";
    public static final String PREF_MAP_FILE_DEFAULT = "";

    public static final int PREF_LAST_FRAGMENT_DEFAULT = 0;
    public static final String PREF_GET_NEW_EXAMS = "pref_key_get_exams_from_lva";
    public static final int PREF_LAST_VERSION_NONE = -1;
    public static final String PREF_USE_LVA_BAR_CHART = "pref_key_use_lva_bar_chart";
    public static final String PREF_MENSA_GROUP_MENU_BY_DAY = "pref_key_group_menu_by_day";
    public static final String PREF_POSITIVE_GRADES_ONLY = "pref_key_positive_grades_only";
    private static final String PREF_LAST_FRAGMENT = "pref_key_last_fragment";
    private static final boolean PREF_GET_NEW_EXAMS_DEFAULT = false;
    private static final String PREF_LAST_VERSION = "pref_key_last_version";
    private static final boolean PREF_USE_LVA_BAR_CHART_DEFAULT = false;
    private static final boolean PREF_MENSA_GROUP_MENU_BY_DAY_DEFAULT = false;
    private static final boolean PREF_POSITIVE_GRADES_ONLY_DEFAULT = false;

    public static final String PREF_EXTEND_CALENDAR_LVA = "pref_key_extend_calendar_lva";
    private static final boolean PREF_EXTEND_CALENDAR_LVA_DEFAULT = false;
    public static final String PREF_EXTENDED_CALENDAR_LVA = "pref_key_extended_calendar_lva";
    private static final String PREF_EXTENDED_CALENDAR_LVA_DEFAULT = null;

    public static final String PREF_EXTEND_CALENDAR_EXAM = "pref_key_extend_calendar_exam";
    private static final boolean PREF_EXTEND_CALENDAR_EXAM_DEFAULT = false;
    public static final String PREF_EXTENDED_CALENDAR_EXAM = "pref_key_extended_calendar_exam";
    private static final String PREF_EXTENDED_CALENDAR_EXAM_DEFAULT = null;

    private static final String PREF_SYNC_CALENDAR_LVA = "pref_key_sync_calendar_lva";
    private static final boolean PREF_SYNC_CALENDAR_LVA_DEFAULT = true;
    private static final String PREF_SYNC_CALENDAR_EXAM = "pref_key_sync_calendar_exam";
    private static final boolean PREF_SYNC_CALENDAR_EXAM_DEFAULT = true;

    public static final String PREF_TRACKING_ERRORS = "pref_key_tracking_errors";
    public static final boolean PREF_TRACKING_ERRORS_DEFAULT = true;

    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    public static final String PREF_COURSES_SHOW_WITH_ASSESSMENT_ONLY = "pref_key_courses_show_with_assessment_only";
    private static final boolean PREF_COURSES_SHOW_WITH_ASSESSMENT_ONLY_DEFAULT = false;

    private PreferenceWrapper() {

    }

    public static int getSyncInterval(Context mContext) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        try {
            return Math.max(Integer.parseInt(sp.getString(PREF_SYNC_INTERVAL_KEY,
                    Integer.toString(PREF_SYNC_INTERVAL_DEFAULT))), 12); // min. 12h interval
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return PREF_SYNC_INTERVAL_DEFAULT;
        }
    }

    public static void applySyncInterval(Context mContext) {
        Account mAccount = AppUtils.getAccount(mContext);

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
            int interval = getSyncInterval(mContext);

            ContentResolver.addPeriodicSync(mAccount,
                    CalendarContractWrapper.AUTHORITY(), new Bundle(),
                    60 * 60 * interval);
            ContentResolver.addPeriodicSync(mAccount,
                    KusssContentContract.AUTHORITY, new Bundle(),
                    60 * 60 * interval);
        }
        AppUtils.updateSyncAlarm(mContext, true);
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

    public static boolean getUseCalendarView(Context mContext) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        try {
            return sp.getBoolean(PREF_USE_CALENDAR_VIEW_KEY,
                    PREF_USE_CALENDAR_VIEW_DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return PREF_USE_CALENDAR_VIEW_DEFAULT;
        }
    }

    public static boolean getNewExamsByCourseId(Context mContext) {
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
            String file = sp.getString(PREF_MAP_FILE, PREF_MAP_FILE_DEFAULT);
            if (file != null) {
                mapFile = new File(file);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            mapFile = null;
        }
        if (mapFile != null && (!mapFile.exists() || !mapFile.canRead())) {
            mapFile = null;
        }
        return mapFile;
    }

    public static int getLastFragment(Context mContext) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        try {
            return sp.getInt(PREF_LAST_FRAGMENT, PREF_LAST_FRAGMENT_DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return PREF_LAST_FRAGMENT_DEFAULT;
        }
    }

    public static void setLastFragment(Context mContext, int id) {
        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            sp.edit().putInt(PREF_LAST_FRAGMENT, id).commit();
        } catch (Exception e) {
            Analytics.sendException(mContext, e, false);
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
        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            sp.edit().putInt(PREF_LAST_VERSION, version).commit();
        } catch (Exception e) {
            Analytics.sendException(mContext, e, false);
        }
    }

    public static boolean getUseLvaBarChart(Context mContext) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        try {
            return sp.getBoolean(PREF_USE_LVA_BAR_CHART,
                    PREF_USE_LVA_BAR_CHART_DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return PREF_USE_LVA_BAR_CHART_DEFAULT;
        }
    }

    public static boolean getPositiveGradesOnly(Context mContext) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        try {
            return sp.getBoolean(PREF_POSITIVE_GRADES_ONLY,
                    PREF_POSITIVE_GRADES_ONLY_DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return PREF_POSITIVE_GRADES_ONLY_DEFAULT;
        }
    }

    public static void setPrefPositiveGradesOnly(Context mContext, boolean positiveOnly) {
        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            sp.edit().putBoolean(PREF_POSITIVE_GRADES_ONLY, positiveOnly).apply();
        } catch (Exception e) {
            Analytics.sendException(mContext, e, false);
        }
    }

    public static boolean getGroupMenuByDay(Context mContext) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        try {
            return sp.getBoolean(PREF_MENSA_GROUP_MENU_BY_DAY,
                    PREF_MENSA_GROUP_MENU_BY_DAY_DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return PREF_MENSA_GROUP_MENU_BY_DAY_DEFAULT;
        }
    }

    public static boolean useDefaultExamCalendarId(Context context) {
        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(context);

            return !sp.getBoolean(PREF_EXTEND_CALENDAR_EXAM,
                    PREF_EXTEND_CALENDAR_EXAM_DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return !PREF_EXTEND_CALENDAR_EXAM_DEFAULT;
        }
    }

    public static String getExamCalendarId(Context context) {
        if (useDefaultExamCalendarId(context))
            return null;

        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(context);

            return sp.getString(PREF_EXTENDED_CALENDAR_EXAM, PREF_EXTENDED_CALENDAR_EXAM_DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return PREF_EXTENDED_CALENDAR_EXAM_DEFAULT;
        }
    }

    public static boolean useDefaultLvaCalendarId(Context context) {
        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(context);

            return !sp.getBoolean(PREF_EXTEND_CALENDAR_LVA,
                    PREF_EXTEND_CALENDAR_LVA_DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return !PREF_EXTEND_CALENDAR_LVA_DEFAULT;
        }
    }

    public static String getLvaCalendarId(Context context) {
        if (useDefaultLvaCalendarId(context))
            return null;

        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(context);

            return sp.getString(PREF_EXTENDED_CALENDAR_LVA, PREF_EXTENDED_CALENDAR_LVA_DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return PREF_EXTENDED_CALENDAR_LVA_DEFAULT;
        }
    }

    public static boolean getSyncCalendarLva(Context context) {
        if (useDefaultLvaCalendarId(context))
            return PREF_SYNC_CALENDAR_LVA_DEFAULT;

        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(context);

            return sp.getBoolean(PREF_SYNC_CALENDAR_LVA, PREF_SYNC_CALENDAR_LVA_DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return PREF_SYNC_CALENDAR_LVA_DEFAULT;
        }
    }

    public static boolean getSyncCalendarExam(Context context) {
        if (useDefaultExamCalendarId(context))
            return PREF_SYNC_CALENDAR_EXAM_DEFAULT;

        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(context);

            return sp.getBoolean(PREF_SYNC_CALENDAR_EXAM, PREF_SYNC_CALENDAR_EXAM_DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return PREF_SYNC_CALENDAR_EXAM_DEFAULT;
        }
    }

    public static boolean trackingErrors(Context context) {
        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(context);

            return sp.getBoolean(PREF_TRACKING_ERRORS,
                    PREF_TRACKING_ERRORS_DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return PREF_TRACKING_ERRORS_DEFAULT;
        }
    }

    public static boolean getUserLearnedDrawer(Context context) {
        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(context);

            return sp.getBoolean(PREF_USER_LEARNED_DRAWER,
                    false);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return false;
        }
    }

    public static void setPrefUserLearnedDrawer(Context c, boolean b) {
        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(c);
            sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, b).apply();
        } catch (Exception e) {
            Analytics.sendException(c, e, false);
        }
    }

    public static boolean getShowCoursesWithAssessmentOnly(Context mContext) {
        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            return sp.getBoolean(PREF_COURSES_SHOW_WITH_ASSESSMENT_ONLY,
                    PREF_COURSES_SHOW_WITH_ASSESSMENT_ONLY_DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Failure", e);
            return PREF_COURSES_SHOW_WITH_ASSESSMENT_ONLY_DEFAULT;
        }
    }

    public static void setShowCoursesWithAssessmentOnly(Context mContext, boolean value) {
        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            sp.edit().putBoolean(PREF_COURSES_SHOW_WITH_ASSESSMENT_ONLY, value).apply();
        } catch (Exception e) {
            Analytics.sendException(mContext, e, false);
        }
    }


}
