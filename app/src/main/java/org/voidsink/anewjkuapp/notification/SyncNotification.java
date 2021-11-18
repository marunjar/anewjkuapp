/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2020 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp.notification;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.utils.Consts;

public class SyncNotification {
    private final Context mContext;
    private NotificationCompat.Builder mBuilder;
    private final int id;

    public SyncNotification(Context mContext, int id) {
        this.mContext = mContext;
        this.mBuilder = null;
        this.id = id;
    }

    public void show(String mTitle) {
        ((NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);

        this.mBuilder = new NotificationCompat.Builder(this.mContext, Consts.CHANNEL_ID_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setAutoCancel(true)
                .setContentTitle(mTitle)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setProgress(0, 100, true)
                .setGroup("Sync Group")
                .setOngoing(true)
                .setPriority(PRIORITY_MIN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mBuilder.setSmallIcon(R.drawable.ic_stat_notify_kusss_24dp_anim)
                    .setLargeIcon(
                            BitmapFactory.decodeResource(
                                    this.mContext.getResources(),
                                    R.drawable.ic_refresh_white_24dp));
        } else {
            this.mBuilder.setSmallIcon(R.drawable.ic_stat_notify_kusss_compat_24dp_anim);
        }

        // pendingIntent required for all Versions before ICS
        PendingIntent pendingIntent = NotificationUtils.newPendingIntent(mContext, 0);
        this.mBuilder.setContentIntent(pendingIntent);

        ((NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE)).notify(id,
                mBuilder.build());
    }

    public void update(String mText) {
        if (this.mBuilder != null) {
            this.mBuilder.setContentText(mText);
            ((NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE)).notify(id,
                    mBuilder.build());
        }
    }

    public void cancel() {
        if (this.mBuilder != null) {
            this.mBuilder.setProgress(100, 100, false);
            ((NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
            this.mBuilder = null;
        }
    }
}
