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

package org.voidsink.anewjkuapp;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.Looper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.notification.KusssNotificationBuilder;
import org.voidsink.anewjkuapp.update.ImportAssessmentTask;
import org.voidsink.anewjkuapp.update.ImportCourseTask;
import org.voidsink.anewjkuapp.update.ImportCurriculaTask;
import org.voidsink.anewjkuapp.update.ImportExamTask;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KusssSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final Logger logger = LoggerFactory.getLogger(KusssSyncAdapter.class);

    // Global variables
    // Define a variable to contain a content resolver instance
    private ExecutorService mExecutorService = null;

    public KusssSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public KusssSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        if (account == null || account.name == null) {
            KusssNotificationBuilder.showErrorNotification(getContext(),
                    R.string.notification_error_account_is_null, null);
            syncResult.stats.numAuthExceptions++;
            return;
        }

        logger.debug("starting sync of account: {}", account.name);

        if (!KusssHandler.getInstance().isAvailable(getContext(),
                AppUtils.getAccountAuthToken(getContext(), account),
                AppUtils.getAccountName(account),
                AppUtils.getAccountPassword(getContext(), account))) {
            syncResult.stats.numAuthExceptions++;
            return;
        }

        Looper.prepare();

        this.mExecutorService = Executors.newSingleThreadExecutor();
        try {
            AppUtils.executeEm(mExecutorService, getContext(),
                    new Callable[]{
                            new ImportCurriculaTask(account, extras,
                                    provider, syncResult, getContext()),
                            new ImportCourseTask(account, extras,
                                    provider, syncResult, getContext()),
                            new ImportAssessmentTask(account, extras,
                                    provider, syncResult, getContext()),
                            new ImportExamTask(account, extras,
                                    provider, syncResult, getContext())},
                    true);

        } finally {
            this.mExecutorService.shutdown();
            KusssHandler.getInstance().logout(getContext());
        }
    }

    @Override
    public void onSyncCanceled() {
        if (this.mExecutorService != null) {
            this.mExecutorService.shutdownNow();
        }
    }

}
