package org.voidsink.anewjkuapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.voidsink.anewjkuapp.base.BaseAsyncTask;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.kusss.Lva;
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
import android.os.Bundle;
import android.util.Log;

public class ImportLvaTask extends BaseAsyncTask<Void, Void, Void> {

	private static final String TAG = ImportLvaTask.class.getSimpleName();

	private static final Object sync_lock = new Object();

	private ContentProviderClient mProvider;
	private Account mAccount;
	private SyncResult mSyncResult;
	private Context mContext;
	private ContentResolver mResolver;

	private boolean isSync;
	private SyncNotification mUpdateNotification;

	public static final String[] LVA_PROJECTION = new String[] {
			KusssContentContract.Lva.LVA_COL_ID,
			KusssContentContract.Lva.LVA_COL_TERM,
			KusssContentContract.Lva.LVA_COL_LVANR,
			KusssContentContract.Lva.LVA_COL_TITLE,
			KusssContentContract.Lva.LVA_COL_SKZ,
			KusssContentContract.Lva.LVA_COL_TYPE,
			KusssContentContract.Lva.LVA_COL_TEACHER,
			KusssContentContract.Lva.LVA_COL_SWS,
			KusssContentContract.Lva.LVA_COL_ECTS,
			KusssContentContract.Lva.LVA_COL_CODE };

	public static final int COLUMN_LVA_ID = 0;
	public static final int COLUMN_LVA_TERM = 1;
	public static final int COLUMN_LVA_LVANR = 2;
	public static final int COLUMN_LVA_TITLE = 3;
	public static final int COLUMN_LVA_SKZ = 4;
	public static final int COLUMN_LVA_TYPE = 5;
	public static final int COLUMN_LVA_TEACHER = 6;
	public static final int COLUMN_LVA_SWS = 7;
	public static final int COLUMN_LVA_ECTS = 8;
	public static final int COLUMN_LVA_CODE = 9;

	public ImportLvaTask(Account account, Context context) {
		this(account, null, null, null, null, context);
		this.mProvider = context.getContentResolver()
				.acquireContentProviderClient(
						KusssContentContract.Lva.CONTENT_URI);
		this.mSyncResult = new SyncResult();
		this.isSync = false;
	}

	public ImportLvaTask(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult,
			Context context) {
		this.mAccount = account;
		this.mProvider = provider;
		this.mSyncResult = syncResult;
		this.mResolver = context.getContentResolver();
		this.mContext = context;
		this.isSync = true;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		Log.d(TAG, "prepare importing LVA");

		if (!isSync) {
			mUpdateNotification = new SyncNotification(mContext,
					R.string.notification_sync_lva);
			mUpdateNotification.show("LVAs werden geladen");
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.d(TAG, "Start importing LVA");

		synchronized (sync_lock) {
			udpateNotify("LVAs werden geladen");

			try {
				Log.d(TAG, "setup connection");

				if (KusssHandler.getInstance().isAvailable(
						AppUtils.getAccountAuthToken(mContext, mAccount),
						AppUtils.getAccountName(mContext, mAccount),
						AppUtils.getAccountPassword(mContext, mAccount))) {

					Log.d(TAG, "load lvas");

					List<Lva> lvas = KusssHandler.getInstance().getLvas();
					if (lvas == null) {
						mSyncResult.stats.numParseExceptions++;
					} else {
						Map<String, Lva> lvaMap = new HashMap<String, Lva>();
						for (Lva lva : lvas) {
							lvaMap.put(lva.getKey(), lva);
						}

						Log.d(TAG, String.format("got %s lvas", lvas.size()));

						udpateNotify("LVAs werden aktualisiert");

						ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

						Uri lvaUri = KusssContentContract.Lva.CONTENT_URI;
						Cursor c = mProvider.query(lvaUri, LVA_PROJECTION,
								null, null, null);

						if (c == null) {
							Log.w(TAG, "selection failed");
						} else {
							Log.d(TAG,
									"Found "
											+ c.getCount()
											+ " local entries. Computing merge solution...");

							int lvaId;
							String lvaTerm;
							int lvaNr;

							while (c.moveToNext()) {
								lvaId = c.getInt(COLUMN_LVA_ID);
								lvaTerm = c.getString(COLUMN_LVA_TERM);
								lvaNr = c.getInt(COLUMN_LVA_LVANR);

								Lva lva = lvaMap
										.get(Lva.getKey(lvaTerm, lvaNr));
								if (lva != null) {
									lvaMap.remove(Lva.getKey(lvaTerm, lvaNr));
									// Check to see if the entry needs to be
									// updated
									Uri existingUri = lvaUri
											.buildUpon()
											.appendPath(Integer.toString(lvaId))
											.build();
									Log.d(TAG, "Scheduling update: "
											+ existingUri);

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
											.withValues(lva.getContentValues())
											.build());
									mSyncResult.stats.numUpdates++;
								} else {
									// delete
									Log.d(TAG,
											"delete: "
													+ Lva.getKey(lvaTerm, lvaNr));
									// Entry doesn't exist. Remove only
									// newer
									// events from the database.
									Uri deleteUri = lvaUri
											.buildUpon()
											.appendPath(Integer.toString(lvaId))
											.build();
									Log.d(TAG, "Scheduling delete: "
											+ deleteUri);

									batch.add(ContentProviderOperation
											.newDelete(
													KusssContentContract
															.asEventSyncAdapter(
																	deleteUri,
																	mAccount.name,
																	mAccount.type))
											.build());
									mSyncResult.stats.numDeletes++;
								}
							}
							c.close();

							for (Lva lva : lvaMap.values()) {
								batch.add(ContentProviderOperation
										.newInsert(
												KusssContentContract
														.asEventSyncAdapter(
																lvaUri,
																mAccount.name,
																mAccount.type))
										.withValues(lva.getContentValues())
										.build());
								Log.d(TAG,
										"Scheduling insert: " + lva.getTerm()
												+ " " + lva.getLvaNr());
								mSyncResult.stats.numInserts++;
							}

							if (batch.size() > 0) {
								udpateNotify("LVAs werden gespeichert");

								Log.d(TAG, "Applying batch update");
								mProvider.applyBatch(batch);
								Log.d(TAG, "Notify resolver");
								mResolver
										.notifyChange(
												KusssContentContract.Lva.CONTENT_CHANGED_URI,
												null, // No
												// local
												// observer
												false); // IMPORTANT: Do not
														// sync to
														// network
							} else {
								Log.w(TAG,
										"No batch operations found! Do nothing");
							}
						}
					}
				} else {
					mSyncResult.stats.numAuthExceptions++;
				}
			} catch (Exception e) {
				Log.e(TAG, "import failed", e);
			}
		}

		setImportDone();
		
		if (mUpdateNotification != null) {
			mUpdateNotification.cancel();
		}
		
		return null;
	}

	private void udpateNotify(String string) {
		if (mUpdateNotification != null) {
			mUpdateNotification.update(string);
		}
	}
}
