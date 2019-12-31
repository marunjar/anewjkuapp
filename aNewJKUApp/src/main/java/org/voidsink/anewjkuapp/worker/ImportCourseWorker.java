/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2019 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp.worker;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.base.BaseWorker;
import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportCourseWorker extends BaseWorker {

    private static final Logger logger = LoggerFactory.getLogger(ImportCourseWorker.class);

    public ImportCourseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return importCourses();
    }

    private Result importCourses() {
        Analytics.eventReloadCourses(getApplicationContext());

        final Account mAccount = AppUtils.getAccount(getApplicationContext());
        if (mAccount == null) {
            return getSuccess();
        }

        final ContentResolver mResolver = getApplicationContext().getContentResolver();
        if (mResolver == null) {
            return getFailure();
        }

        final ContentProviderClient mProvider = mResolver.acquireContentProviderClient(KusssContentContract.Course.CONTENT_URI);

        if (mProvider == null) {
            return getFailure();
        }

        showUpdateNotification(R.string.notification_sync_lva, R.string.notification_sync_lva_loading);

        try {
            logger.debug("setup connection");

            updateNotification(getApplicationContext().getString(R.string.notification_sync_connect));

            if (KusssHandler.getInstance().isAvailable(getApplicationContext(),
                    AppUtils.getAccountAuthToken(getApplicationContext(), mAccount),
                    AppUtils.getAccountName(mAccount),
                    AppUtils.getAccountPassword(getApplicationContext(), mAccount))) {

                updateNotification(getApplicationContext().getString(R.string.notification_sync_lva_loading));

                logger.debug("load lvas");

                List<Term> terms = KusssContentProvider.getTerms(getApplicationContext());
                List<Course> courses = KusssHandler.getInstance().getLvas(getApplicationContext(), terms);
                if (courses == null) {
                    return getRetry();
                } else {
                    Map<String, Course> lvaMap = new HashMap<>();
                    for (Course course : courses) {
                        lvaMap.put(KusssHelper.getCourseKey(course.getTerm(), course.getCourseId()), course);
                    }
                    Map<String, Term> termMap = new HashMap<>();
                    for (Term term : terms) {
                        termMap.put(term.toString(), term);
                    }

                    logger.debug("got {} lvas", courses.size());

                    updateNotification(getApplicationContext().getString(R.string.notification_sync_lva_updating));

                    ArrayList<ContentProviderOperation> batch = new ArrayList<>();

                    Uri lvaUri = KusssContentContract.Course.CONTENT_URI;

                    try (Cursor c = mProvider.query(lvaUri, KusssContentContract.Course.DB.PROJECTION,
                            null, null, null)) {

                        if (c == null) {
                            logger.warn("selection failed");
                        } else {
                            logger.debug("Found {} local entries. Computing merge solution...", c.getCount());

                            int _id;
                            String courseTerm;
                            String courseId;

                            while (c.moveToNext()) {
                                _id = c.getInt(KusssContentContract.Course.DB.COL_ID);
                                courseTerm = c.getString(KusssContentContract.Course.DB.COL_TERM);
                                courseId = c.getString(KusssContentContract.Course.DB.COL_COURSEID);

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
                                        logger.debug("Scheduling update: {}", existingUri);

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
                                    } else {
                                        // delete
                                        logger.debug("delete: {}", KusssHelper.getCourseKey(term, courseId));
                                        // Entry doesn't exist. Remove only
                                        // newer
                                        // events from the database.
                                        Uri deleteUri = lvaUri
                                                .buildUpon()
                                                .appendPath(Integer.toString(_id))
                                                .build();
                                        logger.debug("Scheduling delete: {}", deleteUri);

                                        batch.add(ContentProviderOperation
                                                .newDelete(
                                                        KusssContentContract
                                                                .asEventSyncAdapter(
                                                                        deleteUri,
                                                                        mAccount.name,
                                                                        mAccount.type))
                                                .build());
                                    }
                                } else {
                                }
                            }

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
                                    logger.debug("Scheduling insert: {} {}", course.getTerm(), course.getCourseId());
                                } else {
                                }
                            }

                            updateNotification(getApplicationContext().getString(R.string.notification_sync_lva_saving));

                            if (batch.size() > 0) {
                                logger.debug("Applying batch update");
                                mProvider.applyBatch(batch);
                                logger.debug("Notify resolver");
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
                                logger.debug("No batch operations found! Do nothing");
                            }
                        }
                    }
                }
                KusssHandler.getInstance().logout(getApplicationContext());
            } else {
                return getRetry();
            }

            return getSuccess();
        } catch (Exception e) {
            Analytics.sendException(getApplicationContext(), e, true);

            return getRetry();
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mProvider.close();
            } else {
                mProvider.release();
            }
            cancelUpdateNotification();
        }
    }
}
