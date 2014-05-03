package org.voidsink.anewjkuapp.notification;

import org.voidsink.anewjkuapp.R;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

public class KusssNotificationBuilder {

	private static final int NOTIFICATION_ERROR = R.string.notification_error;

	public static void showErrorNotification(final Context mContext,
			int stringResID, Exception e) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				mContext).setSmallIcon(R.drawable.ic_stat_notify_kusss)
				.setContentTitle(mContext.getText(stringResID))
				.setAutoCancel(true);
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
