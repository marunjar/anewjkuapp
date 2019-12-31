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
import org.voidsink.anewjkuapp.kusss.Curriculum;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportCurriculaWorker extends BaseWorker {

    private static final Logger logger = LoggerFactory.getLogger(ImportCurriculaWorker.class);

    public ImportCurriculaWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        return importCurricula();
    }

    private Result importCurricula() {
        Analytics.eventReloadCurricula(getApplicationContext());

        final Account mAccount = AppUtils.getAccount(getApplicationContext());
        if (mAccount == null) {
            return getSuccess();
        }

        final ContentResolver mResolver = getApplicationContext().getContentResolver();
        if (mResolver == null) {
            return getFailure();
        }

        final ContentProviderClient mProvider = mResolver.acquireContentProviderClient(KusssContentContract.Curricula.CONTENT_URI);

        if (mProvider == null) {
            return getFailure();
        }

        showUpdateNotification(R.string.notification_sync_curricula, R.string.notification_sync_curricula_loading);

        try {
            logger.debug("setup connection");

            updateNotification(getApplicationContext().getString(R.string.notification_sync_connect));

            if (KusssHandler.getInstance().isAvailable(getApplicationContext(),
                    AppUtils.getAccountAuthToken(getApplicationContext(), mAccount),
                    AppUtils.getAccountName(mAccount),
                    AppUtils.getAccountPassword(getApplicationContext(), mAccount))) {

                updateNotification(getApplicationContext().getString(R.string.notification_sync_curricula_loading));

                logger.debug("load lvas");

                List<Curriculum> curricula = KusssHandler.getInstance().getCurricula(getApplicationContext());
                if (curricula == null) {
                    return getRetry();
                } else {
                    Map<String, Curriculum> curriculaMap = new HashMap<>();
                    for (Curriculum curriculum : curricula) {
                        curriculaMap.put(KusssHelper.getCurriculumKey(curriculum.getCid(), curriculum.getDtStart()), curriculum);
                    }

                    logger.debug("got {} lvas", curricula.size());

                    updateNotification(getApplicationContext().getString(R.string.notification_sync_curricula_updating));

                    ArrayList<ContentProviderOperation> batch = new ArrayList<>();

                    Uri curriculaUri = KusssContentContract.Curricula.CONTENT_URI;

                    try (Cursor c = mProvider.query(curriculaUri, KusssContentContract.Curricula.DB.PROJECTION,
                            null, null, null)) {
                        if (c == null) {
                            logger.warn("selection failed");
                        } else {
                            logger.debug("Found {} local entries. Computing merge solution...", c.getCount());

                            int _Id;
                            String curriculumCid;
                            Date curriculumDtStart;

                            while (c.moveToNext()) {
                                _Id = c.getInt(KusssContentContract.Curricula.DB.COL_ID);
                                curriculumCid = c.getString(KusssContentContract.Curricula.DB.COL_ID);
                                curriculumDtStart = new Date(c.getLong(KusssContentContract.Curricula.DB.COL_DT_START));

                                Curriculum curriculum = curriculaMap.remove(KusssHelper.getCurriculumKey(curriculumCid, curriculumDtStart));
                                if (curriculum != null) {
                                    // Check to see if the entry needs to be
                                    // updated
                                    Uri existingUri = curriculaUri
                                            .buildUpon()
                                            .appendPath(Integer.toString(_Id))
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
                                                    KusssContentContract.Curricula.COL_ID,
                                                    Integer.toString(_Id))
                                            .withValues(KusssHelper.getCurriculumContentValues(curriculum))
                                            .build());
                                } else {
                                    // delete
                                }
                            }

                            for (Curriculum curriculum : curriculaMap.values()) {
                                batch.add(ContentProviderOperation
                                        .newInsert(
                                                KusssContentContract
                                                        .asEventSyncAdapter(
                                                                curriculaUri,
                                                                mAccount.name,
                                                                mAccount.type))
                                        .withValues(KusssHelper.getCurriculumContentValues(curriculum))
                                        .build());
                                logger.debug("Scheduling insert: {} {}", curriculum.getCid(), curriculum.getDtStart().toString());
                            }

                            updateNotification(getApplicationContext().getString(R.string.notification_sync_curricula_saving));

                            if (batch.size() > 0) {
                                logger.debug("Applying batch update");
                                mProvider.applyBatch(batch);
                                logger.debug("Notify resolver");
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
                                logger.warn("No batch operations found! Do nothing");
                            }
                        }
                    }
                }
                KusssHandler.getInstance().logout(getApplicationContext());

                return getSuccess();
            } else {
                return getRetry();
            }
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
