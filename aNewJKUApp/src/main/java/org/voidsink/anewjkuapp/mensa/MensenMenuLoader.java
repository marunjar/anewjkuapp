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

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.analytics.Analytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.voidsink.anewjkuapp.utils.Consts.MENSA_MENU_JKU;

public abstract class MensenMenuLoader extends BaseMenuLoader implements MenuLoader {

    static final String PATTERN_BETRAG = "\\d+,\\d{2}";
    static final Pattern betragPattern = Pattern.compile(PATTERN_BETRAG);

    @Override
    protected String getUrl() {
        return MENSA_MENU_JKU;
    }

    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

    @Override
    public IMensa getMensa(Context context) {
        Mensa mensa = new Mensa(getMensaKey(), getLocation(context));

        try {
            Document doc = getData(context);
            if (doc != null) {
                Elements days = doc.select("div#speiseplan.mobile div.day");
                if (days.size() == 0) {
                    days = doc.select("div#speiseplan.desktop div.day");
                }

                if (days.size() > 0) {
                    for (Element dayElement : days) {
                        Elements dateElements = dayElement.getElementsByClass("date");
                        if (dateElements.size() == 1) {

                            MensaDay day;
                            try {
                                day = new MensaDay(df.parse(dateElements.get(0).text()));
                            } catch (ParseException e) {
                                day = null;
                            }

                            if (day != null) {
                                mensa.addDay(day);

                                Elements categories = dayElement.select("div.category");
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
