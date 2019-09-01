/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2019 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp;

import android.app.Application;
import android.webkit.WebView;

import net.fortuna.ical4j.util.CompatibilityHints;
import net.fortuna.ical4j.util.MapTimeZoneCache;

import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.utils.AppUtils;

public class Globals extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize analytics
        Analytics.init(this);

        AppUtils.setupNotificationChannels(this);

        AppUtils.enableSync(this, false);

        System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
//        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);

        try {
            KusssHandler.getInstance().setUserAgent(new WebView(this).getSettings().getUserAgentString());
        } catch (Exception ignored) {
        }
    }
}
