package org.voidsink.anewjkuapp.notification;

import org.voidsink.anewjkuapp.R;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class KusssNotificationBuilder {

	private static final int NOTIFICATION_ERROR = R.string.notification_error;

	public static void showErrorNotification(final Context mContext,
			int stringResID, Exception e) {
		// contenIntent required for all Versions before ICS
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
				new Intent(), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				mContext).setSmallIcon(R.drawable.ic_stat_notify_kusss)
				.setContentTitle(mContext.getText(stringResID))
				.setContentIntent(pendingIntent).setAutoCancel(true);

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
