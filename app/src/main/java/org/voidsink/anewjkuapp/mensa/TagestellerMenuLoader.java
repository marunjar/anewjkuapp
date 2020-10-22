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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TagestellerMenuLoader extends MensenMenuLoader {

    @Override
    protected void addCategories(Context c, MensaDay day, Elements categories) {
        Set<String> titles = new HashSet<>();
        for (Element category : categories) {
            // filter classic menu 1 and classic menu 2
            try {
                String categoryTitle = text(category.getElementsByTag("h2"), " ");
                if (!TextUtils.isEmpty(categoryTitle) && isMatchingCategoryTitle(categoryTitle) && !titles.contains(categoryTitle)) {
                    String meal = text(category.getElementsByTag("p"));
                    if (!TextUtils.isEmpty(meal)) {
                        MensaMenu menu = new MensaMenu(null, null, meal.trim(), 0, 0, 0);
                        day.addMenu(menu);

                        titles.add(categoryTitle);
                    }
                }
            } catch (Exception e) {
                AnalyticsHelper.sendException(c, e, false, category.text());
            }
        }

    }

    @Override
    protected boolean isMatchingCategoryTitle(String categoryTitle) {
        return categoryTitle.toUpperCase(Locale.getDefault()).contains("TAGESTELLER");
    }

    @Override
    protected String getCacheKey() {
        return Mensen.MENSA_TAGESTELLER;
    }

    @Override
    protected String getMensaKey() {
        return Mensen.MENSA_TAGESTELLER;
    }

    @Override
    protected String getLocation(Context c) {
        return c.getString(R.string.mensa_title_tagesteller);
    }
}
