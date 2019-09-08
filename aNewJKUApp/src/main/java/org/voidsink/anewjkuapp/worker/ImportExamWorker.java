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

package org.voidsink.anewjkuapp.worker;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.CourseMap;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.kusss.Exam;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.notification.NewExamNotification;
import org.voidsink.anewjkuapp.notification.SyncNotification;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportExamWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(ImportExamWorker.class);

    private SyncNotification mUpdateNotification;

    public static final String[] EXAM_PROJECTION = new String[]{
            KusssContentContract.Exam.COL_ID,
            KusssContentContract.Exam.COL_TERM,
            KusssContentContract.Exam.COL_COURSEID,
            KusssContentContract.Exam.COL_DTSTART,
            KusssContentContract.Exam.COL_DTEND,
            KusssContentContract.Exam.COL_LOCATION,
            KusssContentContract.Exam.COL_DESCRIPTION,
            KusssContentContract.Exam.COL_INFO,
            KusssContentContract.Exam.COL_IS_REGISTERED,
            KusssContentContract.Exam.COL_TITLE};

    private static final int COLUMN_EXAM_ID = 0;
    public static final int COLUMN_EXAM_TERM = 1;
    public static final int COLUMN_EXAM_COURSEID = 2;
    public static final int COLUMN_EXAM_DTSTART = 3;
    public static final int COLUMN_EXAM_DTEND = 4;
    public static final int COLUMN_EXAM_LOCATION = 5;
    public static final int COLUMN_EXAM_DESCRIPTION = 6;
    public static final int COLUMN_EXAM_INFO = 7;
    public static final int COLUMN_EXAM_IS_REGISTERED = 8;
    public static final int COLUMN_EXAM_TITLE = 9;


    public ImportExamWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return importExams();
    }

    @Override
    public void onStopped() {
        super.onStopped();

        cancelUpdateNotification();
    }

    private Result importExams() {
        Analytics.eventReloadExams(getApplicationContext());

        final Account mAccount = AppUtils.getAccount(getApplicationContext());
        if (mAccount == null) {
            return Result.success();
        }

        final ContentResolver mResolver = getApplicationContext().getContentResolver();
        if (mResolver == null) {
            return Result.failure();
        }

        final ContentProviderClient mProvider = mResolver.acquireContentProviderClient(KusssContentContract.Exam.CONTENT_URI);

        if (mProvider == null) {
            return Result.failure();
        }

        final long mSyncFromNow = System.currentTimeMillis() / DateUtils.DAY_IN_MILLIS * DateUtils.DAY_IN_MILLIS;

        if (getInputData().getBoolean(Consts.SYNC_SHOW_PROGRESS, false)) {
            mUpdateNotification = new SyncNotification(getApplicationContext(), R.string.notification_sync_exam);
            mUpdateNotification.show(getApplicationContext().getString(R.string.notification_sync_exam_loading));
        }

        NewExamNotification mChangedNotification = new NewExamNotification(getApplicationContext());

        try {
            logger.debug("setup connection");

            if (KusssHandler.getInstance().isAvailable(getApplicationContext(),
                    AppUtils.getAccountAuthToken(getApplicationContext(), mAccount),
                    AppUtils.getAccountName(mAccount),
                    AppUtils.getAccountPassword(getApplicationContext(), mAccount))) {
                updateNotification(getApplicationContext().getString(R.string.notification_sync_exam_loading));

                List<Exam> exams;
                if (PreferenceWrapper.getNewExamsByCourseId(getApplicationContext())) {
                    CourseMap courseMap = new CourseMap(getApplicationContext());
                    List<Term> terms = KusssContentProvider.getTerms(getApplicationContext());

                    logger.debug("load exams by courseId");
                    exams = KusssHandler.getInstance().getNewExamsByCourseId(
                            getApplicationContext(), courseMap.getCourses(), terms);
                } else {
                    logger.debug("load exams");
                    exams = KusssHandler.getInstance()
                            .getNewExams(getApplicationContext());
                }
                if (exams == null) {
                    return Result.retry();
                } else {
                    Map<String, Exam> examMap = new HashMap<>();
                    for (Exam exam : exams) {
                        Exam old = examMap.put(KusssHelper.getExamKey(exam.getCourseId(), AppUtils.termToString(exam.getTerm()), exam.getDtStart().getTime()), exam);
                        if (old != null) {
                            logger.warn("exam alread loaded: {}", KusssHelper.getExamKey(old.getCourseId(), AppUtils.termToString(old.getTerm()), old.getDtStart().getTime()));
                        }
                    }

                    logger.debug("got {} exams", exams.size());

                    updateNotification(getApplicationContext().getString(R.string.notification_sync_exam_updating));

                    ArrayList<ContentProviderOperation> batch = new ArrayList<>();

                    Uri examUri = KusssContentContract.Exam.CONTENT_URI;

                    try (Cursor c = mProvider.query(examUri, EXAM_PROJECTION,
                            null, null, null)) {
                        if (c == null) {
                            logger.warn("selection failed");
                        } else {
                            logger.debug("Found {} local entries. Computing merge solution...", c.getCount());
                            int examId;
                            String examTerm;
                            String examCourseId;
                            long examDtStart;
                            long examDtEnd;
                            String examLocation;

                            while (c.moveToNext()) {
                                examId = c.getInt(COLUMN_EXAM_ID);
                                examTerm = c.getString(COLUMN_EXAM_TERM);
                                examCourseId = c.getString(COLUMN_EXAM_COURSEID);
                                examDtStart = c.getLong(COLUMN_EXAM_DTSTART);
                                examDtEnd = c.getLong(COLUMN_EXAM_DTEND);
                                examLocation = c
                                        .getString(COLUMN_EXAM_LOCATION);

                                Exam exam = examMap.remove(KusssHelper.getExamKey(examCourseId, examTerm, examDtStart));
                                if (exam != null) {
                                    // Check to see if the entry needs to be
                                    // updated
                                    Uri existingUri = examUri
                                            .buildUpon()
                                            .appendPath(
                                                    Integer.toString(examId))
                                            .build();
                                    logger.debug("Scheduling update: {}", existingUri);

                                    if (!CalendarUtils.isSameDay(
                                            new Date(examDtStart), exam.getDtStart())
                                            || !new Date(examDtEnd).equals(exam.getDtEnd())
                                            || !examLocation.equals(exam.getLocation())) {
                                        mChangedNotification.addUpdate(getEventString(getApplicationContext(), exam));
                                    }

                                    batch.add(ContentProviderOperation
                                            .newUpdate(
                                                    KusssContentContract
                                                            .asEventSyncAdapter(
                                                                    existingUri,
                                                                    mAccount.name,
                                                                    mAccount.type))
                                            .withValue(
                                                    KusssContentContract.Exam.COL_ID,
                                                    Integer.toString(examId))
                                            .withValues(KusssHelper.getExamContentValues(exam))
                                            .build());
                                } else if (examDtStart >= mSyncFromNow) {
                                    // Entry doesn't exist. Remove only newer
                                    // events from the database.
                                    Uri deleteUri = examUri
                                            .buildUpon()
                                            .appendPath(
                                                    Integer.toString(examId))
                                            .build();
                                    logger.debug("Scheduling delete: {}", deleteUri);

                                    batch.add(ContentProviderOperation
                                            .newDelete(
                                                    KusssContentContract
                                                            .asEventSyncAdapter(
                                                                    deleteUri,
                                                                    mAccount.name,
                                                                    mAccount.type))
                                            .build());
                                }
                            }
                            for (Exam exam : examMap.values()) {
                                batch.add(ContentProviderOperation
                                        .newInsert(
                                                KusssContentContract
                                                        .asEventSyncAdapter(
                                                                examUri,
                                                                mAccount.name,
                                                                mAccount.type))
                                        .withValues(KusssHelper.getExamContentValues(exam))
                                        .build());
                                logger.debug("Scheduling insert: {} {}", exam.getTerm(), exam.getCourseId());

                                mChangedNotification.addInsert(getEventString(getApplicationContext(), exam));
                            }

                            updateNotification(getApplicationContext().getString(R.string.notification_sync_exam_saving));

                            if (batch.size() > 0) {
                                logger.debug("Applying batch update");
                                mProvider.applyBatch(batch);
                                logger.debug("Notify resolver");
                                mResolver
                                        .notifyChange(
                                                KusssContentContract.Exam.CONTENT_CHANGED_URI,
                                                null, // No
                                                // local
                                                // observer
                                                false); // IMPORTANT: Do not
                                // sync to
                                // network
                            } else {
                                logger.warn("No batch operations found! Do nothing");
                            }
                        }
                    }
                }

                KusssHandler.getInstance().logout(getApplicationContext());
            } else {
                return Result.retry();
            }

            mChangedNotification.show();
            return Result.success();
        } catch (Exception e) {
            Analytics.sendException(getApplicationContext(), e, true);
            logger.error("import failed", e);

            return Result.retry();
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mProvider.close();
            } else {
                mProvider.release();
            }
            cancelUpdateNotification();
        }
    }

    private void cancelUpdateNotification() {
        if (mUpdateNotification != null) {
            mUpdateNotification.cancel();
        }
    }

    private void updateNotification(String string) {
        if (mUpdateNotification != null) {
            mUpdateNotification.update(string);
        }
    }

    private String getEventString(Context c, Exam exam) {
        return AppUtils.getEventString(c, exam.getDtStart().getTime(), exam.getDtEnd().getTime(), exam.getTitle(), false);
    }

}
