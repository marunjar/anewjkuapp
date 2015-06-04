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
 */

package org.voidsink.anewjkuapp.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import org.apache.commons.lang.time.DateUtils;
import org.voidsink.anewjkuapp.ImportPoiTask;
import org.voidsink.anewjkuapp.KusssAuthenticator;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.fragment.MapFragment;
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.AssessmentType;
import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.kusss.Curriculum;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.LvaState;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.service.SyncAlarmService;
import org.voidsink.anewjkuapp.update.ImportCurriculaTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.Collections;

public class AppUtils {

    private static final String DEFAULT_POI_FILE_NAME = "JKU.gpx";
    private static final String TAG = AppUtils.class.getSimpleName();

    private static final Comparator<Course> CourseComparator = new Comparator<Course>() {
        @Override
        public int compare(Course lhs, Course rhs) {
            int value = lhs.getTitle().compareTo(rhs.getTitle());
            if (value == 0) {
                value = lhs.getTerm().compareTo(rhs.getTerm());
            }
            return value;
        }
    };

    private static final Comparator<LvaWithGrade> LvaWithGradeComparator = new Comparator<LvaWithGrade>() {
        @Override
        public int compare(LvaWithGrade lhs, LvaWithGrade rhs) {
            int value = lhs.getState().compareTo(rhs.getState());
            if (value == 0) {
                value = lhs.getCourse().getTitle()
                        .compareTo(rhs.getCourse().getTitle());
            }
            if (value == 0) {
                value = TermComparator.compare(lhs.getCourse().getTerm(), rhs.getCourse().getTerm());
            }
            return value;
        }
    };

    private static final Comparator<Curriculum> CurriculaComparator = new Comparator<Curriculum>() {

        @Override
        public int compare(Curriculum lhs, Curriculum rhs) {
            int value = lhs.getUni().compareToIgnoreCase(rhs.getUni());
            if (value == 0) {
                value = lhs.getDtStart().compareTo(rhs.getDtStart());
            }
            if (value == 0) {
                value = lhs.getCid().compareTo(rhs.getCid());
            }
            return value;
        }
    };

    private static final Comparator<Assessment> AssessmentComparator = new Comparator<Assessment>() {
        @Override
        public int compare(Assessment lhs, Assessment rhs) {
            int value = lhs.getAssessmentType().compareTo(rhs.getAssessmentType());
            if (value == 0) {
                value = rhs.getDate().compareTo(lhs.getDate());
            }
            if (value == 0) {
                value = TermComparator.compare(rhs.getTerm(), lhs.getTerm());
            }
            if (value == 0) {
                value = lhs.getTitle().compareTo(rhs.getTitle());
            }
            return value;
        }
    };

    private static final Comparator<Term> TermComparator = new Comparator<Term>() {
        @Override
        public int compare(Term lhs, Term rhs) {
            if (lhs == null && rhs == null) return 0;
            if (lhs == null) return -1;
            if (rhs == null) return 1;

            return rhs.compareTo(lhs);
        }
    };


    public static void doOnNewVersion(Context context) {
        int mLastVersion = PreferenceWrapper.getLastVersion(context);
        int mCurrentVersion = PreferenceWrapper.getCurrentVersion(context);

        if (mLastVersion != mCurrentVersion
                || mLastVersion == PreferenceWrapper.PREF_LAST_VERSION_NONE) {
            boolean errorOccured = false;

            try {
                if (!initPreferences(context)) {
                    errorOccured = true;
                }
                if (!importDefaultPois(context)) {
                    errorOccured = true;
                }
                if (!copyDefaultMap(context)) {
                    errorOccured = true;
                }
                if (shouldRemoveOldAccount(mLastVersion, mCurrentVersion)) {
                    if (!removeAccount(context)) {
                        errorOccured = true;
                    }
                }
                if (shouldImportCurricula(mLastVersion, mCurrentVersion)) {
                    if (!importCurricula(context)) {
                        errorOccured = true;
                    }
                }
                if (shouldRecreateCalendars(mLastVersion, mCurrentVersion)) {
                    if (!removeCalendars(context)) {
                        errorOccured = true;
                    }
                    if (!createCalendars(context)) {
                        errorOccured = true;
                    }
                }

                PreferenceWrapper.applySyncInterval(context);
            } catch (Exception e) {
                Log.e(TAG, "doOnNewVersion failed", e);
                Analytics.sendException(context, e, false);
                errorOccured = true;
            }
            if (!errorOccured) {
                PreferenceWrapper.setLastVersion(context, mCurrentVersion);
            }
        }
    }

