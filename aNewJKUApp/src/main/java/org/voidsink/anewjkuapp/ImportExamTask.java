package org.voidsink.anewjkuapp;

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

import org.apache.commons.lang.time.DateUtils;
import org.voidsink.anewjkuapp.base.BaseAsyncTask;
import org.voidsink.anewjkuapp.kusss.Exam;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.notification.NewExamNotification;
import org.voidsink.anewjkuapp.notification.SyncNotification;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportExamTask extends BaseAsyncTask<Void, Void, Void> {

    private static final String TAG = ImportExamTask.class.getSimpleName();
    private static final Object sync_lock = new Object();

    private ContentProviderClient mProvider;
    private Account mAccount;
    private SyncResult mSyncResult;
    private Context mContext;
    private ContentResolver mResolver;

    private boolean isSync;
    private SyncNotification mUpdateNotification;
    private NewExamNotification mNewExamNotification;

    public static final String[] EXAM_PROJECTION = new String[]{
            KusssContentContract.Exam.EXAM_COL_ID,
            KusssContentContract.Exam.EXAM_COL_TERM,
            KusssContentContract.Exam.EXAM_COL_LVANR,
            KusssContentContract.Exam.EXAM_COL_DATE,
            KusssContentContract.Exam.EXAM_COL_TIME,
            KusssContentContract.Exam.EXAM_COL_LOCATION,
            KusssContentContract.Exam.EXAM_COL_DESCRIPTION,
            KusssContentContract.Exam.EXAM_COL_INFO,
            KusssContentContract.Exam.EXAM_COL_IS_REGISTERED,
            KusssContentContract.Exam.EXAM_COL_TITLE};

    public static final int COLUMN_EXAM_ID = 0;
    public static final int COLUMN_EXAM_TERM = 1;
    public static final int COLUMN_EXAM_LVANR = 2;
    public static final int COLUMN_EXAM_DATE = 3;
    public static final int COLUMN_EXAM_TIME = 4;
    public static final int COLUMN_EXAM_LOCATION = 5;
    public static final int COLUMN_EXAM_DESCRIPTION = 6;
    public static final int COLUMN_EXAM_INFO = 7;
    public static final int COLUMN_EXAM_IS_REGISTERED = 8;
    public static final int COLUMN_EXAM_TITLE = 9;

    public ImportExamTask(Account account, Context context) {
        this(account, null, null, null, null, context);
        this.mProvider = context.getContentResolver()
                .acquireContentProviderClient(
                        KusssContentContract.Exam.CONTENT_URI);
        this.mSyncResult = new SyncResult();
        this.isSync = false;
    }

    public ImportExamTask(Account account, Bundle extras, String authority,
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

        Log.d(TAG, "prepare importing exams");

        if (!isSync) {
            mUpdateNotification = new SyncNotification(mContext,
                    R.string.notification_sync_exam);
            mUpdateNotification.show("Prüfungen werden geladen");
        }
        mNewExamNotification = new NewExamNotification(mContext);
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "Start importing exams");

        synchronized (sync_lock) {
            updateNotify("Prüfungen werden geladen");

            final DateFormat df = DateFormat.getDateInstance();

            try {
                Log.d(TAG, "setup connection");

                if (KusssHandler.getInstance().isAvailable(mContext,
                        AppUtils.getAccountAuthToken(mContext, mAccount),
                        AppUtils.getAccountName(mContext, mAccount),
                        AppUtils.getAccountPassword(mContext, mAccount))) {

                    List<Exam> exams;
                    if (PreferenceWrapper.getNewExamsByLvaNr(mContext)) {
                        LvaMap lvaMap = new LvaMap(mContext);

                        Log.d(TAG, "load exams by lvanr");
                        exams = KusssHandler.getInstance().getNewExamsByLvaNr(
                                mContext, lvaMap.getLVAs());
                    } else {
                        Log.d(TAG, "load exams");
                        exams = KusssHandler.getInstance()
                                .getNewExams(mContext);
                    }
                    if (exams == null) {
                        mSyncResult.stats.numParseExceptions++;
                    } else {
                        Map<String, Exam> examMap = new HashMap<>();
                        for (Exam exam : exams) {
                            Exam old = examMap.put(exam.getKey(), exam);
                            if (old != null) {
                                Log.w(TAG,
                                        "exam alread loaded: " + old.getKey());
                            }
                        }

                        Log.d(TAG, String.format("got %s exams", exams.size()));

                        updateNotify("Prüfungen werden aktualisiert");

                        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

                        Uri examUri = KusssContentContract.Exam.CONTENT_URI;
                        Cursor c = mProvider.query(examUri, EXAM_PROJECTION,
                                null, null, null);

                        if (c == null) {
                            Log.w(TAG, "selection failed");
                        } else {
                            Log.d(TAG,
                                    "Found "
                                            + c.getCount()
                                            + " local entries. Computing merge solution...");
                            int examId;
                            String examTerm;
                            String examLvaNr;
                            long examDate;
                            String examTime;
                            String examLocation;
                            // delete exams one day after exam
                            long validUntil = System.currentTimeMillis()
                                    + DateUtils.MILLIS_PER_DAY;

                            while (c.moveToNext()) {
                                examId = c.getInt(COLUMN_EXAM_ID);
                                examTerm = c.getString(COLUMN_EXAM_TERM);
                                examLvaNr = c.getString(COLUMN_EXAM_LVANR);
                                examDate = c.getLong(COLUMN_EXAM_DATE);
                                examTime = c.getString(COLUMN_EXAM_TIME);
                                examLocation = c
                                        .getString(COLUMN_EXAM_LOCATION);

                                Exam exam = examMap.remove(Exam.getKey(
                                        examLvaNr, examTerm, examDate));
                                if (exam != null) {
                                    // Check to see if the entry needs to be
                                    // updated
                                    Uri existingUri = examUri
                                            .buildUpon()
                                            .appendPath(
                                                    Integer.toString(examId))
                                            .build();
                                    Log.d(TAG, "Scheduling update: "
                                            + existingUri);

                                    if (!DateUtils.isSameDay(
                                            new Date(examDate), exam.getDate())
                                            || !examTime.equals(exam.getTime())
                                            || !examLocation.equals(exam
                                            .getLocation())) {
                                        mNewExamNotification.addUpdate(String
                                                .format("%s: %s, %s, %s",
                                                        df.format(exam
                                                                .getDate()),
                                                        exam.getTitle(), exam
                                                                .getTime(),
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
                                    Uri deleteUri = examUri
                                            .buildUpon()
                                            .appendPath(
                                                    Integer.toString(examId))
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

                            for (Exam exam : examMap.values()) {
                                batch.add(ContentProviderOperation
                                        .newInsert(
                                                KusssContentContract
                                                        .asEventSyncAdapter(
                                                                examUri,
                                                                mAccount.name,
                                                                mAccount.type))
                                        .withValues(exam.getContentValues())
                                        .build());
                                Log.d(TAG,
                                        "Scheduling insert: " + exam.getTerm()
                                                + " " + exam.getLvaNr());

                                mNewExamNotification.addUpdate(String.format(
                                        "%s: %s, %s, %s",
                                        df.format(exam.getDate()),
                                        exam.getTitle(), exam.getTime(),
                                        exam.getLocation()));

                                mSyncResult.stats.numInserts++;
                            }

                            if (batch.size() > 0) {
                                updateNotify("Prüfungen werden gespeichert");

                                Log.d(TAG, "Applying batch update");
                                mProvider.applyBatch(batch);
                                Log.d(TAG, "Notify resolver");
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
                                Log.w(TAG,
                                        "No batch operations found! Do nothing");
                            }
                        }
                    }
                } else {
                    mSyncResult.stats.numAuthExceptions++;
                }
            } catch (Exception e) {
                Analytics.sendException(mContext, e, true);
                Log.e(TAG, "import failed", e);
            }
        }

        setImportDone();

        if (mUpdateNotification != null) {
            mUpdateNotification.cancel();
        }
        mNewExamNotification.show();

        return null;
    }

    private void updateNotify(String string) {
        if (mUpdateNotification != null) {
            mUpdateNotification.update(string);
        }
    }

}
