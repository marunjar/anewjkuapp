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

package org.voidsink.anewjkuapp.service;

import android.app.IntentService;
import android.content.Intent;

import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

public class SyncAlarmService extends IntentService {

    public SyncAlarmService(String name) {
        super(name);
    }

    public SyncAlarmService() {
        this(SyncAlarmService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // update alarm for manual sync
        if (intent.getBooleanExtra(Consts.ARG_RECREATE_SYNC_ALARM, false)) {
            AppUtils.enableSync(this, false);
        }
        AppUtils.triggerSync(this, AppUtils.getAccount(this), intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS, false));

        if (intent.getBooleanExtra(Consts.ARG_WORKER_CAL, false)) {
            AppUtils.syncCalendars(this, false);
        }
        if (intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS, false)) {
            AppUtils.syncCurricula(this, false);
        }
    }
}