    private static boolean createCalendars(Context context) {
        Account account = AppUtils.getAccount(context);
        if (account == null) {
            return true;
        }

        return CalendarUtils.createCalendarsIfNecessary(context, account);
    }

    private static boolean removeCalendars(Context context) {
        return CalendarUtils.removeCalendar(context, CalendarUtils.ARG_CALENDAR_EXAM) &&
                CalendarUtils.removeCalendar(context, CalendarUtils.ARG_CALENDAR_COURSE);
    }

    private static boolean shouldRecreateCalendars(int lastVersion, int currentVersion) {
        // calendars changed with 140029
        // remove old calendars on startup to avoid strange behaviour
        return (lastVersion <= 140028 && currentVersion > 140028);
    }

    private static boolean shouldImportCurricula(int lastVersion, int currentVersion) {
        // curricula added with 140026
        // import on startup to avoid strange behaviour and missing tabs
        return (lastVersion < 100026 && currentVersion >= 100026) ||
                (lastVersion < 140026 && currentVersion >= 140026);
    }

    private static boolean importCurricula(Context context) {
        Account account = getAccount(context);
        if (account != null) {
            try {
                new ImportCurriculaTask(account, context).execute();
            } catch (Exception e) {
                Analytics.sendException(context, e, false);
            }
        }
        return true;
    }

    private static boolean removeAccount(Context context) {
        Account account = getAccount(context);
        if (account != null) {
            AccountManager.get(context).removeAccount(account, null, null);
            Log.d(TAG, "account removed");
        }
        return true;
    }

    private static boolean shouldRemoveOldAccount(int lastVersion,
                                                  int currentVersion) {
        // calendar names changed with 100017, remove account for avoiding
        // corrupted data
        return (lastVersion < 100017 && currentVersion >= 100017) ||
                (lastVersion < 140017 && currentVersion >= 140017);
    }

    private static boolean initPreferences(Context context) {
        try {
            PreferenceManager.setDefaultValues(context, R.xml.preference_app,
                    true);
            PreferenceManager.setDefaultValues(context,
                    R.xml.preference_dashclock_extension_mensa, true);
            PreferenceManager.setDefaultValues(context, R.xml.preference_kusss,
                    true);
        } catch (Exception e) {
            Log.e(TAG, "initPreferences", e);
            return false;
        }
        return true;
    }

