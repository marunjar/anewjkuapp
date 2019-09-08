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

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.AssessmentType;
import org.voidsink.anewjkuapp.kusss.Curriculum;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.notification.AssessmentChangedNotification;
import org.voidsink.anewjkuapp.notification.SyncNotification;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportAssessmentWorker extends Worker {

    private static final Logger logger = LoggerFactory.getLogger(ImportAssessmentWorker.class);

    private SyncNotification mUpdateNotification;

    public static final String[] ASSESSMENT_PROJECTION = new String[]{
            KusssContentContract.Assessment.COL_ID,
            KusssContentContract.Assessment.COL_TERM,
            KusssContentContract.Assessment.COL_COURSEID,
            KusssContentContract.Assessment.COL_DATE,
            KusssContentContract.Assessment.COL_CURRICULA_ID,
            KusssContentContract.Assessment.COL_TYPE,
            KusssContentContract.Assessment.COL_GRADE,
            KusssContentContract.Assessment.COL_TITLE,
            KusssContentContract.Assessment.COL_CODE,
            KusssContentContract.Assessment.COL_ECTS,
            KusssContentContract.Assessment.COL_SWS,
            KusssContentContract.Assessment.COL_LVATYPE};

    // Constants representing column positions from PROJECTION.
    private static final int COLUMN_ASSESSMENT_ID = 0;
    public static final int COLUMN_ASSESSMENT_TERM = 1;
    public static final int COLUMN_ASSESSMENT_COURSEID = 2;
    public static final int COLUMN_ASSESSMENT_DATE = 3;
    public static final int COLUMN_ASSESSMENT_CURRICULA_ID = 4;
    public static final int COLUMN_ASSESSMENT_TYPE = 5;
    public static final int COLUMN_ASSESSMENT_GRADE = 6;
    public static final int COLUMN_ASSESSMENT_TITLE = 7;
    public static final int COLUMN_ASSESSMENT_CODE = 8;
    public static final int COLUMN_ASSESSMENT_ECTS = 9;
    public static final int COLUMN_ASSESSMENT_SWS = 10;
    public static final int COLUMN_ASSESSMENT_LVATYPE = 11;


    public ImportAssessmentWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return importAssessments();
    }

    @Override
    public void onStopped() {
        super.onStopped();

        cancelUpdateNotification();
    }

    private Result importAssessments() {
        Analytics.eventReloadAssessments(getApplicationContext());

        final Account mAccount = AppUtils.getAccount(getApplicationContext());
        if (mAccount == null) {
            return Result.success();
        }

        final ContentResolver mResolver = getApplicationContext().getContentResolver();
        if (mResolver == null) {
            return Result.failure();
        }

        final ContentProviderClient mProvider = mResolver.acquireContentProviderClient(KusssContentContract.Assessment.CONTENT_URI);

        if (mProvider == null) {
            return Result.failure();
        }

        if (getInputData().getBoolean(Consts.SYNC_SHOW_PROGRESS, false)) {
            mUpdateNotification = new SyncNotification(getApplicationContext(), R.string.notification_sync_assessment);
            mUpdateNotification.show(getApplicationContext().getString(R.string.notification_sync_assessment_loading));
        }

        AssessmentChangedNotification mChangedNotification = new AssessmentChangedNotification(getApplicationContext());

        try {
            logger.debug("setup connection");

            if (KusssHandler.getInstance().isAvailable(getApplicationContext(),
                    AppUtils.getAccountAuthToken(getApplicationContext(), mAccount),
                    AppUtils.getAccountName(mAccount),
                    AppUtils.getAccountPassword(getApplicationContext(), mAccount))) {

                updateNotification(getApplicationContext().getString(R.string.notification_sync_assessment_loading));
                logger.debug("load assessments");

                List<Assessment> assessments = KusssHandler.getInstance()
                        .getAssessments(getApplicationContext());
                if (assessments == null) {
                    return Result.retry();
                } else {
                    Map<String, Assessment> assessmentMap = new HashMap<>();
                    ArrayList<Assessment> possibleDuplicates = new ArrayList<>();

                    for (Assessment assessment : assessments) {
                        if (assessment.getAssessmentType().isDuplicatesPossible()) {
                            possibleDuplicates.add(assessment);
                        } else {
                            assessmentMap.put(KusssHelper.getAssessmentKey(assessment.getCode(), assessment.getCourseId(), assessment.getDate().getTime()), assessment);
                        }
                    }

                    logger.debug("got {} assessments", assessments.size());

                    updateNotification(getApplicationContext().getString(R.string.notification_sync_assessment_updating));

                    ArrayList<ContentProviderOperation> batch = new ArrayList<>();

                    Uri examUri = KusssContentContract.Assessment.CONTENT_URI;
                    try (Cursor c = mProvider.query(examUri, ASSESSMENT_PROJECTION, null,
                            null, null)) {
                        if (c == null) {
                            logger.warn("selection failed");
                        } else {
                            logger.debug("Found {} local entries. Computing merge solution...", c.getCount());

                            int _Id;
                            String assessmentCode;
                            String assessmentCourseId;
                            Date assessmentDate;
                            AssessmentType assessmentType;
                            Grade assessmentGrade;
                            while (c.moveToNext()) {
                                _Id = c.getInt(COLUMN_ASSESSMENT_ID);
                                assessmentCode = c.getString(COLUMN_ASSESSMENT_CODE);
                                assessmentDate = new Date(c.getLong(COLUMN_ASSESSMENT_DATE));
                                assessmentType = AssessmentType.parseAssessmentType(c
                                        .getInt(COLUMN_ASSESSMENT_TYPE));
                                assessmentGrade = Grade.parseGradeType(c
                                        .getInt(COLUMN_ASSESSMENT_GRADE));
                                assessmentCourseId = c.getString(COLUMN_ASSESSMENT_COURSEID);

                                if (assessmentType.isDuplicatesPossible()) {
                                    // delete
                                    logger.debug("delete: {}", KusssHelper.getAssessmentKey(assessmentCode, assessmentCourseId, assessmentDate.getTime()));
                                    // duplicate possible. remove existing
                                    Uri deleteUri = examUri
                                            .buildUpon()
                                            .appendPath(Integer.toString(_Id))
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
                                } else {
                                    Assessment assessment = assessmentMap.remove(KusssHelper.getAssessmentKey(assessmentCode, assessmentCourseId, assessmentDate.getTime()));
                                    if (assessment != null) {
                                        // Check to see if the entry needs to be updated
                                        Uri existingUri = examUri.buildUpon()
                                                .appendPath(Integer.toString(_Id))
                                                .build();
                                        logger.debug("Scheduling update: {}", existingUri);

                                        if (!assessmentType.equals(assessment.getAssessmentType())
                                                || !assessmentGrade.equals(assessment.getGrade())) {
                                            mChangedNotification
                                                    .addUpdate(String.format("%s: %s",
                                                            assessment.getTitle(),
                                                            getApplicationContext().getString(assessment
                                                                    .getGrade()
                                                                    .getStringResID())));
                                        }

                                        batch.add(ContentProviderOperation
                                                .newUpdate(
                                                        KusssContentContract
                                                                .asEventSyncAdapter(
                                                                        existingUri,
                                                                        mAccount.name,
                                                                        mAccount.type))
                                                .withValue(
                                                        KusssContentContract.Assessment.COL_ID,
                                                        Integer.toString(_Id))
                                                .withValues(KusssHelper.getAssessmentContentValues(assessment))
                                                .build());
                                    }
                                }
                            }

                            for (Assessment assessment : assessmentMap.values()) {
                                batch.add(ContentProviderOperation
                                        .newInsert(
                                                KusssContentContract
                                                        .asEventSyncAdapter(
                                                                examUri,
                                                                mAccount.name,
                                                                mAccount.type))
                                        .withValues(KusssHelper.getAssessmentContentValues(assessment))
                                        .build());
                                logger.debug("Scheduling insert: {} {}", assessment.getTerm(), assessment.getCourseId());

                                mChangedNotification.addInsert(String.format(
                                        "%s: %s", assessment.getTitle(), getApplicationContext()
                                                .getString(assessment.getGrade()
                                                        .getStringResID())));
                            }
                            for (Assessment assessment : possibleDuplicates) {
                                batch.add(ContentProviderOperation
                                        .newInsert(
                                                KusssContentContract
                                                        .asEventSyncAdapter(
                                                                examUri,
                                                                mAccount.name,
                                                                mAccount.type))
                                        .withValues(KusssHelper.getAssessmentContentValues(assessment))
                                        .build());
                                logger.debug("Scheduling insert: {} {}", assessment.getTerm(), assessment.getCourseId());
                            }

                            updateNotification(getApplicationContext().getString(R.string.notification_sync_assessment_saving));

                            if (batch.size() > 0) {
                                logger.debug("Applying batch update");
                                mProvider.applyBatch(batch);
                                logger.debug("Notify resolver");
                                mResolver
                                        .notifyChange(
                                                KusssContentContract.Assessment.CONTENT_CHANGED_URI,
                                                null, // No
                                                // local
                                                // observer
                                                false); // IMPORTANT: Do not sync to
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

}
