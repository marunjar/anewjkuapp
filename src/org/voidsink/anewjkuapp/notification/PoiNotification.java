package org.voidsink.anewjkuapp.notification;

import java.util.ArrayList;
import java.util.List;

import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.fragment.MapFragment;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import edu.emory.mathcs.backport.java.util.Collections;

public class PoiNotification {
	private static int NOTIFICATION_POI_CHANGED = R.string.notification_poi_changed;

	private Context mContext;

	private List<String> mInserts;
	private List<String> mUpdates;

	public PoiNotification(Context mContext) {
		this.mContext = mContext;
		this.mInserts = new ArrayList<String>();
		this.mUpdates = new ArrayList<String>();
	}

	public void addInsert(String text) {
		mInserts.add(text);
	}

	public void addUpdate(String text) {
		mUpdates.add(text);
	}

	public void show() {
		if (mInserts.size() > 0 || mUpdates.size() > 0) {
			PendingIntent pendingIntent = PendingIntent
					.getActivity(mContext, NOTIFICATION_POI_CHANGED, new Intent(mContext,
							MainActivity.class).putExtra(MainActivity.ARG_SHOW_FRAGMENT, MapFragment.class.getName())
							.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0);

			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					mContext)
					.setSmallIcon(R.drawable.ic_stat_notify_kusss)
					.setLargeIcon(
							BitmapFactory.decodeResource(
									mContext.getResources(),
									R.drawable.ic_launcher_grey))
					.setContentIntent(pendingIntent)
					.setContentTitle(
							mContext.getText(R.string.notification_poi_changed_title))
					.setContentText(
							String.format(
									mContext.getString(R.string.notification_poi_changed),
									(mInserts.size() + mUpdates.size())))
					.setContentIntent(pendingIntent)
					.setAutoCancel(true)
					.setNumber(
							mInserts.size() + mUpdates.size());

			// creates big view with all grades in inbox style
			NotificationCompat.InboxStyle inBoxStyle = new NotificationCompat.InboxStyle();

			inBoxStyle.setBigContentTitle(String.format(
					mContext.getString(R.string.notification_poi_changed),
					(mInserts.size() + mUpdates.size())));

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
