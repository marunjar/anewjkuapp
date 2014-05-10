package org.voidsink.anewjkuapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.voidsink.anewjkuapp.base.BaseAsyncTask;
import org.voidsink.anewjkuapp.kusss.Exam;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.notification.NewExamNotification;
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

public class ImportExamTask extends BaseAsyncTask<Void, Void, Void> {

	private static final String TAG = ImportLvaTask.class.getSimpleName();
	private static final Object sync_lock = new Object();
	private static final DateFormat df = SimpleDateFormat.getDateInstance();

	private ContentProviderClient mProvider;
	private Account mAccount;
	private SyncResult mSyncResult;
	private Context mContext;
	private ContentResolver mResolver;

	public static final String[] EXAM_PROJECTION = new String[] {
			KusssContentContract.Exam.EXAM_COL_ID,
			KusssContentContract.Exam.EXAM_COL_TERM,
			KusssContentContract.Exam.EXAM_COL_LVANR,
			KusssContentContract.Exam.EXAM_COL_DATE,
			KusssContentContract.Exam.EXAM_COL_TIME,
			KusssContentContract.Exam.EXAM_COL_LOCATION,
			KusssContentContract.Exam.EXAM_COL_DESCRIPTION,
			KusssContentContract.Exam.EXAM_COL_INFO };

	public static final int COLUMN_EXAM_ID = 0;
	public static final int COLUMN_EXAM_TERM = 1;
	public static final int COLUMN_EXAM_LVANR = 2;
	public static final int COLUMN_EXAM_DATE = 3;
	public static final int COLUMN_EXAM_TIME = 4;
	public static final int COLUMN_EXAM_LOCATION = 5;
	public static final int COLUMN_EXAM_DESCRIPTION = 6;
	public static final int COLUMN_EXAM_INFO = 7;

	public ImportExamTask(Account account, Context context) {
		this(account, null, null, null, null, context);
		this.mProvider = context.getContentResolver().acquireContentProviderClient(KusssContentContract.Exam.CONTENT_URI);
		this.mSyncResult = new SyncResult();
	}
	
