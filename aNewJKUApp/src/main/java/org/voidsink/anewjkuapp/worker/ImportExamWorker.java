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
import androidx.work.WorkerParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.CourseMap;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.base.BaseWorker;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.kusss.Exam;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.notification.NewExamNotification;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportExamWorker extends BaseWorker {

    private static final Logger logger = LoggerFactory.getLogger(ImportExamWorker.class);

    public ImportExamWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return importExams();
    }

    private Result importExams() {
        Analytics.eventReloadExams(getApplicationContext());

        final Account mAccount = AppUtils.getAccount(getApplicationContext());
        if (mAccount == null) {
            return getSuccess();
        }

        final ContentResolver mResolver = getApplicationContext().getContentResolver();
        if (mResolver == null) {
            return getFailure();
        }

        final ContentProviderClient mProvider = mResolver.acquireContentProviderClient(KusssContentContract.Exam.CONTENT_URI);

        if (mProvider == null) {
            return getFailure();
        }

        final long mSyncFromNow = System.currentTimeMillis() / DateUtils.DAY_IN_MILLIS * DateUtils.DAY_IN_MILLIS;

        showUpdateNotification(R.string.notification_sync_exam, R.string.notification_sync_exam_loading);

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
                    return getRetry();
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

                    try (Cursor c = mProvider.query(examUri, KusssContentContract.Exam.DB.PROJECTION,
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
                                examId = c.getInt(KusssContentContract.Exam.DB.COL_ID);
                                examTerm = c.getString(KusssContentContract.Exam.DB.COL_TERM);
                                examCourseId = c.getString(KusssContentContract.Exam.DB.COL_COURSEID);
                                examDtStart = c.getLong(KusssContentContract.Exam.DB.COL_DTSTART);
                                examDtEnd = c.getLong(KusssContentContract.Exam.DB.COL_DTEND);
                                examLocation = c.getString(KusssContentContract.Exam.DB.COL_LOCATION);

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
                return getRetry();
            }

            mChangedNotification.show();
            return getSuccess();
        } catch (Exception e) {
            Analytics.sendException(getApplicationContext(), e, true);
            logger.error("import failed", e);

            return getRetry();
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mProvider.close();
            } else {
                mProvider.release();
            }
            cancelUpdateNotification();
        }
    }

    private String getEventString(Context c, Exam exam) {
        return AppUtils.getEventString(c, exam.getDtStart().getTime(), exam.getDtEnd().getTime(), exam.getTitle(), false);
    }

}
