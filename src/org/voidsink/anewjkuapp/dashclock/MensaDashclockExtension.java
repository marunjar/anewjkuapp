package org.voidsink.anewjkuapp.dashclock;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.fragment.MensaFragment;
import org.voidsink.anewjkuapp.kusss.mensa.ChoiceMenuLoader;
import org.voidsink.anewjkuapp.kusss.mensa.ClassicMenuLoader;
import org.voidsink.anewjkuapp.kusss.mensa.KHGMenuLoader;
import org.voidsink.anewjkuapp.kusss.mensa.Mensa;
import org.voidsink.anewjkuapp.kusss.mensa.MensaDay;
import org.voidsink.anewjkuapp.kusss.mensa.MensaMenu;
import org.voidsink.anewjkuapp.kusss.mensa.RaabMenuLoader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class MensaDashclockExtension extends DashClockExtension {

	private static final String TAG = MensaDashclockExtension.class
			.getSimpleName();

	@Override
	protected void onUpdateData(int reason) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		long mFromTime = sp.getLong("pref_key_dashclock_ext_mensa_from",
				32400000);
		long mToTime = sp.getLong("pref_key_dashclock_ext_mensa_to", 46800000);

		Date now = new Date();

		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(now.getTime());

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		calendar.setTimeInMillis(0);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		long mNow = calendar.getTimeInMillis();

		Log.i(TAG, "onUpdateData: " + reason + ", " + mFromTime + " - "
				+ mToTime + "(" + mNow + ")");

		if (mNow >= mFromTime && mNow <= mToTime) {
			if (reason == DashClockExtension.UPDATE_REASON_PERIODIC
					|| reason == UPDATE_REASON_SETTINGS_CHANGED) {
				List<Mensa> mensaList = new ArrayList<Mensa>();

				if (sp.getBoolean("pref_key_dashclock_ext_mensa_classic", false)) {
					mensaList.add(new ClassicMenuLoader()
							.getMensa(getApplicationContext()));
				}
				if (sp.getBoolean("pref_key_dashclock_ext_mensa_choice", false)) {
					mensaList.add(new ChoiceMenuLoader()
							.getMensa(getApplicationContext()));
				}
				if (sp.getBoolean("pref_key_dashclock_ext_mensa_khg", false)) {
					mensaList.add(new KHGMenuLoader()
							.getMensa(getApplicationContext()));
				}
				if (sp.getBoolean("pref_key_dashclock_ext_mensa_raab", false)) {
					mensaList.add(new RaabMenuLoader()
							.getMensa(getApplicationContext()));
				}

				String status = "";
				String body = "";

				for (Mensa mensa : mensaList) {
					if (mensa != null && !mensa.isEmpty()) {
						MensaDay mensaDay = mensa.getDay(now);
						if (mensaDay != null) {
							if (!status.isEmpty()) {
								status += ", ";
							} else {
								status += "Mensa: ";
							}
							status += mensa.getName();

							for (MensaMenu mensaMenu : mensaDay.getMenus()) {
								if (!body.isEmpty()) {
									body += "\n";
								}
								String meal = mensaMenu.getMeal();
								if (meal.length() >= 55) {
									meal = meal.substring(0, 52) + "...";
								}
								body += "* " + meal;
							}
						}
					}
				}

				publishUpdate(new ExtensionData()
						.visible(true)
						.icon(R.drawable.ic_launcher_grey)
						.status("Mensa")
						.expandedTitle(status)
						.expandedBody(body)
						.clickIntent(
								new Intent(getApplicationContext(),
										MainActivity.class)
										.putExtra(
												MainActivity.ARG_SHOW_FRAGMENT,
												MensaFragment.class.getName())
										.addFlags(
												Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

						));
			}
		} else {
			publishUpdate(new ExtensionData().visible(false));
		}
	}
}
