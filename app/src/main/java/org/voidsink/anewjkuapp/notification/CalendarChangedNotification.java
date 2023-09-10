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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.voidsink.anewjkuapp.PreferenceHelper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalendarChangedNotification {

    private static final int MAX_LINES = 5;
    private final Context mContext;

    private final String mName;
    private final List<String> mInserts;
    private final List<String> mUpdates;
    private final List<String> mDeletes;

    public CalendarChangedNotification(Context mContext, String name) {
        this.mName = name;
        this.mContext = mContext;
        this.mInserts = new ArrayList<>();
        this.mUpdates = new ArrayList<>();
        this.mDeletes = new ArrayList<>();
    }

    public void addInsert(String text) {
        mInserts.add(text);
    }

    public void addDelete(String text) {
        mDeletes.add(text);
    }

    public void addUpdate(String text) {
        mUpdates.add(text);
    }

    private void showInternal(List<String> lines, int notificationId, int resIdtitle, int resIdContent) {
        if (PreferenceHelper.getNotifyCalendar(mContext) && (lines.size() > 0)) {
            PendingIntent pendingIntent = NotificationUtils.newPendingIntent(mContext, notificationId, R.id.nav_cal);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    mContext, Consts.CHANNEL_ID_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(mContext.getString(resIdtitle, mName))
                    .setContentText(mContext.getResources().getQuantityString(resIdContent, lines.size(), lines.size(), mName))
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_EVENT)
                    .setAutoCancel(true)
                    .setNumber(lines.size());

            mBuilder.setSmallIcon(R.drawable.ic_stat_notify_kusss_24dp)
                    .setLargeIcon(
                            BitmapFactory.decodeResource(
                                    this.mContext.getResources(),
                                    R.drawable.ic_calendar_blank_white_24dp));

            // creates big view with all grades in inbox style
            NotificationCompat.InboxStyle inBoxStyle = new NotificationCompat.InboxStyle();

            inBoxStyle.setBigContentTitle(mContext.getResources().getQuantityString(resIdContent, lines.size(), lines.size(), mName));

            Collections.sort(lines);

            for (int i = 0; i < MAX_LINES; i++) {
                if (i < lines.size()) {
                    inBoxStyle.addLine(lines.get(i));
                }
            }

            if (lines.size() > MAX_LINES) {
                int quantity = lines.size() - MAX_LINES;
                inBoxStyle.setSummaryText(mContext.getResources().getQuantityString(R.plurals.notification_more, quantity, quantity));
            }
            // Moves the big view style object into the notification object.
            mBuilder.setStyle(inBoxStyle);

            ((NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE)).notify(
                    notificationId + mName.hashCode(),
                    mBuilder.build());
        }
    }

    public void show() {
        List<String> changed = new ArrayList<>(mInserts.size() + mUpdates.size());
        changed.addAll(mInserts);
        changed.addAll(mUpdates);
        showInternal(changed, R.plurals.notification_events_changed, R.string.notification_events_changed_title, R.plurals.notification_events_changed);

        showInternal(mDeletes, R.plurals.notification_events_deleted, R.string.notification_events_deleted_title, R.plurals.notification_events_deleted);
    }
}
