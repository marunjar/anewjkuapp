package org.voidsink.anewjkuapp.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Setup sync alarm
            AppUtils.updateSyncAlarm(context, false);
        }
    }
}
