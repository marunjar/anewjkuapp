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

package org.voidsink.anewjkuapp.dashclock;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.mensa.ChoiceMenuLoader;
import org.voidsink.anewjkuapp.mensa.ClassicMenuLoader;
import org.voidsink.anewjkuapp.mensa.IDay;
import org.voidsink.anewjkuapp.mensa.IMensa;
import org.voidsink.anewjkuapp.mensa.IMenu;
import org.voidsink.anewjkuapp.mensa.KHGMenuLoader;
import org.voidsink.anewjkuapp.mensa.RaabMenuLoader;
import org.voidsink.anewjkuapp.mensa.TagestellerMenuLoader;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MensaDashclockExtension extends DashClockExtension {

    private static final Logger logger = LoggerFactory.getLogger(MensaDashclockExtension.class);

    @Override
    protected void onUpdateData(int reason) {
        boolean mShowMenu = false;

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        long mFromTime;
        long mToTime;
        boolean mShowAlays;
        try {
            mFromTime = sp.getLong("pref_key_dashclock_ext_mensa_from",
                    32400000);
            mToTime = sp.getLong("pref_key_dashclock_ext_mensa_to", 46800000);

            mShowAlays = sp.getBoolean("pref_key_dashclock_ext_mensa_always",
                    false);
        } catch (Exception e) {
            logger.error("load preferences faile", e);
            mFromTime = 32400000;
            mToTime = 46800000;
            mShowAlays = true;
        }

        Date now = new Date();

        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(now.getTime());

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        calendar.setTimeInMillis(0);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        long mNow = calendar.getTimeInMillis();

        StringBuilder status = new StringBuilder();
        StringBuilder body = new StringBuilder();

        logger.info("onUpdateData: {}, {} - {} ({})", reason, mFromTime, mToTime, mNow);

        if (mShowAlays || (mNow >= mFromTime && mNow <= mToTime)) {
            List<IMensa> mensaList = new ArrayList<>();

            if (sp.getBoolean("pref_key_dashclock_ext_mensa_classic", false)) {
                mensaList.add(new ClassicMenuLoader()
                        .getMensa(getApplicationContext()));
                mensaList.add(new TagestellerMenuLoader()
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

            for (IMensa mensa : mensaList) {
                if (mensa != null && !mensa.isEmpty()) {
                    // get menu for today
                    IDay mensaDay = mensa.getDay(now);
                    if (mensaDay != null) {
                        if (status.length() > 0) {
                            status.append(", ");
                        } else {
                            status.append(getString(R.string.title_mensa));
                            status.append(": ");
                        }
                        status.append(mensa.getName());

                        for (IMenu mensaMenu : mensaDay.getMenus()) {
                            // show menu if found
                            mShowMenu = true;

                            if (body.length() > 0) {
                                body.append("\n");
                            }
                            String meal = mensaMenu.getMeal();
                            if (meal.length() >= 55) {
                                meal = meal.substring(0, 52) + "...";
                            }
                            body.append("* ");
                            body.append(meal);
                        }
                    }
                }
            }
        }

        if (mShowMenu || mShowAlays) {
            publishUpdate(new ExtensionData()
                    .visible(true)
                    .icon(R.drawable.ic_food_24dp)
                    .status(getString(R.string.title_mensa))
                    .expandedTitle(status.toString())
                    .expandedBody(body.toString())
                    .clickIntent(
                            new Intent(getApplicationContext(),
                                    MainActivity.class).putExtra(
                                    MainActivity.ARG_SHOW_FRAGMENT_ID,
                                    R.id.nav_mensa).addFlags(
                                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
        } else {
            publishUpdate(new ExtensionData().visible(false));
        }

        Analytics.sendScreen(null, Consts.SCREEN_DASHCLOCK);
    }
}
