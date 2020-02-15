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
import androidx.work.WorkerParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;
import org.voidsink.anewjkuapp.base.BaseWorker;
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.AssessmentType;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.notification.AssessmentChangedNotification;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportAssessmentWorker extends BaseWorker {

    private static final Logger logger = LoggerFactory.getLogger(ImportAssessmentWorker.class);

    public ImportAssessmentWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return importAssessments();
    }

    private Result importAssessments() {
        AnalyticsHelper.eventReloadAssessments(getApplicationContext());

        final Account mAccount = AppUtils.getAccount(getApplicationContext());
        if (mAccount == null) {
            return getSuccess();
        }

        final ContentResolver mResolver = getApplicationContext().getContentResolver();
        if (mResolver == null) {
            return getFailure();
        }

        final ContentProviderClient mProvider = mResolver.acquireContentProviderClient(KusssContentContract.Assessment.CONTENT_URI);

        if (mProvider == null) {
            return getFailure();
        }

        showUpdateNotification(R.string.notification_sync_assessment, R.string.notification_sync_assessment_loading);

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
                    return getRetry();
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
                    try (Cursor c = mProvider.query(examUri, KusssContentContract.Assessment.DB.PROJECTION, null,
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
                                _Id = c.getInt(KusssContentContract.Assessment.DB.COL_ID);
                                assessmentCode = c.getString(KusssContentContract.Assessment.DB.COL_CODE);
                                assessmentDate = new Date(c.getLong(KusssContentContract.Assessment.DB.COL_DATE));
                                assessmentType = AssessmentType.parseAssessmentType(c
                                        .getInt(KusssContentContract.Assessment.DB.COL_TYPE));
                                assessmentGrade = Grade.parseGradeType(c
                                        .getInt(KusssContentContract.Assessment.DB.COL_GRADE));
                                assessmentCourseId = c.getString(KusssContentContract.Assessment.DB.COL_COURSEID);

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
                return getRetry();
            }

            mChangedNotification.show();
            return getSuccess();
        } catch (Exception e) {
            AnalyticsHelper.sendException(getApplicationContext(), e, true);

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
}
