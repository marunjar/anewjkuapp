/*******************************************************************************
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
 ******************************************************************************/

package org.voidsink.anewjkuapp;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.notification.KusssNotificationBuilder;
import org.voidsink.anewjkuapp.update.ImportAssessmentTask;
import org.voidsink.anewjkuapp.update.ImportCourseTask;
import org.voidsink.anewjkuapp.update.ImportCurriculaTask;
import org.voidsink.anewjkuapp.update.ImportExamTask;
import org.voidsink.anewjkuapp.utils.AppUtils;

public class KusssSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = KusssSyncAdapter.class.getSimpleName();

    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;
    private boolean mSyncCancled;
    private Context mContext;

    /**
     * Set up the sync adapter
     */
    public KusssSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it from the
		 * incoming Context
		 */
        this.mSyncCancled = false;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the constructor maintains
     * compatibility with Android 3.0 and later platform versions
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public KusssSyncAdapter(Context context, boolean autoInitialize,
                            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
		/*
		 * If your app uses a content resolver, get an instance of it from the
		 * incoming Context
		 */
        this.mSyncCancled = false;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        if (account == null || account.name == null) {
            KusssNotificationBuilder.showErrorNotification(mContext,
                    R.string.notification_error_account_is_null, null);
            syncResult.stats.numAuthExceptions++;
            return;
        }

        Log.d(TAG, "starting sync of account: " + account.name);

        if (!KusssHandler.getInstance().isAvailable(mContext,
                AppUtils.getAccountAuthToken(mContext, account),
                AppUtils.getAccountName(mContext, account),
                AppUtils.getAccountPassword(mContext, account))) {
            KusssNotificationBuilder.showErrorNotification(mContext,
                    R.string.notification_error_account_not_available, null);
            syncResult.stats.numAuthExceptions++;
            return;
        }

        // TODO Download data here
        try {
            Looper.prepare();

            Log.d(TAG, "importing curricula");

            ImportCurriculaTask curriculaTask = new ImportCurriculaTask(account, extras,
                    authority, provider, syncResult, mContext);
            curriculaTask.execute();
            while (!curriculaTask.isDone() && !mSyncCancled) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    Analytics.sendException(mContext, e, false);
                }
                if (mSyncCancled) {
                    curriculaTask.cancel(true);
                }
            }

            Log.d(TAG, "importing courses");

            ImportCourseTask courseTask = new ImportCourseTask(account, extras,
                    authority, provider, syncResult, mContext);
            courseTask.execute();
            while (!courseTask.isDone() && !mSyncCancled) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    Analytics.sendException(mContext, e, false);
                }
                if (mSyncCancled) {
                    courseTask.cancel(true);
                }
            }

            Log.d(TAG, "importing Grades");

            ImportAssessmentTask gradeTask = new ImportAssessmentTask(account, extras,
                    authority, provider, syncResult, mContext);

            gradeTask.execute();
            while (!gradeTask.isDone() && !mSyncCancled) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    Analytics.sendException(mContext, e, false);
                }
                if (mSyncCancled) {
                    gradeTask.cancel(true);
                }
            }

            Log.d(TAG, "importing Exams");

            ImportExamTask examTask = new ImportExamTask(account, extras,
                    authority, provider, syncResult, mContext);

            examTask.execute();
            while (!examTask.isDone() && !mSyncCancled) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    Analytics.sendException(mContext, e, false);
                }
                if (mSyncCancled) {
                    examTask.cancel(true);
                }
            }

            Log.d(TAG, "importing finished");
        } catch (Exception e) {
            Analytics.sendException(mContext, e, true);
            KusssNotificationBuilder.showErrorNotification(mContext,
                    R.string.notification_error, e);
        } finally {
            KusssHandler.getInstance().logout(mContext);
        }
    }

    @Override
    public void onSyncCanceled() {
        Log.d(TAG, "Canceled Sync");
        mSyncCancled = true;
    }

}
