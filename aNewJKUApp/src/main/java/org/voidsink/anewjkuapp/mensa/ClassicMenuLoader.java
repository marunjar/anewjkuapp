/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2018 Paul "Marunjar" Pretsch
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

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Matcher;

public class ClassicMenuLoader extends MensenMenuLoader {

    @Override
    protected void addCategories(Context c, MensaDay day, Elements categories) {
        for (Element category : categories) {
            // filter classic menu 1 and classic menu 2
            if (category.hasClass("category-first") || category.hasClass("category-second")) {
                try {
                    String name = category.getElementsByClass("category-title").text().replace((char) 0xA0, ' ').trim();
                    String meal = category.getElementsByClass("category-content").text().replace((char) 0xA0, ' ').trim();

                    double price = 0;
                    double priceBig = 0;
                    double oehBonus = 0;
                    String priceString = category.getElementsByClass("category-price").text().replace((char) 0xA0, ' ').trim();
                    if (!TextUtils.isEmpty(priceString)) {
                        final NumberFormat nf = NumberFormat.getInstance(Locale.FRENCH);

                        Matcher betragMatcher = betragPattern.matcher(priceString);
                        if (betragMatcher.find()) {
                            price = nf.parse(betragMatcher.group()).doubleValue();
                        }
                        if (betragMatcher.find()) {
                            priceBig = nf.parse(betragMatcher.group()).doubleValue();
                        }
                    }

                    MensaMenu menu = new MensaMenu(name, null, meal, price, priceBig, oehBonus);
                    day.addMenu(menu);
                } catch (Exception e) {
                    Analytics.sendException(c, e, false, category.text());
                }
            }
        }
    }

    @Override
    protected String getCacheKey() {
        return "Classic";
    }

    @Override
    protected String getMensaKey() {
        return Mensen.MENSA_CLASSIC;
    }


    @Override
    protected String getLocation(Context c) {
        return c.getString(R.string.mensa_title_classic);
    }
}
