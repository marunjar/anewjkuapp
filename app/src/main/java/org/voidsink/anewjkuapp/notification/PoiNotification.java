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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.core.app.NotificationCompat.PRIORITY_LOW;

public class PoiNotification {

    private final Context mContext;

    private final List<String> mInserts;
    private final List<String> mUpdates;

    public PoiNotification(Context mContext) {
        this.mContext = mContext;
        this.mInserts = new ArrayList<>();
        this.mUpdates = new ArrayList<>();
    }

    public void addInsert(String text) {
        mInserts.add(text);
    }

    public void addUpdate(String text) {
        mUpdates.add(text);
    }

    public void show() {
        int changes = mInserts.size() + mUpdates.size();
        if (changes > 0) {
            int NOTIFICATION_POI_CHANGED = R.plurals.notification_poi_changed;

            PendingIntent pendingIntent = NotificationUtils.newPendingIntent(mContext, NOTIFICATION_POI_CHANGED, R.id.nav_map);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    mContext, Consts.CHANNEL_ID_DEFAULT)
                    .setSmallIcon(R.drawable.ic_stat_notify_kusss_24dp)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(mContext.getText(R.string.notification_poi_changed_title))
                    .setContentText(mContext.getResources().getQuantityString(R.plurals.notification_poi_changed, changes, changes))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setNumber(changes)
                    .setPriority(PRIORITY_LOW);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setSmallIcon(R.drawable.ic_stat_notify_kusss_24dp)
                        .setLargeIcon(
                                BitmapFactory.decodeResource(
                                        this.mContext.getResources(),
                                        R.drawable.ic_place_white_24dp));
            } else {
                mBuilder.setSmallIcon(R.drawable.ic_stat_notify_kusss_compat_24dp);
            }

            // creates big view with all grades in inbox style
            NotificationCompat.InboxStyle inBoxStyle = new NotificationCompat.InboxStyle();

            inBoxStyle.setBigContentTitle(mContext.getResources().getQuantityString(R.plurals.notification_poi_changed, changes, changes));

            Collections.sort(mInserts);
            for (String text : mInserts) {
                inBoxStyle.addLine(text);
            }
            Collections.sort(mUpdates);
            for (String text : mUpdates) {
                inBoxStyle.addLine(text);
            }
            // Moves the big view style object into the notification object.
            mBuilder.setStyle(inBoxStyle);

            ((NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE)).notify(
                    NOTIFICATION_POI_CHANGED, mBuilder.build());
        }
    }
}
