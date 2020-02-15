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
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.utils.Consts;

public class KusssNotificationHelper {

    private static final int NOTIFICATION_ERROR = R.string.notification_error;

    public static void showErrorNotification(final Context mContext,
                                             int stringResID, Exception e) {
        // contenIntent required for all Versions before ICS
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, Consts.CHANNEL_ID_DEFAULT)
                .setContentTitle(mContext.getText(stringResID))
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setAutoCancel(true);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setSmallIcon(R.drawable.ic_stat_notify_kusss_24dp);
        } else {
            mBuilder.setSmallIcon(R.drawable.ic_stat_notify_kusss_compat_24dp);
        }

        if (e != null) {
            mBuilder.setContentText(e.getLocalizedMessage());
        } else {
            mBuilder.setContentText("...");
        }

        ((NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE)).notify(
                NOTIFICATION_ERROR, mBuilder.build());
    }
}
