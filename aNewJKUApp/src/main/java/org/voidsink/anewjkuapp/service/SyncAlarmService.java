package org.voidsink.anewjkuapp.service;

import android.app.IntentService;
import android.content.Intent;

import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

/**
 * Created by paul on 08.02.2015.
 */
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
            AppUtils.updateSyncAlarm(this, false);
        }
        if (intent != null) {
            AppUtils.triggerSync(this, AppUtils.getAccount(this), intent.getBooleanExtra(Consts.ARG_UPDATE_CAL, false), intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS, false));
        }
    }
}
