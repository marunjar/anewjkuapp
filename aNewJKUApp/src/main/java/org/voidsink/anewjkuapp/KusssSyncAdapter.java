package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.notification.KusssNotificationBuilder;
import org.voidsink.anewjkuapp.update.ImportExamTask;
import org.voidsink.anewjkuapp.update.ImportGradeTask;
import org.voidsink.anewjkuapp.update.ImportLvaTask;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.AppUtils;

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

            Log.d(TAG, "importing Studies");

            ImportStudiesTask studiesTask = new ImportStudiesTask(account, extras,
                    authority, provider, syncResult, mContext);
            studiesTask.execute();
            while (!studiesTask.isDone() && !mSyncCancled) {
                try {
                    Thread.sleep(600);
                } catch (Exception e) {
                    Analytics.sendException(mContext, e, false);
                }
                if (mSyncCancled) {
                    studiesTask.cancel(true);
                }
            }

			Log.d(TAG, "importing LVAs");

			ImportLvaTask lvaTask = new ImportLvaTask(account, extras,
					authority, provider, syncResult, mContext);
			lvaTask.execute();
			while (!lvaTask.isDone() && !mSyncCancled) {
				try {
					Thread.sleep(600);
				} catch (Exception e) {
                    Analytics.sendException(mContext, e, false);
				}
				if (mSyncCancled) {
					lvaTask.cancel(true);
				}
			}

            Log.d(TAG, "importing Grades");

            ImportGradeTask gradeTask = new ImportGradeTask(account, extras,
                    authority, provider, syncResult, mContext);

            gradeTask.execute();
            while (!gradeTask.isDone() && !mSyncCancled) {
                try {
                    Thread.sleep(600);
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
					Thread.sleep(600);
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
			// mNotification.show();
		}
	}

	@Override
	public void onSyncCanceled() {
		Log.d(TAG, "Canceled Sync");
		mSyncCancled = true;
	}

}
