/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
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
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.analytics.Analytics;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

public class KHGMenuLoader implements MenuLoader {

    private static final String PREF_DATA_PREFIX = "MENSA_DATA_";
    private static final String PREF_DATE_PREFIX = "MENSA_DATE_";

    private String getUrl() {
        if (Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
            //return "http://www.dioezese-linz.at/khg/menueplan/33420";
            return "http://www.dioezese-linz.at/institution/8075/essen/menueplan/article/33420.html";
        } else {
            //return "http://www.dioezese-linz.at/khg/menueplan/33077";
            return "http://www.dioezese-linz.at/institution/8075/essen/menueplan/article/33077.html";
        }
    }

    @Override
    public IMensa getMensa(Context context) {
        Mensa mensa = new Mensa(Mensen.MENSA_KHG, "KHG");
        MensaDay day = null;
        try {
            Document doc = getData(context);

            Elements elements = doc.getElementsByClass("detailContent");
            if (elements.size() == 1) {
                elements = elements.get(0).getElementsByTag("table");
                if (elements.size() == 1) {
                    elements = elements.get(0).getElementsByTag("tr");

                    NumberFormat nf = NumberFormat.getInstance(Locale.FRENCH);

                    for (Element element : elements) {
                        Elements columns = element.children();

                        String[] strings;
                        String meal;
                        String soup;
                        double price;
                        double priceBig;
                        double oehBonus;

                        switch (columns.size()) {
                            case 4:
                                Calendar cal = Calendar.getInstance();
                                cal.add(Calendar.DAY_OF_YEAR, -cal.get(Calendar.DAY_OF_WEEK));
                                switch (columns.get(0).text()) {
                                    case "SO":
                                        cal.add(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                                        break;
                                    case "MO":
                                        cal.add(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                                        break;
                                    case "DI":
                                        cal.add(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                                        break;
                                    case "MI":
                                        cal.add(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                                        break;
                                    case "DO":
                                        cal.add(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                                        break;
                                    case "FR":
                                        cal.add(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                                        break;
                                    case "SA":
                                        cal.add(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                                        break;
                                }
                                day = new MensaDay(cal.getTime());
                                mensa.addDay(day);

                                strings = columns.get(1).text().split(",", 2);
                                if (strings.length == 2) {
                                    soup = strings[0].trim();
                                    meal = strings[1].trim();
                                } else {
                                    soup = null;
                                    meal = strings[0];
                                }
                                try {
                                    price = nf.parse(columns.get(2).text()).doubleValue();
                                    priceBig = nf.parse(columns.get(3).text()).doubleValue();
                                    oehBonus = priceBig - price;
                                } catch (ParseException e) {
                                    price = 0;
                                    priceBig = 0;
                                    oehBonus = 0;
                                }

                                day.addMenu(new MensaMenu(null, soup, meal, price, priceBig, oehBonus));

                                break;
                            case 3:
                                //IMenu menu = new MensaMenu()
                                if (day != null) {
                                    strings = columns.get(0).text().split(",", 2);
                                    if (strings.length == 2) {
                                        soup = strings[0].trim();
                                        meal = strings[1].trim();
                                    } else {
                                        soup = null;
                                        meal = strings[0];
                                    }
                                    try {
                                        price = nf.parse(columns.get(1).text()).doubleValue();
                                        priceBig = nf.parse(columns.get(2).text()).doubleValue();
                                        oehBonus = priceBig - price;
                                    } catch (ParseException e) {
                                        price = 0;
                                        priceBig = 0;
                                        oehBonus = 0;
                                    }

                                    day.addMenu(new MensaMenu(null, soup, meal, price, priceBig, oehBonus));
                                }
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(this.getClass().getCanonicalName(), "failed", e);
            return null;
        }

        return mensa;
    }

    private Document getData(Context context) {
        Document result = null;
        String cacheDateKey = PREF_DATE_PREFIX + getCacheKey();
        String cacheDataKey = PREF_DATA_PREFIX + getCacheKey();

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        if (sp.getLong(cacheDateKey, 0) > (System.currentTimeMillis() - 6 * DateUtils.HOUR_IN_MILLIS)) {
            String html = sp.getString(cacheDataKey, null);
            if (html != null) {
                result = Jsoup.parse(html);
            }
        }

        if (result == null) {
            try {
                result = Jsoup.connect(getUrl()).get();

                SharedPreferences.Editor editor = sp.edit();
                editor.putString(cacheDataKey, result.html());
                editor.putLong(cacheDateKey, System.currentTimeMillis());
                editor.apply();
            } catch (Exception e) {
                Analytics.sendException(context, e, false);
                String html = sp.getString(cacheDataKey, null);
                if (html != null) {
                    result = Jsoup.parse(html);
                }
            }
        }
        return result;
    }

    private String getCacheKey() {
        return Mensen.MENSA_KHG;
    }
}
