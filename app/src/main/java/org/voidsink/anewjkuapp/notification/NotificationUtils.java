/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2021 Paul "Marunjar" Pretsch
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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.voidsink.anewjkuapp.activity.MainActivity;

public class NotificationUtils {

    public static PendingIntent newPendingIntent(Context context, int requestCode) {
        return PendingIntent.getActivity(
                context,
                requestCode,
                new Intent(context, MainActivity.class),
                getDefaultPendingIntentFlags());
    }

    public static PendingIntent newPendingIntent(Context context, int requestCode, int fragmentId) {
        return PendingIntent.getActivity(
                context,
                requestCode,
                new Intent(context, MainActivity.class)
                        .putExtra(MainActivity.ARG_SHOW_FRAGMENT_ID, fragmentId)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
                getDefaultPendingIntentFlags());
    }

    private static int getDefaultPendingIntentFlags() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return flags;
    }

}
