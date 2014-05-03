package org.voidsink.anewjkuapp.notification;

import org.voidsink.anewjkuapp.R;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

public class SyncNotification {
	private Context mContext;
	private NotificationCompat.Builder mBuilder;
	private int id;

	public SyncNotification(Context mContext, int id) {
		this.mContext = mContext;
		this.mBuilder = null;
		this.id = id;
	}

	public void show(String mTitle) {
		this.mBuilder = new NotificationCompat.Builder(this.mContext)
				.setSmallIcon(R.drawable.ic_stat_notify_kusss)
				.setOngoing(true)
				.setLargeIcon(
						BitmapFactory.decodeResource(
								this.mContext.getResources(),
								R.drawable.ic_menu_refresh))
				.setContentTitle(mTitle).setProgress(0, 0, true);

		// contenIntent required for all Versions before ICS
		PendingIntent pendingIntent = PendingIntent.getActivity(this.mContext,
				0, new Intent(), 0);
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
			this.mBuilder.setProgress(0, 0, false);
			((NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
			this.mBuilder = null;
		}
	}
}
