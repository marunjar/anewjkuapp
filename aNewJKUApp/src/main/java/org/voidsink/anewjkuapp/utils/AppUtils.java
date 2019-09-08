/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2019 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp.utils;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.KusssAuthenticator;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
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
import org.voidsink.anewjkuapp.worker.ImportAssessmentWorker;
import org.voidsink.anewjkuapp.worker.ImportCalendarWorker;
import org.voidsink.anewjkuapp.worker.ImportCourseWorker;
import org.voidsink.anewjkuapp.worker.ImportCurriculaWorker;
import org.voidsink.anewjkuapp.worker.ImportExamWorker;
import org.voidsink.anewjkuapp.worker.ImportPoiWorker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AppUtils {

    private static final String DEFAULT_POI_FILE_NAME = "JKU.gpx";
    private static final String ARG_WORKER_CAL_HELPER = "UPDATE_CAL";

    private static final Logger logger = LoggerFactory.getLogger(AppUtils.class);

    private static final Comparator<Course> CourseComparator = (lhs, rhs) -> {
        int value = lhs.getTitle().compareTo(rhs.getTitle());
        if (value == 0) {
            value = lhs.getTerm().compareTo(rhs.getTerm());
        }
        return value;
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

    private static final Comparator<Curriculum> CurriculaComparator = (lhs, rhs) -> {
        int value = lhs.getUni().compareToIgnoreCase(rhs.getUni());
        if (value == 0) {
            value = lhs.getDtStart().compareTo(rhs.getDtStart());
        }
        if (value == 0) {
            value = lhs.getCid().compareTo(rhs.getCid());
        }
        return value;
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

    private static final Comparator<Term> TermComparator = (lhs, rhs) -> {
        if (lhs == null && rhs == null) return 0;
        if (lhs == null) return -1;
        if (rhs == null) return 1;

        return rhs.compareTo(lhs);
    };


    public static void doOnNewVersion(Context context) {
        int mLastVersion = PreferenceWrapper.getLastVersion(context);
        int mCurrentVersion = PreferenceWrapper.getCurrentVersion(context);

        boolean errorOccured = false;

        if (mLastVersion != mCurrentVersion
                || mLastVersion == PreferenceWrapper.PREF_LAST_VERSION_NONE) {
            try {
                if (!initPreferences(context)) {
                    errorOccured = true;
                }
                if (!copyDefaultMap(context)) {
                    errorOccured = true;
                }
                if (!importDefaultPois(context)) {
                    errorOccured = true;
                }
            } catch (Exception e) {
                logger.error("doOnNewVersion failed", e);
                Analytics.sendException(context, e, false);
                errorOccured = true;
            }
        }

        // only if another version was installed before
        if (mLastVersion != mCurrentVersion
                && mLastVersion != PreferenceWrapper.PREF_LAST_VERSION_NONE) {
            try {
                if (shouldRemoveOldAccount(mLastVersion, mCurrentVersion)) {
                    if (!removeAccount(context)) {
                        errorOccured = true;
                    }
                }
                if (shouldImportCurricula(mLastVersion, mCurrentVersion)) {
                    triggerSync(context, true, Consts.ARG_WORKER_KUSSS_CURRICULA);
                }
                if (shouldRecreateCalendars(mLastVersion, mCurrentVersion)) {
                    if (!removeCalendars(context)) {
                        errorOccured = true;
                    } else if (!createCalendars(context)) {
                        errorOccured = true;
                    } else {
                        triggerSync(context, true, Consts.ARG_WORKER_CAL_COURSES, Consts.ARG_WORKER_CAL_EXAM);
                    }
                } else if (shouldDeleteKusssEvents(mLastVersion, mCurrentVersion)) {
                    if (!deleteKusssEvents(context)) {
                        errorOccured = true;
                    }
                }

                PreferenceWrapper.applySyncInterval(context);
            } catch (Exception e) {
                logger.error("doOnVersionChange failed", e);
                Analytics.sendException(context, e, false);
                errorOccured = true;
            }
        }
        if (!errorOccured) {
            PreferenceWrapper.setLastVersion(context, mCurrentVersion);
        }
    }

    private static boolean createCalendars(Context context) {
        Account account = AppUtils.getAccount(context);
        if (account == null) {
            return true;
        }
        return CalendarUtils.createCalendarsIfNecessary(context, account);
    }

    private static boolean deleteKusssEvents(Context context) {
        Account account = AppUtils.getAccount(context);
        return (account == null || CalendarUtils.deleteKusssEvents(context, account));
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

    private static boolean shouldDeleteKusssEvents(int lastVersion, int currentVersion) {
        // events changed with 140053
        return (lastVersion <= 140052 && currentVersion > 140052);
    }

    private static boolean shouldImportCurricula(int lastVersion, int currentVersion) {
        // curricula added with 140026
        // import on startup to avoid strange behaviour and missing tabs
        return (lastVersion < 140026 && currentVersion >= 140026);
    }

    private static boolean removeAccount(Context context) {
        Account account = getAccount(context);
        if (account != null) {
            removeAccout(AccountManager.get(context), account);
        }
        return true;
    }

    public static void removeAccout(AccountManager accountManager, Account account) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            accountManager.removeAccount(account, null, null, null);
        } else {
            accountManager.removeAccount(account, null, null);
        }
        logger.debug("account removed");
    }

    private static boolean shouldRemoveOldAccount(int lastVersion,
                                                  int currentVersion) {
        // calendar names changed with 100017, remove account for avoiding
        // corrupted data
        return (lastVersion < 140017 && currentVersion >= 140017);
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
            logger.error("initPreferences", e);
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
            logger.error("copyDefaultMap", e);
            return false;
        } catch (IOException e) {
            logger.error("copyDefaultMap", e);
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
            OneTimeWorkRequest.Builder importRequest = setupOneTimeWorkRequest(true, Consts.ARG_WORKER_POI);
            if (importRequest != null) {
                importRequest.setInputData(new Data.Builder().putString(Consts.ARG_FILENAME, DEFAULT_POI_FILE_NAME).putBoolean(Consts.ARG_IS_DEFAULT, true).build());

                WorkManager workManager = WorkManager.getInstance(context);
                workManager.beginWith(importRequest.build()).enqueue();
            }
            return true;
        } catch (IOException e) {
            Analytics.sendException(context, e, false);
            return false;
        }
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

    private static void sortLVAsWithGrade(List<LvaWithGrade> mCourses) {
        Collections.sort(mCourses, LvaWithGradeComparator);

    }

    public static void sortAssessments(List<Assessment> assessments) {
        Collections.sort(assessments, AssessmentComparator);
    }

    private static void removeDuplicates(List<LvaWithGrade> mCourses) {
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
                        logger.debug("remove from done {} {}", nextCourse.getCode(), nextCourse.getTitle());
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
                        logger.debug("remove from other {} {}", nextCourse.getCode(), nextCourse.getTitle());
                    } else {
                        j++;
                    }
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

        if ((android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) && (ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)) {
            logger.warn("getAccount failed, no permission");
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

    public static String getAccountName(Account account) {
        return account == null ? null : account.name;
    }

    public static String getAccountPassword(Context context, Account account) {
        if (account == null) {
            return null;
        }
        return AccountManager.get(context).getPassword(account);
    }

    public static String getAccountAuthToken(Context context, Account account) {
        if (account == null) {
            return null;
        }

        AccountManager am = AccountManager.get(context);
        AccountManagerFuture<Bundle> response = am.getAuthToken(account,
                KusssAuthenticator.AUTHTOKEN_TYPE_READ_ONLY, null, true,
                null, null);

        if (response == null) return null;

        try {
            return response.getResult().getString(AccountManager.KEY_AUTHTOKEN);
        } catch (OperationCanceledException | AuthenticatorException
                | IOException e) {
            logger.error("getAccountAuthToken", e);
            return null;
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


    public static List<LvaWithGrade> getGradesWithLva(List<Term> terms, List<Course> courses, List<Assessment> assessments, boolean withAssessmentOnly, Term forLastTerm) {
        List<LvaWithGrade> result = new ArrayList<>();

        Map<String, Term> termMap = null;
        if (terms != null) {
            termMap = new HashMap<>();
            for (Term term : terms) {
                termMap.put(term.toString(), term);
            }
        }

        List<Assessment> done = new ArrayList<>();

        for (Course course : courses) {
            if (termMap == null || termMap.containsKey(course.getTerm().toString())) {
                Assessment assessment = findAssessment(assessments, course);
                if (!withAssessmentOnly || assessment != null || (forLastTerm != null && forLastTerm.equals(course.getTerm()))) {
                    result.add(new LvaWithGrade(course, assessment));
                    done.add(assessment);
                }
            }
        }

        AppUtils.removeDuplicates(result);

        AppUtils.sortLVAsWithGrade(result);

        for (Assessment assessment : assessments) {
            if (!done.contains(assessment)) {
                Term assessmentTerm = Term.fromDate(assessment.getDate());
                if (termMap == null || termMap.containsKey(assessmentTerm.toString())) {
                    result.add(new LvaWithGrade(new Course(assessmentTerm, assessment.getCourseId(), assessment.getTitle(), assessment.getCid(), null, assessment.getSws(), assessment.getEcts(), assessment.getLvaType(), assessment.getCode()), assessment));
                }
            }
        }

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
                logger.debug("found by code/title: {}/{} -> {}", course.getCode(), course.getTitle(), course.getCourseId());
            }
        } else {
            logger.debug("found by code/courseId: {}/{} -> {}", course.getCode(), course.getCourseId(), course.getTitle());
        }

        return finalAssessment;
    }

    private static void addIfRecent(List<Assessment> assessments, Assessment assessment) {
        if (!assessment.getAssessmentType().isDuplicatesPossible()) {
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

    public static boolean isWorkScheduled(Context context, String tag) {
        WorkManager workManager = WorkManager.getInstance(context);
        ListenableFuture<List<WorkInfo>> workInfosFuture = workManager.getWorkInfosByTag(tag);
        try {
            List<WorkInfo> workInfos = workInfosFuture.get();
            if (workInfos == null || workInfos.isEmpty()) {
                return false;
            }
            for (WorkInfo workInfo : workInfos) {
                if (!workInfo.getState().isFinished()) {
                    return true;
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static PeriodicWorkRequest.Builder setupPeriodicWorkRequest(Context context,
                                                                        @NonNull Class<? extends ListenableWorker> workerClass, String... tags) {
        Constraints.Builder constraints = new Constraints.Builder();
        constraints.setRequiredNetworkType(NetworkType.CONNECTED);

        long interval = PreferenceWrapper.getSyncInterval(context);

        PeriodicWorkRequest.Builder request = new PeriodicWorkRequest.Builder(workerClass, interval, TimeUnit.HOURS, 6, TimeUnit.HOURS);
        request.setInitialDelay(30, TimeUnit.MINUTES);
        request.setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES);
        request.setConstraints(constraints.build());
        for (String tag : tags) {
            request.addTag(tag);
        }
        request.setInputData(new Data.Builder().putBoolean(Consts.SYNC_SHOW_PROGRESS, true).build());

        return request;
    }

    private static Class<? extends ListenableWorker> getWorkerClassByTag(String tag) {
        if (Consts.ARG_WORKER_CAL_COURSES.equals(tag)) {
            return ImportCalendarWorker.class;
        } else if (Consts.ARG_WORKER_CAL_EXAM.equals(tag)) {
            return ImportCalendarWorker.class;
        } else if (Consts.ARG_WORKER_KUSSS_CURRICULA.equals(tag)) {
            return ImportCurriculaWorker.class;
        } else if (Consts.ARG_WORKER_KUSSS_ASSESSMENTS.equals(tag)) {
            return ImportAssessmentWorker.class;
        } else if (Consts.ARG_WORKER_POI.equals(tag)) {
            return ImportPoiWorker.class;
        } else {
            return null;
        }
    }

    private static OneTimeWorkRequest.Builder setupOneTimeWorkRequest(boolean immediately, String tag) {
        Class<? extends ListenableWorker> workerClass = getWorkerClassByTag(tag);
        if (workerClass == null) {
            return null;
        }

        Constraints.Builder constraints = new Constraints.Builder();
        constraints.setRequiredNetworkType(NetworkType.CONNECTED);

        OneTimeWorkRequest.Builder request = new OneTimeWorkRequest.Builder(workerClass);
        request.setInitialDelay(immediately ? 0 : 30, TimeUnit.MINUTES);
        request.setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES);
        request.setConstraints(constraints.build());
        request.setInputData(new Data.Builder().putBoolean(Consts.SYNC_SHOW_PROGRESS, true).build());
        request.addTag(tag);

        return request;
    }

    public static void enableSync(Context context, boolean reCreateAlarms) {
        boolean mIsCalendarSyncEnabled = false;
        boolean mIsMasterSyncEnabled = ContentResolver.getMasterSyncAutomatically();

        if (mIsMasterSyncEnabled) {
            final Account mAccount = getAccount(context);
            if (mAccount != null) {
                mIsCalendarSyncEnabled = ContentResolver.getSyncAutomatically(mAccount, CalendarContractWrapper.AUTHORITY());
            }
        }

        logger.debug("MasterSync={}, CalendarSync={}", mIsMasterSyncEnabled, mIsCalendarSyncEnabled);

        WorkManager workManager = WorkManager.getInstance(context);
        if (mIsCalendarSyncEnabled) {
            if (reCreateAlarms || !isWorkScheduled(context, ARG_WORKER_CAL_HELPER)) {
                workManager.cancelAllWorkByTag(ARG_WORKER_CAL_HELPER);

                PeriodicWorkRequest.Builder courseCalendarRequest = setupPeriodicWorkRequest(context, ImportCalendarWorker.class, ARG_WORKER_CAL_HELPER, Consts.ARG_WORKER_CAL_COURSES);
                workManager.enqueueUniquePeriodicWork(Consts.ARG_WORKER_CAL_COURSES, ExistingPeriodicWorkPolicy.REPLACE, courseCalendarRequest.build());


                PeriodicWorkRequest.Builder examCalendarRequest = setupPeriodicWorkRequest(context, ImportCalendarWorker.class, ARG_WORKER_CAL_HELPER, Consts.ARG_WORKER_CAL_EXAM);
                workManager.enqueueUniquePeriodicWork(Consts.ARG_WORKER_CAL_EXAM, ExistingPeriodicWorkPolicy.REPLACE, examCalendarRequest.build());
            }
        } else {
            workManager.cancelAllWorkByTag(ARG_WORKER_CAL_HELPER);
        }

        if (reCreateAlarms || !isWorkScheduled(context, Consts.ARG_WORKER_KUSSS_CURRICULA)) {
            workManager.cancelAllWorkByTag(Consts.ARG_WORKER_KUSSS_CURRICULA);

            PeriodicWorkRequest.Builder curriculaRequest = setupPeriodicWorkRequest(context, ImportCurriculaWorker.class, Consts.ARG_WORKER_KUSSS_CURRICULA);
            workManager.enqueueUniquePeriodicWork(Consts.ARG_WORKER_KUSSS_CURRICULA, ExistingPeriodicWorkPolicy.REPLACE, curriculaRequest.build());
        }
        if (reCreateAlarms || !isWorkScheduled(context, Consts.ARG_WORKER_KUSSS_COURSES)) {
            workManager.cancelAllWorkByTag(Consts.ARG_WORKER_KUSSS_COURSES);

            PeriodicWorkRequest.Builder courseRequest = setupPeriodicWorkRequest(context, ImportCourseWorker.class, Consts.ARG_WORKER_KUSSS_COURSES);
            workManager.enqueueUniquePeriodicWork(Consts.ARG_WORKER_KUSSS_COURSES, ExistingPeriodicWorkPolicy.REPLACE, courseRequest.build());
        }
        if (reCreateAlarms || !isWorkScheduled(context, Consts.ARG_WORKER_KUSSS_ASSESSMENTS)) {
            workManager.cancelAllWorkByTag(Consts.ARG_WORKER_KUSSS_ASSESSMENTS);

            PeriodicWorkRequest.Builder assessmentRequest = setupPeriodicWorkRequest(context, ImportAssessmentWorker.class, Consts.ARG_WORKER_KUSSS_ASSESSMENTS);
            workManager.enqueueUniquePeriodicWork(Consts.ARG_WORKER_KUSSS_ASSESSMENTS, ExistingPeriodicWorkPolicy.REPLACE, assessmentRequest.build());
        }
        if (reCreateAlarms || !isWorkScheduled(context, Consts.ARG_WORKER_KUSSS_EXAMS)) {
            workManager.cancelAllWorkByTag(Consts.ARG_WORKER_KUSSS_EXAMS);

            PeriodicWorkRequest.Builder assessmentRequest = setupPeriodicWorkRequest(context, ImportExamWorker.class, Consts.ARG_WORKER_KUSSS_EXAMS);
            workManager.enqueueUniquePeriodicWork(Consts.ARG_WORKER_KUSSS_EXAMS, ExistingPeriodicWorkPolicy.REPLACE, assessmentRequest.build());
        }
    }

    public static void triggerSync(Context context, boolean immediately, String... tags) {
        if (tags.length > 0) {
            String uniqueName = "";
            List<OneTimeWorkRequest> requests = new ArrayList<>();
            for (String tag : tags) {
                OneTimeWorkRequest.Builder request = setupOneTimeWorkRequest(immediately, tag);
                if (request != null) {
                    uniqueName = uniqueName + tag;
                    requests.add(request.build());
                }
            }

            if (requests.size() > 0) {
                WorkManager workManager = WorkManager.getInstance(context);
                WorkContinuation continuation = workManager.beginUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, requests.get(0));
                for (int i = 1; i < requests.size(); i++) {
                    continuation = continuation.then(requests.get(i));
                }
                continuation.enqueue();
            }
        }
    }

    public static void triggerSync(Context context, Account account, boolean syncKusss) {
        try {
            if (context == null || account == null) {
                return;
            }

            if (syncKusss) {
                Bundle b = new Bundle();
                // Disable sync backoff and ignore sync preferences. In other
                // words...perform sync NOW!
                b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                b.putBoolean(Consts.SYNC_SHOW_PROGRESS, true);

                ContentResolver.requestSync(account, // Sync
                        KusssContentContract.AUTHORITY, // KUSSS Content authority
                        b); // Extras
            }
        } catch (Exception e) {
            Analytics.sendException(context, e, true);
        }
    }

    public static String getTimeString(Context c, Date dtStart, Date dtEnd, boolean allDay) {
        int flags = 0;
        String tzString = TimeZone.getDefault().getID();
        if (allDay) {
            tzString = "UTC";
        } else {
            flags = DateUtils.FORMAT_SHOW_TIME;
        }

        return DateUtils.formatDateRange(c, new Formatter(Locale.getDefault()), dtStart.getTime(), dtEnd.getTime(), flags, tzString).toString();
    }


    public static String getEventString(Context c, long eventDTStart, long eventDTEnd,
                                        String eventTitle, boolean allDay) {
        int index = eventTitle.indexOf(", ");
        if (index > 1) {
            eventTitle = eventTitle.substring(0, index);
        }

        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

        return String.format("%s: %s, %s", df.format(eventDTStart), AppUtils.getTimeString(c, new Date(eventDTStart), new Date(eventDTEnd), allDay), eventTitle);
    }

    public static String termToString(Term term) {
        if (term != null) {
            return term.toString();
        }
        return "";
    }

    public static void showEventInCalendar(Context context, long eventId, long dtStart) {
        if (eventId > 0) {
            Uri uri = ContentUris.withAppendedId(CalendarContractWrapper.Events.CONTENT_URI(), eventId);
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setData(uri);
            context.startActivity(intent);
        } else {
            Uri.Builder builder = CalendarContractWrapper.CONTENT_URI().buildUpon();
            builder.appendPath("time");
            ContentUris.appendId(builder, dtStart);
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setData(builder.build());
            context.startActivity(intent);
        }
    }

    public static void showEventLocation(Context context, String location) {
        Intent intent = new Intent(context, MainActivity.class)
                .putExtra(MainActivity.ARG_SHOW_FRAGMENT_ID, R.id.nav_map)
                .putExtra(MainActivity.ARG_SAVE_LAST_FRAGMENT, false)
                .setAction(Intent.ACTION_SEARCH)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (!TextUtils.isEmpty(location)) {
            intent.putExtra(SearchManager.QUERY, location);
            intent.putExtra(MainActivity.ARG_EXACT_LOCATION, true);
        } else {
            intent.putExtra(SearchManager.QUERY, "Uniteich");
        }
        context.startActivity(intent);
    }

    @Deprecated
    public static boolean executeEm(Context context, Callable<?>[] callables, boolean wait) {
        boolean result;

        ExecutorService es = Executors.newSingleThreadExecutor();
        try {
            result = executeEm(es, context, callables, wait);
        } finally {
            es.shutdown();
        }
        return result;
    }

    @Deprecated
    public static boolean executeEm(ExecutorService es, Context context, Callable<?>[] callables, boolean wait) {
        boolean result = true;

        List<Future<?>> futures = new ArrayList<>();

        for (Callable<?> c : callables) {
            futures.add(es.submit(c));
        }

        if (wait) {
            for (Future f : futures) {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException e) {
                    Analytics.sendException(context, e, false);
                    result = false;
                }
            }
        }
        return result;
    }

    public static int getRandomColor() {
        Random rand = new Random(System.currentTimeMillis());

        float hue;
        do {
            hue = rand.nextFloat() * 360;
        } while ((Math.abs(mLastHue - hue) < 45) ||
                (hue > 280 && hue < 320));

        mLastHue = hue;

        float[] hsv = new float[3];
        hsv[0] = hue;
        hsv[1] = 0.95f;
        hsv[2] = 0.8f;

        return Color.HSVToColor(hsv);
    }

    private static float mLastHue = new Random(System.currentTimeMillis()).nextFloat() * 360;

    public static Locale getLocale(Context context) {
        Locale locale;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = context.getResources().getConfiguration().locale;
            }
        } catch (Exception e) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    public static String format(Context c, String format, Object... args) {
        return String.format(getLocale(c), format, args);
    }

    public static void setupNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                if (mNotificationManager != null) {
                    // create default channel
                    NotificationChannel defaultChannel = new NotificationChannel(Consts.CHANNEL_ID_DEFAULT,
                            context.getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
                    // Sets whether notifications posted to this channel should display notification lights
                    defaultChannel.enableLights(true);
                    // Sets whether notification posted to this channel should vibrate.
                    defaultChannel.enableVibration(true);
                    // Sets the notification light color for notifications posted to this channel
                    defaultChannel.setLightColor(Color.BLUE);
                    // Sets whether notifications posted to this channel appear on the lockscreen or not
                    defaultChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                    defaultChannel.setShowBadge(false);

                    mNotificationManager.createNotificationChannel(defaultChannel);

                    // create exams channel
                    NotificationChannel examsChannel = new NotificationChannel(Consts.CHANNEL_ID_EXAMS,
                            context.getString(R.string.title_exams), NotificationManager.IMPORTANCE_DEFAULT);
                    // Sets whether notifications posted to this channel should display notification lights
                    examsChannel.enableLights(true);
                    // Sets whether notification posted to this channel should vibrate.
                    examsChannel.enableVibration(true);
                    // Sets the notification light color for notifications posted to this channel
                    examsChannel.setLightColor(Color.RED);
                    // Sets whether notifications posted to this channel appear on the lockscreen or not
                    examsChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                    defaultChannel.setShowBadge(false);

                    mNotificationManager.createNotificationChannel(examsChannel);

                    // create grades channel
                    NotificationChannel gradesChannel = new NotificationChannel(Consts.CHANNEL_ID_GRADES,
                            context.getString(R.string.title_grades), NotificationManager.IMPORTANCE_HIGH);
                    // Sets whether notifications posted to this channel should display notification lights
                    gradesChannel.enableLights(true);
                    // Sets whether notification posted to this channel should vibrate.
                    gradesChannel.enableVibration(true);
                    // Sets the notification light color for notifications posted to this channel
                    gradesChannel.setLightColor(Color.GREEN);
                    // Sets whether notifications posted to this channel appear on the lockscreen or not
                    gradesChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                    defaultChannel.setShowBadge(true);

                    mNotificationManager.createNotificationChannel(gradesChannel);
                }
            } catch (Exception e) {
                Analytics.sendException(context, e, true);
            }
        }
    }

}
