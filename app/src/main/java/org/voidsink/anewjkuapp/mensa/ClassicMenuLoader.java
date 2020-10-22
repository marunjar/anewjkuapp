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

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;

public class ClassicMenuLoader extends MensenMenuLoader {

    @Override
    protected void addCategories(Context c, MensaDay day, Elements categories) {
        Set<String> titles = new HashSet<>();
        for (Element category : categories) {
            // filter classic menu 1 and classic menu 2
            try {
                String categoryTitle = text(category.getElementsByTag("h2"), " ");
                if (!TextUtils.isEmpty(categoryTitle) && isMatchingCategoryTitle(categoryTitle) && !titles.contains(categoryTitle)) {
                    String meal = text(category.getElementsByTag("p"));

                    double price = 0;

                    if (!TextUtils.isEmpty(meal)) {
                        final NumberFormat nf = NumberFormat.getInstance(Locale.FRENCH);

                        Matcher betragMatcher = betragPattern.matcher(meal);
                        if (betragMatcher.find()) {
                            price = parsePrice(nf, betragMatcher.group());
                        }
                    }

                    MensaMenu menu = new MensaMenu(categoryTitle, null, meal, price, 0, 0);
                    day.addMenu(menu);

                    titles.add(categoryTitle);
                }
            } catch (Exception e) {
                AnalyticsHelper.sendException(c, e, false, category.text());
            }
        }

    }

    @Override
    protected boolean isMatchingCategoryTitle(String categoryTitle) {
        return categoryTitle.toUpperCase(Locale.getDefault()).contains("CLASSIC");
    }

    @Override
    protected String getCacheKey() {
        return Mensen.MENSA_CLASSIC;
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