	public ImportExamTask(Account account, Bundle extras, String authority,
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
		Log.d(TAG, "Start importing exams");

		NewExamNotification mNewExamNotification = new NewExamNotification(
				mContext);
		SyncNotification mSyncNotification = new SyncNotification(mContext,
				R.string.notification_sync_exam);
		mSyncNotification.show("Prüfungen werden geladen");

		synchronized (sync_lock) {
			mSyncNotification.update("");
			mSyncNotification.update("Prüfungen werden geladen");

			try {
				Log.d(TAG, "setup connection");

				if (KusssHandler.handler
						.isAvailable(
								this.mAccount.name,
								AccountManager.get(mContext).getPassword(
										this.mAccount))) {

					List<Exam> exams = null;
					if (PreferenceWrapper.getNewExamsByLvaNr(mContext)) {
						LvaMap lvaMap = new LvaMap(mContext);
						
						Log.d(TAG, "load exams by lvanr");
						exams = KusssHandler.handler
								.getNewExamsByLvaNr(lvaMap.getLVAs());
					} else {
						Log.d(TAG, "load exams");
						exams = KusssHandler.handler.getNewExams();
					}
					if (exams == null) {
						mSyncResult.stats.numParseExceptions++;
					}
					Map<String, Exam> examMap = new HashMap<String, Exam>();
					for (Exam exam : exams) {
						examMap.put(
								String.format("%s-%d", exam.getTerm(),
										exam.getLvaNr()), exam);
					}

					Log.d(TAG, String.format("got %s exams", exams.size()));

					mSyncNotification.update("Prüfungen werden aktualisiert");

					ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

					Uri examUri = KusssContentContract.Exam.CONTENT_URI;
					Cursor c = mProvider.query(examUri, EXAM_PROJECTION, null,
							null, null);

					if (c == null) {
						Log.w(TAG, "selection failed");
					} else {
						Log.d(TAG, "Found " + c.getCount()
								+ " local entries. Computing merge solution...");
						int examId;
						String examTerm;
						int examLvaNr;
						long examDate;
						String examTime;
						String examLocation;
						// delete exams one day after exam
						long validUntil = System.currentTimeMillis()
								+ DateUtils.MILLIS_PER_DAY;
	
						while (c.moveToNext()) {
							examId = c.getInt(COLUMN_EXAM_ID);
							examTerm = c.getString(COLUMN_EXAM_TERM);
							examLvaNr = c.getInt(COLUMN_EXAM_LVANR);
							examDate = c.getLong(COLUMN_EXAM_DATE);
							examTime = c.getString(COLUMN_EXAM_TIME);
							examLocation = c.getString(COLUMN_EXAM_LOCATION);
	
							Exam exam = examMap.get(String.format("%s-%d",
									examTerm, examLvaNr));
							if (exam != null) {
								examMap.remove(String.format("%s-%d", examTerm,
										examLvaNr));
								// Check to see if the entry needs to be updated
								Uri existingUri = examUri.buildUpon()
										.appendPath(Integer.toString(examId))
										.build();
								Log.d(TAG, "Scheduling update: " + existingUri);
	
								if (!DateUtils.isSameDay(new Date(examDate),
										exam.getDate())
										|| !examTime.equals(exam.getTime())
										|| !examLocation.equals(exam.getLocation())) {
									mNewExamNotification.addUpdate(String.format(
											"%s: %s, %s, %s",
											df.format(exam.getDate()),
											exam.getTitle(), exam.getTime(),
											exam.getLocation()));
								}
	
								batch.add(ContentProviderOperation
										.newUpdate(
												KusssContentContract
														.asEventSyncAdapter(
																existingUri,
																mAccount.name,
																mAccount.type))
										.withValue(
												KusssContentContract.Exam.EXAM_COL_ID,
												Integer.toString(examId))
										.withValues(exam.getContentValues())
										.build());
								mSyncResult.stats.numUpdates++;
							} else if (examDate < validUntil) {
								// Entry doesn't exist. Remove only newer
								// events from the database.
								Uri deleteUri = examUri.buildUpon()
										.appendPath(Integer.toString(examId))
										.build();
								Log.d(TAG, "Scheduling delete: " + deleteUri);
	
								batch.add(ContentProviderOperation.newDelete(
										KusssContentContract.asEventSyncAdapter(
												deleteUri, mAccount.name,
												mAccount.type)).build());
								mSyncResult.stats.numDeletes++;
							}
						}
						c.close();
	
						for (Exam exam : examMap.values()) {
							batch.add(ContentProviderOperation
									.newInsert(
											KusssContentContract
													.asEventSyncAdapter(examUri,
															mAccount.name,
															mAccount.type))
									.withValues(exam.getContentValues()).build());
							Log.d(TAG, "Scheduling insert: " + exam.getTerm() + " "
									+ exam.getLvaNr());
	
							mNewExamNotification.addUpdate(String.format(
									"%s: %s, %s, %s", df.format(exam.getDate()),
									exam.getTitle(), exam.getTime(),
									exam.getLocation()));
	
							mSyncResult.stats.numInserts++;
						}
	
						if (batch.size() > 0) {
							mSyncNotification
									.update("Prüfungen werden gespeichert");
	
							Log.d(TAG, "Applying batch update");
							mProvider.applyBatch(batch);
							Log.d(TAG, "Notify resolver");
							mResolver.notifyChange(
									KusssContentContract.Exam.CONTENT_URI, null, // No
																					// local
																					// observer
									false); // IMPORTANT: Do not sync to network
						} else {
							Log.w(TAG, "No batch operations found! Do nothing");
						}
					}
				} else {
					mSyncResult.stats.numAuthExceptions++;
				}

			} catch (Exception e) {
				Log.e(TAG, "import failed: " + e);
			} finally {
				mSyncNotification.cancel();
				mNewExamNotification.show();
				setImportDone(true);
			}
		}

		return null;
	}

}
