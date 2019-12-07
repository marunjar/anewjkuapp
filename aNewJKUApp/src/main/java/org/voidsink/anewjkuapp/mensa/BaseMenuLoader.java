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

package org.voidsink.anewjkuapp.mensa;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import androidx.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.voidsink.anewjkuapp.analytics.Analytics;

import static org.jsoup.Connection.Method.GET;

public abstract class BaseMenuLoader {

    private static final String PREF_DATA_PREFIX = "MENSA_DATA_";
    private static final String PREF_DATE_PREFIX = "MENSA_DATE_";

    protected Document getData(Context context) {
        String html = null;
        String cacheDateKey = PREF_DATE_PREFIX + getCacheKey();
        String cacheDataKey = PREF_DATA_PREFIX + getCacheKey();

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (sp.getLong(cacheDateKey, 0) > (System.currentTimeMillis() - 6 * DateUtils.HOUR_IN_MILLIS)) {
            html = sp.getString(cacheDataKey, null);
        }

        if (html == null) {
            try {
                html = Jsoup.connect(getUrl()).method(GET).execute().body();

                SharedPreferences.Editor editor = sp.edit();
                editor.putString(cacheDataKey, html);
                editor.putLong(cacheDateKey, System.currentTimeMillis());
                editor.apply();
            } catch (Exception e) {
                Analytics.sendException(context, e, false);
                html = sp.getString(cacheDataKey, null);
            }
        }
        try {
            return html != null ? Jsoup.parse(html) : null;
        } catch (Exception e) {
            Analytics.sendException(context, e, true, getCacheKey(), getUrl());

            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(cacheDateKey, 0);
            editor.apply();

            return null;
        }
    }

    protected abstract String getCacheKey();

    protected abstract String getUrl();
}
