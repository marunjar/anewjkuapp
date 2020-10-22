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

import java.util.Locale;

public class ChoiceMenuLoader extends MensenMenuLoader {

    @Override
    protected void addCategories(Context c, MensaDay day, Elements categories) {
        for (Element category : categories) {
            // filter choice
            String categoryTitle = text(category.getElementsByTag("h2"), " ");
            if (!TextUtils.isEmpty(categoryTitle) && isMatchingCategoryTitle(categoryTitle)) {
                Elements paragraphs = category.getElementsByTag("p");
                String name = null;
                StringBuilder meal = new StringBuilder();
                boolean hasData = false;

                for (int i = 0; i < paragraphs.size(); i++) {
                    String newName = parseName(paragraphs.get(i));
                    if (newName != null) {
                        if (hasData) {
                            day.addMenu(new MensaMenu(name, null, meal.toString().trim(), 0, 0, 0));
                        }
                        name = newName;
                        meal.setLength(0);
                        hasData = false;

                        continue;
                    } else {
                        String text = paragraphs.get(i).text().trim();
                        if (!TextUtils.isEmpty(text)) {
                            meal.append("\r\n");
                            meal.append(text);
                            hasData = true;
                        }
                    }

                    if (hasData && (i == paragraphs.size() - 1)) {
                        day.addMenu(new MensaMenu(name, null, meal.toString().trim(), 0, 0, 0));
                    }
                }
            }
        }
    }

    @Override
    protected boolean isMatchingCategoryTitle(String categoryTitle) {
        categoryTitle = categoryTitle.toUpperCase(Locale.getDefault());
        return categoryTitle.contains("CHOICE") || categoryTitle.contains("WOCHENANGEBOT");
    }

    private String parseName(Element element) {
        if (element.tag().getName().equals("p")) {
            Elements children = element.children();
            if (children.size() == 1) {
                Element child = children.get(0);
                if (child.tag().getName().equals("strong") && child.text().endsWith(":")) {
                    return child.text();
                }
            }
        }
        return null;
    }

    @Override
    protected String getCacheKey() {
        return Mensen.MENSA_CHOICE;
    }

    @Override
    protected String getMensaKey() {
        return Mensen.MENSA_CHOICE;
    }

    @Override
    protected String getLocation(Context c) {
        return c.getString(R.string.mensa_title_choice);
    }
}
