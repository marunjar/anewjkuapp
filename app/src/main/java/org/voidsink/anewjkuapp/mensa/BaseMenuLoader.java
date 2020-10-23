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

package org.voidsink.anewjkuapp.mensa;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.BuildConfig;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.jsoup.Connection.Method.GET;

public abstract class BaseMenuLoader {

    private static final String PREF_DATA_PREFIX = "MENSA_DATA_";
    private static final String PREF_DATE_PREFIX = "MENSA_DATE_";

    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

    protected double parsePrice(NumberFormat nf, String value) {
        try {
            Number number = nf.parse(value);
            return number != null ? number.doubleValue() : 0;
        } catch (ParseException ignored) {
            return 0;
        }
    }

    protected String text(Elements elements) {
        return text(elements, "\r\n");
    }

    protected String text(Elements elements, String separator) {
        StringBuilder sb = StringUtil.borrowBuilder();
        for (Element element : elements) {
            if (sb.length() != 0)
                sb.append(separator);
            sb.append(element.text().replace('\r', ' ').replace('\n', ' ').trim());
        }
        return StringUtil.releaseBuilder(sb).trim();
    }

    protected Document getData(@NonNull Context context) {
        String html = null;
        String cacheDateKey = PREF_DATE_PREFIX + getCacheKey();
        String cacheDataKey = PREF_DATA_PREFIX + getCacheKey();

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (!BuildConfig.DEBUG && (sp.getLong(cacheDateKey, 0) > (System.currentTimeMillis() - 6 * DateUtils.HOUR_IN_MILLIS))) {
            html = sp.getString(cacheDataKey, null);
        }

        if (html == null) {
            try {
                Connection connection = getConnection(getUrl());
                if (connection != null) {
                    html = connection.execute().body();

                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(cacheDataKey, html);
                    editor.putLong(cacheDateKey, System.currentTimeMillis());
                    editor.apply();
                }
            } catch (Exception e) {
                AnalyticsHelper.sendException(context, e, false);
                html = sp.getString(cacheDataKey, null);
            }
        }
        try {
            return html != null ? Jsoup.parse(html) : null;
        } catch (Exception e) {
            AnalyticsHelper.sendException(context, e, true, getCacheKey(), getUrl());

            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(cacheDateKey, 0);
            editor.apply();

            return null;
        }
    }

    protected Date parseDate(String value) throws ParseException {
        Date date;
        try {
            date = df.parse(value);
        } catch (ParseException e) {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            date = df.parse(value + year);
            cal.add(Calendar.DAY_OF_MONTH, -7);
            if (date != null && date.before(cal.getTime())) {
                date = df.parse(value + (year + 1));
            }
        }

        return date;
    }

    protected Connection getConnection(String url) {
        return Jsoup.connect(getUrl()).method(GET);
    }

    protected abstract String getCacheKey();

    protected abstract String getUrl();
}