    private static boolean copyDefaultMap(Context context) {
        try {
            // write file to sd for mapsforge
            OutputStream mapFileWriter = new BufferedOutputStream(
                    context.openFileOutput(MapFragment.MAP_FILE_NAME,
                            Context.MODE_PRIVATE));
            InputStream assetData = new BufferedInputStream(context.getAssets()
                    .open(MapFragment.MAP_FILE_NAME));

            byte[] buffer = new byte[1024];
            int len = assetData.read(buffer);
            while (len != -1) {
                mapFileWriter.write(buffer, 0, len);
                len = assetData.read(buffer);
            }
            mapFileWriter.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "copyDefaultMap", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "copyDefaultMap", e);
            return false;
        }
        return true;
    }

    private static boolean importDefaultPois(Context context) {
        // import JKU Pois
        try {
            // write file to sd for import
            OutputStream mapFileWriter = new BufferedOutputStream(
                    context.openFileOutput(DEFAULT_POI_FILE_NAME,
                            Context.MODE_PRIVATE));
            InputStream assetData = new BufferedInputStream(context.getAssets()
                    .open(DEFAULT_POI_FILE_NAME));

            byte[] buffer = new byte[1024];
            int len = assetData.read(buffer);
            while (len != -1) {
                mapFileWriter.write(buffer, 0, len);
                len = assetData.read(buffer);
            }
            mapFileWriter.close();

            // import file
            new ImportPoiTask(context, new File(context.getFilesDir(),
                    DEFAULT_POI_FILE_NAME), true).execute();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "importDefaultPois", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "importDefaultPois", e);
            return false;
        }
        return true;
    }

    public static double getECTS(LvaState state, List<LvaWithGrade> lvas) {
        double sum = 0;
        for (LvaWithGrade lva : lvas) {
            if (state == LvaState.ALL || state == lva.getState()) {
                sum += lva.getCourse().getEcts();
            }
        }
        return sum;
    }

    public static void sortCourses(List<Course> courses) {
        Collections.sort(courses, CourseComparator);
    }

    public static void sortLVAsWithGrade(List<LvaWithGrade> mCourses) {
        Collections.sort(mCourses, LvaWithGradeComparator);

    }

    public static void sortAssessments(List<Assessment> assessments) {
        Collections.sort(assessments, AssessmentComparator);
    }

    public static void removeDuplicates(List<LvaWithGrade> mCourses) {
        // remove done duplicates
        int i = 0;
        while (i < mCourses.size()) {
            if (mCourses.get(i).getState() == LvaState.DONE) {
                Course course = mCourses.get(i).getCourse();
                int j = i + 1;

                while (j < mCourses.size()) {
                    Course nextCourse = mCourses.get(j).getCourse();
                    if (course.getCode().equals(nextCourse.getCode())
                            && course.getTitle().equals(nextCourse.getTitle())) {
                        mCourses.remove(j);
                        Log.d("removeDuplicates",
                                "remove from done " + nextCourse.getCode() + " "
                                        + nextCourse.getTitle());
                    } else {
                        j++;
                    }
                }
            }
            i++;
        }

        // remove all other duplicates
        i = 0;
        while (i < mCourses.size()) {
            if (mCourses.get(i).getState() != LvaState.DONE) {
                Course course = mCourses.get(i).getCourse();
                int j = i + 1;

                while (j < mCourses.size()) {
                    Course nextCourse = mCourses.get(j).getCourse();
                    if (course.getCode().equals(nextCourse.getCode())
                            && course.getTitle().equals(nextCourse.getTitle())) {
                        mCourses.remove(j);
                        Log.d("removeDuplicates",
                                "remove from other " + nextCourse.getCode() + " "
                                        + nextCourse.getTitle());
                    } else {
                        j++;
                    }
                }
            }
            i++;
        }

    }

    public static void removeDuplicates(List<LvaWithGrade> mDoneLvas,
                                        List<LvaWithGrade> mOpenLvas) {

        // Log.i("removeDuplicates", "---------");
        // for (LvaWithGrade lva : mDoneLvas) {
        // Log.i("removeDuplicates", "done: " + lva.getLva().getCode() + " "
        // + lva.getLva().getTitle());
        // }
        // for (LvaWithGrade lva : mOpenLvas) {
        // Log.i("removeDuplicates", "open: " + lva.getLva().getCode() + " "
        // + lva.getLva().getTitle());
        // }

        int i = 0;
        while (i < mDoneLvas.size()) {
            Course course = mDoneLvas.get(i).getCourse();
            int j = i + 1;

            while (j < mDoneLvas.size()) {
                Course nextCourse = mDoneLvas.get(j).getCourse();
                if (course.getCode().equals(nextCourse.getCode())
                        && course.getTitle().equals(nextCourse.getTitle())) {
                    mDoneLvas.remove(j);
                    Log.d("removeDuplicates",
                            "remove from done " + nextCourse.getCode() + " "
                                    + nextCourse.getTitle());
                } else {
                    j++;
                }
            }

            j = 0;
            while (j < mOpenLvas.size()) {
                Course nextCourse = mOpenLvas.get(j).getCourse();
                if (course.getCode().equals(nextCourse.getCode())
                        && course.getTitle().equals(nextCourse.getTitle())) {
                    mOpenLvas.remove(j);
                    Log.d("removeDuplicates",
                            "remove from open " + nextCourse.getCode() + " "
                                    + nextCourse.getTitle());
                } else {
                    j++;
                }
            }

            i++;
        }

        i = 0;
        while (i < mOpenLvas.size()) {
            Course course = mOpenLvas.get(i).getCourse();
            int j = i + 1;

            while (j < mOpenLvas.size()) {
                Course nextCourse = mOpenLvas.get(j).getCourse();
                if (course.getCode().equals(nextCourse.getCode())
                        && course.getTitle().equals(nextCourse.getTitle())) {
                    mOpenLvas.remove(j);
                    Log.d("removeDuplicates",
                            "remove from open " + nextCourse.getCode() + " "
                                    + nextCourse.getTitle());
                } else {
                    j++;
                }
            }
            i++;
        }
    }

    public static double getAvgGrade(List<Assessment> assessments,
                                     boolean ectsWeighting, AssessmentType type, boolean positiveOnly) {
        double sum = 0;
        double count = 0;

        if (assessments != null) {
            for (Assessment assessment : assessments) {
                if (type == AssessmentType.ALL || type == assessment.getAssessmentType()) {
                    if (!positiveOnly || assessment.getGrade().isPositive()) {
                        if (!ectsWeighting) {
                            sum += assessment.getGrade().getValue();
                            count++;
                        } else {
                            sum += assessment.getEcts() * assessment.getGrade().getValue();
                            count += assessment.getEcts();
                        }
                    }
                }
            }
        }

        if (count == 0) {
            return 0;
        } else {
            return sum / count;
        }
    }

    public static Account getAccount(Context context) {
        if (context == null) {
            return null;
        }
        // get first account
        Account[] accounts = AccountManager.get(context).getAccountsByType(
                KusssAuthenticator.ACCOUNT_TYPE);
        if (accounts.length == 0) {
            return null;
        }
        return accounts[0];
    }

    public static String getAccountName(Context context, Account account) {
        if (account == null) {
            return null;
        }
        return account.name;
    }

    public static String getAccountPassword(Context context, Account account) {
        if (account == null) {
            return null;
        }
        return AccountManager.get(context).getPassword(account);
    }

    @SuppressLint("NewApi")
    public static String getAccountAuthToken(Context context, Account account) {
        if (account == null) {
            return null;
        }

        AccountManager am = AccountManager.get(context);
        AccountManagerFuture<Bundle> response = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            response = am.getAuthToken(account,
                    KusssAuthenticator.AUTHTOKEN_TYPE_READ_ONLY, null, true,
                    null, null);
        } else {
            response = am.getAuthToken(account,
                    KusssAuthenticator.AUTHTOKEN_TYPE_READ_ONLY, true, null,
                    null);
        }

        try {
            return response.getResult().getString(AccountManager.KEY_AUTHTOKEN);
        } catch (OperationCanceledException | AuthenticatorException
                | IOException e) {
            Log.e(TAG, "getAccountAuthToken", e);
            return null;
        }
    }

    public static void addSerieToPieChart(List<Entry> values, List<String> captions, List<Integer> colors, String category,
                                          double value, double ects, int color) {
        if (value > 0) {
            values.add(new EctsEntry((float) value, (float) ects, values.size()));
            captions.add(category);
            colors.add(color);
        }
    }

    public static double getGradePercent(List<Assessment> assessments, Grade grade, boolean ectsWeighting) {
        if (assessments == null || assessments.size() == 0) return 0;

        double count = 0;
        double sum = 0;
        double value;

        for (Assessment assessment : assessments) {
            if (!ectsWeighting) {
                value = 1;
            } else {
                value = assessment.getEcts();
            }

            if (assessment.getGrade().equals(grade)) {
                count += value;
            }
            sum += value;
        }

        return count / sum * 100;
    }

    public static double getGradeEcts(List<Assessment> assessments, Grade grade) {
        if (assessments == null || assessments.size() == 0) return 0;

        double sum = 0;
        for (Assessment assessment : assessments) {
            if (assessment.getGrade().equals(grade)) {
                sum += assessment.getEcts();
            }
        }
        return sum;
    }


    public static List<LvaWithGrade> getLvasWithGrades(List<Term> terms, List<Course> courses, List<Assessment> assessments, boolean withAssessmentOnly, Term forLastTerm) {
        List<LvaWithGrade> result = new ArrayList<>();

        Map<String, Term> termMap = null;
        if (terms != null) {
            termMap = new HashMap<>();
            for (Term term : terms) {
                termMap.put(term.toString(), term);
            }
        }

        for (Course course : courses) {
            if (termMap == null || termMap.containsKey(course.getTerm().toString())) {
                Assessment assessment = findAssessment(assessments, course);
                if (!withAssessmentOnly || assessment != null || (forLastTerm != null && forLastTerm.equals(course.getTerm()))) {
                    result.add(new LvaWithGrade(course, assessment));
                }
            }
        }

        AppUtils.removeDuplicates(result);

        AppUtils.sortLVAsWithGrade(result);

        return result;
    }

    private static Assessment findAssessment(List<Assessment> assessments, Course course) {
        Assessment finalAssessment = null;

        for (Assessment assessment : assessments) {
            if (assessment.getCode().equals(course.getCode()) && assessment.getCourseId().equals(course.getCourseId())) {
                if (finalAssessment == null || finalAssessment.getGrade().getValue() > assessment.getGrade().getValue()) {
                    finalAssessment = assessment;
                }
            }
        }

        if (finalAssessment == null) {
            for (Assessment assessment : assessments) {
                if (assessment.getCode().equals(course.getCode()) &&
                        (assessment.getTitle().contains(course.getTitle()) ||
                                course.getTitle().contains(assessment.getTitle()))) {
                    if (finalAssessment == null || finalAssessment.getGrade().getValue() > assessment.getGrade().getValue()) {
                        finalAssessment = assessment;
                    }
                }
            }
            if (finalAssessment != null) {
                Log.d(TAG, String.format("found by courseId/title: %s/%s", course.getCourseId(), course.getTitle()));
            }
        } else {
            Log.d(TAG, String.format("found by courseId/code: %s/%s", course.getCourseId(), course.getCode()));
        }

        return finalAssessment;
    }

    private static void addIfRecent(List<Assessment> assessments, Assessment assessment) {
        int i = 0;
        while (i < assessments.size()) {
            Assessment g = assessments.get(i);
            // check only assessments for same lva and term
            if (g.getCode().equals(assessment.getCode())
                    && g.getCourseId().equals(assessment.getCourseId())) {
                // keep only recent (best and newest) assessment
                if (g.getDate().before(assessment.getDate())) {
                    // remove last assessment
                    assessments.remove(i);
                } else {
                    // break without adding
                    return;
                }
            } else {
                i++;
            }
        }
        // finally add assessment
        assessments.add(assessment);
    }

    public static List<Assessment> filterAssessments(List<Term> terms, List<Assessment> assessments) {
        List<Assessment> result = new ArrayList<>();
        Map<String, Term> termMap = null;
        if (terms != null) {
            termMap = new HashMap<>();
            for (Term term : terms) {
                termMap.put(term.toString(), term);
            }
        }

        if (assessments != null) {
            for (Assessment assessment : assessments) {
                if (termMap == null || (assessment.getTerm() != null && termMap.containsKey(assessment.getTerm().toString()))) {
                    addIfRecent(result, assessment);
                }
            }
        }
        AppUtils.sortAssessments(result);

        return result;
    }

    public static void sortCurricula(List<Curriculum> mCurricula) {
        Collections.sort(mCurricula, CurriculaComparator);
    }

    public static String getRowString(Cursor c) {
        if (c == null) {
            return null;
        }

        String row = "";
        for (int i = 0; i < c.getColumnCount(); i++) {
            try {
                row = row + c.getColumnName(i) + "=" + c.getString(i) + ";";
            } catch (Exception e) {
                row = row + "@" + Integer.toString(i) + "=?;";
            }
        }
        return row;
    }

    public static void updateSyncAlarm(Context context, boolean reCreateAlarm) {
        boolean mIsCalendarSyncEnabled = false;
        boolean mIsKusssSyncEnable = false;
        boolean mIsMasterSyncEnabled = ContentResolver.getMasterSyncAutomatically();

        if (mIsMasterSyncEnabled) {
            final Account mAccount = getAccount(context);
            if (mAccount != null) {
                mIsCalendarSyncEnabled = ContentResolver.getSyncAutomatically(mAccount, CalendarContractWrapper.AUTHORITY());
                mIsKusssSyncEnable = ContentResolver.getSyncAutomatically(mAccount, KusssContentContract.AUTHORITY);
            }
        }

        Log.d(TAG, String.format("MasterSync=%b, CalendarSync=%b, KusssSync=%b", mIsMasterSyncEnabled, mIsCalendarSyncEnabled, mIsKusssSyncEnable));

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SyncAlarmService.class);
        intent.putExtra(Consts.ARG_UPDATE_CAL, !mIsCalendarSyncEnabled);
        intent.putExtra(Consts.ARG_UPDATE_KUSSS, !mIsKusssSyncEnable);
        intent.putExtra(Consts.ARG_RECREATE_SYNC_ALARM, true);
        intent.putExtra(Consts.SYNC_SHOW_PROGRESS, true);

        // check if pending intent exists
        reCreateAlarm = reCreateAlarm || (PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE) == null);

        // new pending intent
        PendingIntent alarmIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (!mIsMasterSyncEnabled || !mIsCalendarSyncEnabled || !mIsKusssSyncEnable) {
            if (reCreateAlarm) {
                long interval = PreferenceWrapper.getSyncInterval(context) * DateUtils.MILLIS_PER_HOUR;

                // synchronize in half an hour
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + AlarmManager.INTERVAL_HALF_HOUR, interval, alarmIntent);
            }
        } else {
            am.cancel(alarmIntent);
        }
    }


    public static void triggerSync(Context context, Account account, boolean syncCalendar, boolean syncKusss) {
        try {
            if (context == null || account == null) {
                return;
            }

            if (syncCalendar || syncKusss) {
                Bundle b = new Bundle();
                // Disable sync backoff and ignore sync preferences. In other
                // words...perform sync NOW!
                b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                b.putBoolean(Consts.SYNC_SHOW_PROGRESS, true);

                if (syncCalendar) {
                    ContentResolver.requestSync(account, // Sync
                            CalendarContractWrapper.AUTHORITY(), // Calendar Content authority
                            b); // Extras
                }
                if (syncKusss) {
                    ContentResolver.requestSync(account, // Sync
                            KusssContentContract.AUTHORITY, // KUSSS Content authority
                            b); // Extras
                }
            }
        } catch (Exception e) {
            Analytics.sendException(context, e, true);
        }
    }

    public static String getTimeString(Date dtStart, Date dtEnd) {
        DateFormat dfStart = DateFormat.getTimeInstance(DateFormat.SHORT);
        DateFormat dfEnd = DateFormat.getTimeInstance(DateFormat.SHORT);
        if (!DateUtils.isSameDay(dtStart, dtEnd)) {
            dfEnd = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        }

        return String.format("%s - %s", dfStart.format(dtStart),
                dfEnd.format(dtEnd));
    }

    public static String getEventString(long eventDTStart, long eventDTEnd,
                                        String eventTitle) {
        int index = eventTitle.indexOf(", ");
        if (index > 1) {
            eventTitle = eventTitle.substring(0, index);
        }

        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

        return String.format("%s: %s, %s", eventTitle, df.format(eventDTStart), AppUtils.getTimeString(new Date(eventDTStart), new Date(eventDTEnd)));
    }

    public static String termToString(Term term) {
        if (term != null) {
            return term.toString();
        }
        return "";
    }
}
