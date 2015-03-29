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

import org.apache.commons.lang.time.DateUtils;
import org.voidsink.anewjkuapp.CourseMap;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseAsyncTask;
import org.voidsink.anewjkuapp.kusss.Exam;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.notification.NewExamNotification;
import org.voidsink.anewjkuapp.notification.SyncNotification;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportExamTask extends BaseAsyncTask<Void, Void, Void> {

    private static final String TAG = ImportExamTask.class.getSimpleName();
    private static final Object sync_lock = new Object();
    private final long mSyncFromNow;

    private ContentProviderClient mProvider;
    private Account mAccount;
    private SyncResult mSyncResult;
    private Context mContext;
    private ContentResolver mResolver;

    private boolean mShowProgress;
    private SyncNotification mUpdateNotification;
    private NewExamNotification mNewExamNotification;

    public static final String[] EXAM_PROJECTION = new String[]{
            KusssContentContract.Exam.COL_ID,
            KusssContentContract.Exam.COL_TERM,
            KusssContentContract.Exam.COL_COURSEID,
            KusssContentContract.Exam.COL_DTSTART,
            KusssContentContract.Exam.COL_DTEND,
            KusssContentContract.Exam.COL_LOCATION,
            KusssContentContract.Exam.COL_DESCRIPTION,
            KusssContentContract.Exam.COL_INFO,
            KusssContentContract.Exam.COL_IS_REGISTERED,
            KusssContentContract.Exam.COL_TITLE};

    public static final int COLUMN_EXAM_ID = 0;
    public static final int COLUMN_EXAM_TERM = 1;
    public static final int COLUMN_EXAM_COURSEID = 2;
    public static final int COLUMN_EXAM_DTSTART = 3;
    public static final int COLUMN_EXAM_DTEND = 4;
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
        this.mShowProgress = true;
    }

    public ImportExamTask(Account account, Bundle extras, String authority,
                          ContentProviderClient provider, SyncResult syncResult,
                          Context context) {
        this.mAccount = account;
        this.mProvider = provider;
        this.mSyncResult = syncResult;
        this.mResolver = context.getContentResolver();
        this.mContext = context;
        this.mShowProgress = (extras != null && extras.getBoolean(Consts.SYNC_SHOW_PROGRESS, false));
        this.mSyncFromNow = System.currentTimeMillis();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Log.d(TAG, "prepare importing exams");

        if (mShowProgress) {
            mUpdateNotification = new SyncNotification(mContext,
                    R.string.notification_sync_exam);
            mUpdateNotification.show(mContext.getString(R.string.notification_sync_exam_loading));
        }
        mNewExamNotification = new NewExamNotification(mContext);
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "Start importing exams");

        synchronized (sync_lock) {
            final DateFormat df = DateFormat.getDateInstance();

            try {
                Log.d(TAG, "setup connection");

                updateNotify(mContext.getString(R.string.notification_sync_connect));

                if (KusssHandler.getInstance().isAvailable(mContext,
                        AppUtils.getAccountAuthToken(mContext, mAccount),
                        AppUtils.getAccountName(mContext, mAccount),
                        AppUtils.getAccountPassword(mContext, mAccount))) {

                    updateNotify(mContext.getString(R.string.notification_sync_exam_loading));

                    List<Exam> exams;
                    if (PreferenceWrapper.getNewExamsByCourseId(mContext)) {
                        CourseMap courseMap = new CourseMap(mContext);
                        List<Term> terms = KusssContentProvider.getTerms(mContext);

                        Log.d(TAG, "load exams by courseId");
                        exams = KusssHandler.getInstance().getNewExamsByCourseId(
                                mContext, courseMap.getCourses(), terms);
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
                            Exam old = examMap.put(KusssHelper.getExamKey(exam.getCourseId(), AppUtils.termToString(exam.getTerm()), exam.getDtStart().getTime()), exam);
                            if (old != null) {
                                Log.w(TAG,
                                        "exam alread loaded: " + KusssHelper.getExamKey(old.getCourseId(), AppUtils.termToString(old.getTerm()), old.getDtStart().getTime()));
                            }
                        }

                        Log.d(TAG, String.format("got %s exams", exams.size()));

                        updateNotify(mContext.getString(R.string.notification_sync_exam_updating));

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
                            String examCourseId;
                            long examDtStart;
                            long examDtEnd;
                            String examLocation;

                            while (c.moveToNext()) {
                                examId = c.getInt(COLUMN_EXAM_ID);
                                examTerm = c.getString(COLUMN_EXAM_TERM);
                                examCourseId = c.getString(COLUMN_EXAM_COURSEID);
                                examDtStart = c.getLong(COLUMN_EXAM_DTSTART);
                                examDtEnd = c.getLong(COLUMN_EXAM_DTEND);
                                examLocation = c
                                        .getString(COLUMN_EXAM_LOCATION);

                                Exam exam = examMap.remove(KusssHelper.getExamKey(examCourseId, examTerm, examDtStart));
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
                                            new Date(examDtStart), exam.getDtStart())
                                            || !new Date(examDtEnd).equals(exam.getDtEnd())
                                            || !examLocation.equals(exam.getLocation())) {
                                        mNewExamNotification.addUpdate(getEventString(exam));
                                    }

                                    batch.add(ContentProviderOperation
                                            .newUpdate(
                                                    KusssContentContract
                                                            .asEventSyncAdapter(
                                                                    existingUri,
                                                                    mAccount.name,
                                                                    mAccount.type))
                                            .withValue(
                                                    KusssContentContract.Exam.COL_ID,
                                                    Integer.toString(examId))
                                            .withValues(KusssHelper.getExamContentValues(exam))
                                            .build());
                                    mSyncResult.stats.numUpdates++;
                                } else if (examDtStart > mSyncFromNow - DateUtils.MILLIS_PER_DAY) {
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
                                        .withValues(KusssHelper.getExamContentValues(exam))
                                        .build());
                                Log.d(TAG,
                                        "Scheduling insert: " + exam.getTerm()
                                                + " " + exam.getCourseId());

                                mNewExamNotification.addInsert(getEventString(exam));

                                mSyncResult.stats.numInserts++;
                            }

                            if (batch.size() > 0) {
                                updateNotify(mContext.getString(R.string.notification_sync_exam_saving));

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
                    KusssHandler.getInstance().logout(mContext);
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
        mNewExamNotification.show();
    }

    private void updateNotify(String string) {
        if (mUpdateNotification != null) {
            mUpdateNotification.update(string);
        }
    }


    private String getEventString(Exam exam) {
        return AppUtils.getEventString(exam.getDtStart().getTime(), exam.getDtEnd().getTime(), exam.getTitle());
    }

}
