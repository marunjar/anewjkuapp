package org.voidsink.anewjkuapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.kusss.LVA;
import org.voidsink.anewjkuapp.notification.SyncNotification;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class ImportLvaTask extends AsyncTask<Void, Void, Void> {

	private static final String TAG = ImportLvaTask.class.getSimpleName();

	private static final Object sync_lock = new Object();

	private boolean importDone = false;

	private ContentProviderClient mProvider;
	private Account mAccount;
	private SyncResult mSyncResult;
	private Context mContext;
	private ContentResolver mResolver;

	public static final String[] LVA_PROJECTION = new String[] {
			KusssContentContract.Lva.LVA_COL_ID,
			KusssContentContract.Lva.LVA_COL_TERM,
			KusssContentContract.Lva.LVA_COL_LVANR,
			KusssContentContract.Lva.LVA_COL_TITLE,
			KusssContentContract.Lva.LVA_COL_SKZ,
			KusssContentContract.Lva.LVA_COL_TYPE,
			KusssContentContract.Lva.LVA_COL_TEACHER};

	public static final int COLUMN_LVA_ID = 0;
	public static final int COLUMN_LVA_TERM = 1;
	public static final int COLUMN_LVA_LVANR = 2;
	public static final int COLUMN_LVA_TITLE = 3;
	public static final int COLUMN_LVA_SKZ = 4;
	public static final int COLUMN_LVA_TYPE = 5;
	public static final int COLUMN_LVA_TEACHER = 6;

	public ImportLvaTask(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult,
			Context context) {
		this.mAccount = account;
		this.mProvider = provider;
		this.mSyncResult = syncResult;
		this.mResolver = context.getContentResolver();
		this.mContext = context;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.d(TAG, "Start importing LVA");

		SyncNotification mSyncNotification = new SyncNotification(mContext, R.string.notification_sync_lva);
		mSyncNotification.show("LVAs werden geladen");

		synchronized (sync_lock) {
			mSyncNotification.update("");
			mSyncNotification.update("LVAs werden geladen");

			try {
				Log.d(TAG, "setup connection");

				if (KusssHandler.handler
						.isAvailable(
								this.mAccount.name,
								AccountManager.get(mContext).getPassword(
										this.mAccount))) {
					Log.d(TAG, "load lvas");

					List<LVA> lvas = KusssHandler.handler.getLvas();
					if (lvas == null) {
						mSyncResult.stats.numParseExceptions++;
					}
					Map<String, LVA> lvaMap = new HashMap<String, LVA>();
					for (LVA lva : lvas) {
						lvaMap.put(
								String.format("%s-%d", lva.getTerm(),
										lva.getLvaNr()), lva);
					}

					Log.d(TAG, String.format("got %s lvas", lvas.size()));

					mSyncNotification.update("LVAs werden aktualisiert");

					ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

					Uri lvaUri = KusssContentContract.Lva.CONTENT_URI;
					Cursor c = mProvider.query(lvaUri, LVA_PROJECTION, null,
							null, null);

					if (c != null) {
						Log.d(TAG, "Found " + c.getCount()
								+ " local entries. Computing merge solution...");
					} else {
						Log.w(TAG, "selection failed");
					}

					int lvaId;
					String lvaTerm;
					int lvaNr;

					while (c.moveToNext()) {
						lvaId = c.getInt(COLUMN_LVA_ID);
						lvaTerm = c.getString(COLUMN_LVA_TERM);
						lvaNr = c.getInt(COLUMN_LVA_LVANR);

						LVA lva = lvaMap.get(String.format("%s-%d", lvaTerm,
								lvaNr));
						if (lva != null) {
							lvaMap.remove(String
									.format("%s-%d", lvaTerm, lvaNr));
							// Check to see if the entry needs to be updated
							Uri existingUri = lvaUri.buildUpon()
									.appendPath(Integer.toString(lvaId))
									.build();
							Log.d(TAG, "Scheduling update: " + existingUri);

							batch.add(ContentProviderOperation
									.newUpdate(
											KusssContentContract
													.asEventSyncAdapter(
															existingUri,
															mAccount.name,
															mAccount.type))
									.withValue(
											KusssContentContract.Lva.LVA_COL_ID,
											Integer.toString(lvaId))
									.withValues(lva.getContentValues()).build());
							mSyncResult.stats.numUpdates++;
						}
					}
					c.close();

					for (LVA lva : lvaMap.values()) {
						batch.add(ContentProviderOperation
								.newInsert(
										KusssContentContract
												.asEventSyncAdapter(lvaUri,
														mAccount.name,
														mAccount.type))
								.withValues(lva.getContentValues()).build());
						Log.d(TAG, "Scheduling insert: " + lva.getTerm() + " "
								+ lva.getLvaNr());
						mSyncResult.stats.numInserts++;
					}

					if (batch.size() > 0) {
						mSyncNotification.update("LVAs werden gespeichert");

						Log.d(TAG, "Applying batch update");
						mProvider.applyBatch(batch);
						Log.d(TAG, "Notify resolver");
						mResolver.notifyChange(
								KusssContentContract.Lva.CONTENT_URI, null, // No
																			// local
																			// observer
								false); // IMPORTANT: Do not sync to network
					} else {
						Log.w(TAG, "No batch operations found! Do nothing");
					}
				} else {
					mSyncResult.stats.numAuthExceptions++;
				}
			} catch (Exception e) {
				Log.e(TAG, "import failed: " + e);
			} finally {
				mSyncNotification.cancel();
				importDone = true;
			}
		}

		return null;
	}

	public boolean isDone() {
		return importDone;
	}

}
