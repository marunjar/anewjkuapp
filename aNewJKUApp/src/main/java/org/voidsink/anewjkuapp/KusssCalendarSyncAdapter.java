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

package org.voidsink.anewjkuapp;

import android.Manifest;
import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import net.fortuna.ical4j.data.CalendarBuilder;

import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.notification.KusssNotificationBuilder;
import org.voidsink.anewjkuapp.update.ImportCalendarTask;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KusssCalendarSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = KusssCalendarSyncAdapter.class
            .getSimpleName();

    // Global variables
    // Define a variable to contain a content resolver instance
    private final CalendarBuilder mCalendarBuilder;
    private ExecutorService mExecutorService = null;

    public KusssCalendarSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it from the
         * incoming Context
         */
        this.mCalendarBuilder = CalendarUtils.newCalendarBuilder(); // must create in main
    }

    public KusssCalendarSyncAdapter(Context context, boolean autoInitialize,
                                    boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it from the
         * incoming Context
         */
        this.mCalendarBuilder = CalendarUtils.newCalendarBuilder(); // must create in main
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

        Log.d(TAG, "starting sync of account: " + account.name);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            syncResult.stats.numAuthExceptions++;
            return;
        }

        if (!KusssHandler.getInstance().isAvailable(getContext(),
                AppUtils.getAccountAuthToken(getContext(), account),
                AppUtils.getAccountName(account),
                AppUtils.getAccountPassword(getContext(), account))) {
            syncResult.stats.numAuthExceptions++;
            return;
        }

        Looper.prepare();

        mExecutorService = Executors.newSingleThreadExecutor();
        try {
            AppUtils.executeEm(mExecutorService, getContext(),
                    new Callable[]{
                            new ImportCalendarTask(account, extras,
                                    provider, syncResult, getContext(),
                                    CalendarUtils.ARG_CALENDAR_EXAM, mCalendarBuilder),
                            new ImportCalendarTask(account, extras, provider,
                                    syncResult, getContext(),
                                    CalendarUtils.ARG_CALENDAR_COURSE, mCalendarBuilder)
                    },
                    true
            );
        } finally {
            mExecutorService.shutdown();
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
