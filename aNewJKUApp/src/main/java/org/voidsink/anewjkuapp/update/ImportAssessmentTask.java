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

package org.voidsink.anewjkuapp.update;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.AssessmentType;
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
import java.util.concurrent.Callable;

public class ImportAssessmentTask implements Callable<Void> {

    private static final String TAG = ImportCourseTask.class.getSimpleName();

    private ContentProviderClient mProvider;
    private Account mAccount;
    private SyncResult mSyncResult;
    private Context mContext;
    private ContentResolver mResolver;

    private boolean mShowProgress;
    private SyncNotification mUpdateNotification;
    private AssessmentChangedNotification mAssessmentChangeNotification;

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
            KusssContentContract.Assessment.COL_SWS};

    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ASSESSMENT_ID = 0;
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

    public ImportAssessmentTask(Account account, Context context) {
        this(account, null, null, null, null, context);
        this.mProvider = context.getContentResolver()
                .acquireContentProviderClient(
                        KusssContentContract.Exam.CONTENT_URI);
        this.mSyncResult = new SyncResult();
        this.mShowProgress = true;
    }

    public ImportAssessmentTask(Account account, Bundle extras, String authority,
                                ContentProviderClient provider, SyncResult syncResult,
                                Context context) {
        this.mAccount = account;
        this.mProvider = provider;
        this.mSyncResult = syncResult;
        this.mResolver = context.getContentResolver();
        this.mContext = context;
        this.mShowProgress = (extras != null && extras.getBoolean(Consts.SYNC_SHOW_PROGRESS, false));
    }

    private void updateNotify(String string) {
        if (mUpdateNotification != null) {
            mUpdateNotification.update(string);
        }
    }

    @Override
    public Void call() throws Exception {
        if (mShowProgress) {
            mUpdateNotification = new SyncNotification(mContext,
                    R.string.notification_sync_assessment);
            mUpdateNotification.show(mContext.getString(R.string.notification_sync_assessment_loading));
        }
        mAssessmentChangeNotification = new AssessmentChangedNotification(mContext);

        updateNotify(mContext.getString(R.string.notification_sync_connect));

        try {
            Log.d(TAG, "setup connection");

            if (KusssHandler.getInstance().isAvailable(mContext,
                    AppUtils.getAccountAuthToken(mContext, mAccount),
                    AppUtils.getAccountName(mContext, mAccount),
                    AppUtils.getAccountPassword(mContext, mAccount))) {

                updateNotify(mContext.getString(R.string.notification_sync_assessment_loading));
                Log.d(TAG, "load assessments");

                List<Assessment> assessments = KusssHandler.getInstance()
                        .getAssessments(mContext);
                if (assessments == null) {
                    mSyncResult.stats.numParseExceptions++;
                } else {
                    Map<String, Assessment> assessmentMap = new HashMap<>();
                    for (Assessment assessment : assessments) {
                        assessmentMap.put(KusssHelper.getAssessmentKey(assessment.getCode(), assessment.getCourseId(), assessment.getDate().getTime()), assessment);
                    }

                    Log.d(TAG, String.format("got %s assessments", assessments.size()));

                    updateNotify(mContext.getString(R.string.notification_sync_assessment_updating));

                    ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

                    Uri examUri = KusssContentContract.Assessment.CONTENT_URI;
                    Cursor c = mProvider.query(examUri, ASSESSMENT_PROJECTION, null,
                            null, null);

                    if (c == null) {
                        Log.w(TAG, "selection failed");
                    } else {
                        Log.d(TAG, "Found " + c.getCount()
                                + " local entries. Computing merge solution...");

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

                            Assessment assessment = assessmentMap.remove(KusssHelper.getAssessmentKey(assessmentCode, assessmentCourseId, assessmentDate.getTime()));
                            if (assessment != null) {
                                // Check to see if the entry needs to be updated
                                Uri existingUri = examUri.buildUpon()
                                        .appendPath(Integer.toString(_Id))
                                        .build();
                                Log.d(TAG, "Scheduling update: " + existingUri);

                                if (!assessmentType.equals(assessment.getAssessmentType())
                                        || !assessmentGrade.equals(assessment.getGrade())) {
                                    mAssessmentChangeNotification
                                            .addUpdate(String.format("%s: %s",
                                                    assessment.getTitle(),
                                                    mContext.getString(assessment
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
                                mSyncResult.stats.numUpdates++;
                            }
                        }
                        c.close();

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
                            Log.d(TAG, "Scheduling insert: " + assessment.getTerm()
                                    + " " + assessment.getCourseId());

                            mAssessmentChangeNotification.addInsert(String.format(
                                    "%s: %s", assessment.getTitle(), mContext
                                            .getString(assessment.getGrade()
                                                    .getStringResID())));

                            mSyncResult.stats.numInserts++;
                        }

                        if (batch.size() > 0) {
                            updateNotify(mContext.getString(R.string.notification_sync_assessment_saving));

                            Log.d(TAG, "Applying batch update");
                            mProvider.applyBatch(batch);
                            Log.d(TAG, "Notify resolver");
                            mResolver
                                    .notifyChange(
                                            KusssContentContract.Assessment.CONTENT_CHANGED_URI,
                                            null, // No
                                            // local
                                            // observer
                                            false); // IMPORTANT: Do not sync to
                            // network
                        } else {
                            Log.w(TAG, "No batch operations found! Do nothing");
                        }
                    }
                }
                KusssHandler.getInstance().logout(mContext);
            } else {
                mSyncResult.stats.numAuthExceptions++;
            }
        } catch (Exception e) {
            Analytics.sendException(mContext, e, true);
            Log.e(TAG, "import failed", e);
        }

        if (mUpdateNotification != null) {
            mUpdateNotification.cancel();
        }
        mAssessmentChangeNotification.show();

        return null;
    }
}
