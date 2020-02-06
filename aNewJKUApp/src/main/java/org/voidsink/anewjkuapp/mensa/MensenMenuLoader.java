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
import android.text.TextUtils;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.analytics.Analytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.voidsink.anewjkuapp.utils.Consts.MENSA_MENU_JKU;

public abstract class MensenMenuLoader extends BaseMenuLoader implements MenuLoader {

    private static final String PATTERN_BETRAG = "\\d+,\\d{2}";
    protected static final Pattern betragPattern = Pattern.compile(PATTERN_BETRAG);
    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

    @Override
    protected String getUrl() {
        return MENSA_MENU_JKU;
    }

    @Override
    protected Connection getConnection(String url) {
        return super.getConnection(url)
                .cookie("mensenCookieHintClosed", "1")
                .cookie("mensenExtLocation", "1")
                .cookie("selectedLocation", "1");
    }

    private Date parseDate(String value) throws ParseException {
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

    @Override
    public IMensa getMensa(Context context) {
        Mensa mensa = new Mensa(getMensaKey(), getLocation(context));

        try {
            Document doc = getData(context);
            if (doc != null) {
                Elements days = doc.select("div.menu-nav div.weekdays.desktop li.nav-item");

                if (days.size() > 0) {
                    for (Element dayElement : days) {
                        String dataIndex = dayElement.attributes().get("data-index");
                        Elements dateElements = dayElement.getElementsByClass("date");
                        if (dateElements.size() == 1 && !TextUtils.isEmpty(dataIndex)) {
                            Date date = parseDate(dateElements.get(0).text());
                            if (date != null) {
                                MensaDay day = new MensaDay(date);

                                mensa.addDay(day);

                                Elements categories = doc.select("div.menu-plan div.menu-item.menu-item-" + dataIndex);
                                if (categories.size() > 0) {
                                    addCategories(context, day, categories);
                                }
                            }
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

    protected abstract void addCategories(Context c, MensaDay day, Elements categories);

    protected abstract String getMensaKey();

    protected abstract String getLocation(Context c);
}
