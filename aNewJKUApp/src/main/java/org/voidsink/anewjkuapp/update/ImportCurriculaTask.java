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
import org.voidsink.anewjkuapp.base.BaseAsyncTask;
import org.voidsink.anewjkuapp.kusss.Curriculum;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.notification.SyncNotification;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportCurriculaTask extends BaseAsyncTask<Void, Void, Void> {

    private static final String TAG = ImportCurriculaTask.class.getSimpleName();

    private static final Object sync_lock = new Object();

    private ContentProviderClient mProvider;
    private Account mAccount;
    private SyncResult mSyncResult;
    private Context mContext;
    private ContentResolver mResolver;

    private boolean mShowProgress;
    private SyncNotification mUpdateNotification;

    public static final String[] CURRICULA_PROJECTION = new String[]{
            KusssContentContract.Curricula.COL_ID,
            KusssContentContract.Curricula.COL_IS_STD,
            KusssContentContract.Curricula.COL_CURRICULA_ID,
            KusssContentContract.Curricula.COL_TITLE,
            KusssContentContract.Curricula.COL_STEOP_DONE,
            KusssContentContract.Curricula.COL_ACTIVE_STATE,
            KusssContentContract.Curricula.COL_UNI,
            KusssContentContract.Curricula.COL_DT_START,
            KusssContentContract.Curricula.COL_DT_END};

    public static final int COLUMN_STUDIES_ID = 0;
    public static final int COLUMN_STUDIES_IS_STD = 1;
    public static final int COLUMN_STUDIES_CURRICULA_ID = 2;
    public static final int COLUMN_STUDIES_TITLE = 3;
    public static final int COLUMN_STUDIES_STEOP_DONE = 4;
    public static final int COLUMN_STUDIES_ACTIVE_STATE = 5;
    public static final int COLUMN_STUDIES_UNI = 6;
    public static final int COLUMN_STUDIES_DT_START = 7;
    public static final int COLUMN_STUDIES_DT_END = 8;

    public ImportCurriculaTask(Account account, Context context) {
        this(account, null, null, null, null, context);
        this.mProvider = context.getContentResolver()
                .acquireContentProviderClient(
                        KusssContentContract.Course.CONTENT_URI);
        this.mSyncResult = new SyncResult();
        this.mShowProgress = true;
    }

    public ImportCurriculaTask(Account account, Bundle extras, String authority,
                               ContentProviderClient provider, SyncResult syncResult,
                               Context context) {
        this.mAccount = account;
        this.mProvider = provider;
        this.mSyncResult = syncResult;
        this.mResolver = context.getContentResolver();
        this.mContext = context;
        this.mShowProgress = (extras != null && extras.getBoolean(Consts.SYNC_SHOW_PROGRESS, false));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Log.d(TAG, "prepare importing LVA");

        if (mShowProgress) {
            mUpdateNotification = new SyncNotification(mContext,
                    R.string.notification_sync_studies);
            mUpdateNotification.show(mContext.getString(R.string.notification_sync_studies_loading));
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "Start importing studies");

        synchronized (sync_lock) {
            try {
                Log.d(TAG, "setup connection");

                updateNotify(mContext.getString(R.string.notification_sync_connect));

                if (KusssHandler.getInstance().isAvailable(mContext,
                        AppUtils.getAccountAuthToken(mContext, mAccount),
                        AppUtils.getAccountName(mContext, mAccount),
                        AppUtils.getAccountPassword(mContext, mAccount))) {

                    updateNotify(mContext.getString(R.string.notification_sync_studies_loading));

                    Log.d(TAG, "load lvas");

                    List<Curriculum> studies = KusssHandler.getInstance().getCurricula(mContext);
                    if (studies == null) {
                        mSyncResult.stats.numParseExceptions++;
                    } else {
                        Map<String, Curriculum> studiesMap = new HashMap<>();
                        for (Curriculum studie : studies) {
                            studiesMap.put(studie.getKey(), studie);
                        }

                        Log.d(TAG, String.format("got %s lvas", studies.size()));

                        updateNotify(mContext.getString(R.string.notification_sync_studies_updating));

                        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

                        Uri studiesUri = KusssContentContract.Curricula.CONTENT_URI;
                        Cursor c = mProvider.query(studiesUri, CURRICULA_PROJECTION,
                                null, null, null);

                        if (c == null) {
                            Log.w(TAG, "selection failed");
                        } else {
                            Log.d(TAG,
                                    "Found "
                                            + c.getCount()
                                            + " local entries. Computing merge solution...");

                            int _Id;
                            String studiesCid;
                            Date studiesDtStart;

                            while (c.moveToNext()) {
                                _Id = c.getInt(COLUMN_STUDIES_ID);
                                studiesCid = c.getString(COLUMN_STUDIES_CURRICULA_ID);
                                studiesDtStart = new Date(c.getLong(COLUMN_STUDIES_DT_START));

                                Curriculum studie = studiesMap
                                        .get(Curriculum.getKey(studiesCid, studiesDtStart));
                                if (studie != null) {
                                    studiesMap.remove(Curriculum.getKey(studiesCid, studiesDtStart));
                                    // Check to see if the entry needs to be
                                    // updated
                                    Uri existingUri = studiesUri
                                            .buildUpon()
                                            .appendPath(Integer.toString(_Id))
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
                                                    KusssContentContract.Curricula.COL_ID,
                                                    Integer.toString(_Id))
                                            .withValues(KusssHelper.getStudiesContentValues(studie))
                                            .build());
                                    mSyncResult.stats.numUpdates++;
                                } else {
                                    // delete
                                }
                            }
                            c.close();

                            for (Curriculum studie : studiesMap.values()) {
                                batch.add(ContentProviderOperation
                                        .newInsert(
                                                KusssContentContract
                                                        .asEventSyncAdapter(
                                                                studiesUri,
                                                                mAccount.name,
                                                                mAccount.type))
                                        .withValues(KusssHelper.getStudiesContentValues(studie))
                                        .build());
                                Log.d(TAG,
                                        "Scheduling insert: " + studie.getCid()
                                                + " " + studie.getDtStart().toString());
                                mSyncResult.stats.numInserts++;
                            }

                            if (batch.size() > 0) {
                                updateNotify(mContext.getString(R.string.notification_sync_studies_saving));

                                Log.d(TAG, "Applying batch update");
                                mProvider.applyBatch(batch);
                                Log.d(TAG, "Notify resolver");
                                mResolver
                                        .notifyChange(
                                                KusssContentContract.Curricula.CONTENT_CHANGED_URI,
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

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if (mUpdateNotification != null) {
            mUpdateNotification.cancel();
        }
    }

    private void updateNotify(String string) {
        if (mUpdateNotification != null) {
            mUpdateNotification.update(string);
        }
    }
}
