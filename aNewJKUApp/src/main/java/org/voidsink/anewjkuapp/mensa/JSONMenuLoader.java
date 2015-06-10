/*
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp.mensa;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class JSONMenuLoader implements MenuLoader {
    protected abstract String getUrl();

    private static final String PREF_DATA_PREFIX = "MENSA_DATA_";
    private static final String PREF_DATE_PREFIX = "MENSA_DATE_";

    protected abstract String getCacheKey();

    protected abstract String getMensaKey();

    private String getData(Context context) {
        String result = null;
        String cacheDateKey = PREF_DATE_PREFIX + getCacheKey();
        String cacheDataKey = PREF_DATA_PREFIX + getCacheKey();

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (sp.getLong(cacheDateKey, 0) > (System.currentTimeMillis() - 6 * DateUtils.HOUR_IN_MILLIS)) {
            result = sp.getString(cacheDataKey, null);
        }

        if (result == null) {
            try {
                URL url = new URL(getUrl());
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(15000);

                Writer writer = new StringWriter();

                char[] buffer = new char[1024];
                Reader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                result = writer.toString();

                conn.disconnect();

                Editor editor = sp.edit();
                editor.putString(cacheDataKey, result);
                editor.putLong(cacheDateKey, System.currentTimeMillis());
                editor.apply();
            } catch (Exception e) {
                Analytics.sendException(context, e, false);
                result = sp.getString(cacheDataKey, null);
            }
        }

        return result;
    }

    private String getLocation(Context c, int nr) {
        switch (nr) {
            case 1:
                return c.getString(R.string.mensa_title_classic);
            case 2:
                return c.getString(R.string.mensa_title_choice);
            case 3:
                return c.getString(R.string.mensa_title_khg);
            case 4:
                return c.getString(R.string.mensa_title_raab);
            default:
                return c.getString(R.string.mensa_title_unknown);
        }
    }

    public IMensa getMensa(Context context) {
        Mensa mensa = null;
        try {
            String data = getData(context);
            if (data != null) {
                JSONObject jsonData = new JSONObject(data);
                if (jsonData.getString("success").equals("true")) {
                    JSONObject jsonMensa = jsonData.getJSONObject("result");

                    mensa = new Mensa(getMensaKey(), getLocation(context, Integer.parseInt(jsonMensa
                            .getString("location"))));
                    JSONArray jsonDays = jsonMensa.getJSONArray("offers");
                    for (int i = 0; i < jsonDays.length(); i++) {
                        JSONObject jsonDay = jsonDays.getJSONObject(i);
                        MensaDay day = new MensaDay(jsonDay);

                        onNewDay(day);

                        mensa.addDay(day);
                        JSONArray jsonMenus = jsonDay.getJSONArray("menus");
                        checkName(jsonMenus);
                        normalize(context, jsonMenus);
                        for (int j = 0; j < jsonMenus.length(); j++) {
                            day.addMenu(new MensaMenu(jsonMenus
                                    .getJSONObject(j)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Analytics.sendException(context, e, false);
            return null;
        }
        return mensa;
    }

    private void checkName(JSONArray jsonMenus) throws JSONException {
        if (getNameFromMeal()) {
            for (int i = 0; i < jsonMenus.length(); i++) {
                JSONObject jsonMenu = jsonMenus.getJSONObject(i);

                String meal = jsonMenu.getString("meal");
                String name = "";
                // get name from meal, separated by :
                int index = meal.indexOf(":");
                if (index >= 0) {
                    name = meal.substring(0, index).trim();
                    if (index < meal.length()) {
                        meal = meal.substring(index + 1,
                                meal.length()).trim();
                    }
                }
                jsonMenu.put("name", name);
                jsonMenu.put("meal", meal);
            }
        }
    }

    //push every followup-element one position later
    private void insert(JSONArray a, Object o, int index) throws JSONException {
        if (index >= a.length() || index < 0) { //just for some cornercases
            a.put(o);
        } else {
            Object tmp = a.get(index);
            a.put(index, o);
            //recursion ftw
            insert(a, tmp, index + 1);
        }
    }

    private void normalize(Context c, JSONArray jsonDays) throws JSONException {
        final Pattern pricePattern = Pattern.compile("(€? ?)\\d[\\,\\.]\\d{2}( ?€?)");

        int i = 0;
        while (i < jsonDays.length()) {
            try {
                JSONObject jsonDay = jsonDays.getJSONObject(i);
                String meal = jsonDay.getString("meal").trim();
                if (meal != null) {
                    Matcher priceMatcher = pricePattern.matcher(meal);

                    if (priceMatcher.find()) {
                        // get rest of meal and insert for processing as next
                        String nextMeal = meal.substring(priceMatcher.end()).trim();
                        if (Pattern.compile("\\w+").matcher(nextMeal).find()) {
                            JSONObject clone = new JSONObject(jsonDay.toString());
                            clone.put("meal", nextMeal);
                            insert(jsonDays, clone, i + 1);
                        }

                        // insert meal
                        jsonDay.put("meal", meal.substring(0, priceMatcher.start()).trim());
                        jsonDay.put("price", normalizePrice(meal.substring(priceMatcher.start(), priceMatcher.end())));
                    }
                }
            } catch (JSONException e) {
                Analytics.sendException(c, e, false);
            }
            i++;
        }
    }

    private String normalizePrice(String price) {
        String result = price.trim().replaceAll("[€ ]", "");
        try {
            String[] parts = result.split("[\\,\\.]");
            if (parts.length == 2 && parts[1].length() == 2) {
                result = parts[0] + parts[1];
            } else {
                result = "";
            }
        } catch (Exception e) {
            result = "";
        }
        return result;
    }


    protected void onNewDay(MensaDay day) {

    }

    protected boolean getNameFromMeal() {
        return false;
    }
}
