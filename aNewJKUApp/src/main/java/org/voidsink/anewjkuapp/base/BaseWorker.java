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

package org.voidsink.anewjkuapp.base;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.voidsink.anewjkuapp.notification.SyncNotification;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

public abstract class BaseWorker extends Worker {

    private SyncNotification mSyncNotification = null;

    protected BaseWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public void onStopped() {
        super.onStopped();

        cancelUpdateNotification();
    }

    protected void showUpdateNotification(int id, int titleId) {
        if (mSyncNotification == null) {
            showUpdateNotification(id, getApplicationContext().getString(titleId));
        }
    }

    protected void showUpdateNotification(int id, String title) {
        if (mSyncNotification == null) {
            if (getInputData().getBoolean(Consts.SYNC_SHOW_PROGRESS, false)) {
                mSyncNotification = new SyncNotification(getApplicationContext(), id);
                mSyncNotification.show(title);
            }
        }
    }

    protected void cancelUpdateNotification() {
        if (mSyncNotification != null) {
            mSyncNotification.cancel();
        }
    }

    protected void updateNotification(String string) {
        if (mSyncNotification != null) {
            mSyncNotification.update(string);
        }
    }

    protected Result getSuccess() {
        return Result.success();
    }

    protected Result getFailure() {
        return Result.failure();
    }

    protected Result getRetry() {
        final Account mAccount = AppUtils.getAccount(getApplicationContext());
        if (mAccount == null) {
            return Result.success(new Data.Builder().putString(Consts.ARG_ACCOUNT_TYPE, null).build());
        }
        if (getInputData().getBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, false)) {
            return Result.failure(new Data.Builder().putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, true).build());
        }
        return Result.retry();
    }
}
