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

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import org.voidsink.anewjkuapp.PreferenceHelper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewExamNotification {

    private static final int MAX_LINES = 5;
    private final Context mContext;

    private final List<String> mInserts;
    private final List<String> mUpdates;

    public NewExamNotification(@NonNull Context mContext) {
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

    private void showInternal(List<String> lines) {
        if (PreferenceHelper.getNotifyExam(mContext) && (lines.size() > 0)) {
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    mContext,
                    R.plurals.notification_new_exams,
                    new Intent(mContext, MainActivity.class).putExtra(
                            MainActivity.ARG_SHOW_FRAGMENT_ID,
                            R.id.nav_exams).addFlags(
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    mContext, Consts.CHANNEL_ID_EXAMS)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(mContext.getString(R.string.notification_new_exams_title))
                    .setContentText(mContext.getResources().getQuantityString(R.plurals.notification_new_exams, lines.size(), lines.size()))
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_EMAIL)
                    .setAutoCancel(true)
                    .setNumber(lines.size());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setSmallIcon(R.drawable.ic_stat_notify_kusss_24dp)
                        .setLargeIcon(
                                BitmapFactory.decodeResource(
                                        this.mContext.getResources(),
                                        R.drawable.ic_list_white_24dp));
            } else {
                mBuilder.setSmallIcon(R.drawable.ic_stat_notify_kusss_compat_24dp);
            }

            // creates big view with all grades in inbox style
            NotificationCompat.InboxStyle inBoxStyle = new NotificationCompat.InboxStyle();

            inBoxStyle.setBigContentTitle(mContext.getResources().getQuantityString(R.plurals.notification_new_exams, lines.size(), lines.size()));

            Collections.sort(lines);

            for (int i = 0; i < MAX_LINES; i++) {
                if (i < lines.size()) {
                    inBoxStyle.addLine(lines.get(i));
                }
            }

            if (lines.size() > MAX_LINES) {
                inBoxStyle.setSummaryText(mContext.getResources().getQuantityString(R.plurals.notification_more, lines.size() - MAX_LINES));
            }
            // Moves the big view style object into the notification object.
            mBuilder.setStyle(inBoxStyle);

            ((NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE)).notify(
                    R.plurals.notification_new_exams,
                    mBuilder.build());
        }
    }

    public void show() {
        List<String> changed = new ArrayList<>(mInserts.size() + mUpdates.size());
        changed.addAll(mInserts);
        changed.addAll(mUpdates);
        showInternal(changed);
    }
}
