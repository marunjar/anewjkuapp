/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2018 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp.update;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.notification.SyncNotification;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ImportCourseTask implements Callable<Void> {

    private static final String TAG = ImportCourseTask.class.getSimpleName();

    private ContentProviderClient mProvider;
    private boolean mReleaseProvider = false;
    private final Account mAccount;
    private SyncResult mSyncResult;
    private final Context mContext;
    private final ContentResolver mResolver;

    private boolean mShowProgress;
    private SyncNotification mUpdateNotification;

    public static final String[] COURSE_PROJECTION = new String[]{
            KusssContentContract.Course.COL_ID,
            KusssContentContract.Course.COL_TERM,
            KusssContentContract.Course.COL_COURSEID,
            KusssContentContract.Course.COL_TITLE,
            KusssContentContract.Course.COL_CURRICULA_ID,
            KusssContentContract.Course.COL_TYPE,
            KusssContentContract.Course.COL_LECTURER,
            KusssContentContract.Course.COL_SWS,
            KusssContentContract.Course.COL_ECTS,
            KusssContentContract.Course.COL_CLASS_CODE};

    private static final int COLUMN_LVA_ID = 0;
    public static final int COLUMN_LVA_TERM = 1;
    public static final int COLUMN_LVA_COURSEID = 2;
    public static final int COLUMN_LVA_TITLE = 3;
    public static final int COLUMN_LVA_CURRICULA_ID = 4;
    public static final int COLUMN_LVA_TYPE = 5;
    public static final int COLUMN_LVA_TEACHER = 6;
    public static final int COLUMN_LVA_SWS = 7;
    public static final int COLUMN_LVA_ECTS = 8;
    public static final int COLUMN_LVA_CODE = 9;

    public ImportCourseTask(Account account, Context context) {
        this(account, null, null, null, null, context);
        this.mProvider = context.getContentResolver()
                .acquireContentProviderClient(
                        KusssContentContract.Course.CONTENT_URI);
        this.mReleaseProvider = true;
        this.mSyncResult = new SyncResult();
        this.mShowProgress = true;
    }

    public ImportCourseTask(Account account, Bundle extras, String authority,
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
        if (mProvider == null) {
            return null;
        }

        if (mShowProgress) {
            mUpdateNotification = new SyncNotification(mContext,
                    R.string.notification_sync_lva);
            mUpdateNotification.show(mContext.getString(R.string.notification_sync_lva_loading));
        }


        try {
            Log.d(TAG, "setup connection");

            updateNotify(mContext.getString(R.string.notification_sync_connect));

            if (KusssHandler.getInstance().isAvailable(mContext,
                    AppUtils.getAccountAuthToken(mContext, mAccount),
                    AppUtils.getAccountName(mContext, mAccount),
                    AppUtils.getAccountPassword(mContext, mAccount))) {

                updateNotify(mContext.getString(R.string.notification_sync_lva_loading));

                Log.d(TAG, "load lvas");

                List<Term> terms = KusssContentProvider.getTerms(mContext);
                List<Course> courses = KusssHandler.getInstance().getLvas(mContext, terms);
                if (courses == null) {
                    mSyncResult.stats.numParseExceptions++;
                } else {
                    Map<String, Course> lvaMap = new HashMap<>();
                    for (Course course : courses) {
                        lvaMap.put(KusssHelper.getCourseKey(course.getTerm(), course.getCourseId()), course);
                    }
                    Map<String, Term> termMap = new HashMap<>();
                    for (Term term : terms) {
                        termMap.put(term.toString(), term);
                    }

                    Log.d(TAG, String.format("got %s lvas", courses.size()));

                    updateNotify(mContext.getString(R.string.notification_sync_lva_updating));

                    ArrayList<ContentProviderOperation> batch = new ArrayList<>();

                    Uri lvaUri = KusssContentContract.Course.CONTENT_URI;
                    Cursor c = mProvider.query(lvaUri, COURSE_PROJECTION,
                            null, null, null);

                    if (c == null) {
                        Log.w(TAG, "selection failed");
                    } else {
                        Log.d(TAG,
                                "Found "
                                        + c.getCount()
                                        + " local entries. Computing merge solution...");

                        int _id;
                        String courseTerm;
                        String courseId;

                        while (c.moveToNext()) {
                            _id = c.getInt(COLUMN_LVA_ID);
                            courseTerm = c.getString(COLUMN_LVA_TERM);
                            courseId = c.getString(COLUMN_LVA_COURSEID);

                            // update only lvas from loaded terms, ignore all other
                            Term term = termMap.get(courseTerm);
                            if (term != null && term.isLoaded()) {
                                Course course = lvaMap.remove(KusssHelper.getCourseKey(term, courseId));
                                if (course != null) {
                                    // Check to see if the entry needs to be
                                    // updated
                                    Uri existingUri = lvaUri
                                            .buildUpon()
                                            .appendPath(Integer.toString(_id))
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
                                                    KusssContentContract.Course.COL_ID,
                                                    Integer.toString(_id))
                                            .withValues(KusssHelper.getLvaContentValues(course))
                                            .build());
                                    mSyncResult.stats.numUpdates++;
                                } else {
                                    // delete
                                    Log.d(TAG,
                                            "delete: "
                                                    + KusssHelper.getCourseKey(term, courseId));
                                    // Entry doesn't exist. Remove only
                                    // newer
                                    // events from the database.
                                    Uri deleteUri = lvaUri
                                            .buildUpon()
                                            .appendPath(Integer.toString(_id))
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
                            } else {
                                mSyncResult.stats.numSkippedEntries++;
                            }
                        }
                        c.close();

                        for (Course course : lvaMap.values()) {
                            // insert only lvas from loaded terms, ignore all other
                            Term term = termMap.get(course.getTerm().toString());
                            if (term != null && term.isLoaded()) {
                                batch.add(ContentProviderOperation
                                        .newInsert(
                                                KusssContentContract
                                                        .asEventSyncAdapter(
                                                                lvaUri,
                                                                mAccount.name,
                                                                mAccount.type))
                                        .withValues(KusssHelper.getLvaContentValues(course))
                                        .build());
                                Log.d(TAG,
                                        "Scheduling insert: " + course.getTerm()
                                                + " " + course.getCourseId());
                                mSyncResult.stats.numInserts++;
                            } else {
                                mSyncResult.stats.numSkippedEntries++;
                            }
                        }

                        updateNotify(mContext.getString(R.string.notification_sync_lva_saving));

                        if (batch.size() > 0) {
                            Log.d(TAG, "Applying batch update");
                            mProvider.applyBatch(batch);
                            Log.d(TAG, "Notify resolver");
                            mResolver
                                    .notifyChange(
                                            KusssContentContract.Course.CONTENT_CHANGED_URI,
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

        if (mReleaseProvider) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mProvider.close();
            } else {
                mProvider.release();
            }
        }

        return null;
    }
}
