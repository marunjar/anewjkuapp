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

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.voidsink.anewjkuapp.utils.Consts.MENSA_MENU_RAAB;

public class RaabMenuLoader extends BaseMenuLoader implements MenuLoader {

    private static final String MENU_REGEX = "MENÜ\\s*\\d";
    private static final Pattern MENU_PATTERN = Pattern.compile(MENU_REGEX);

    private static class MealCollection {

        private final String name;
        private final StringBuilder meals;
        private boolean hasData;

        private MealCollection(String name) {
            this.name = name;
            this.meals = new StringBuilder();
            this.hasData = false;
        }

        public String getName() {
            return name;
        }

        public void addMeal(String meal) {
            if (!TextUtils.isEmpty(meal)) {
                meals.append("\r\n");
                meals.append(meal);
                hasData = true;
            }
        }

        public MensaMenu getMensaMenu() {
            if (hasData) {
                return new MensaMenu(name, null, this.meals.toString().trim(), 0, 0, 0);
            } else {
                return null;
            }
        }
    }

    @Override
    public IMensa getMensa(Context context) {
        Mensa mensa = new Mensa(Mensen.MENSA_RAAB, context.getString(R.string.mensa_title_raab));

        try {
            Document doc = getData(context);
            if (doc != null) {
                Elements elements = doc.select("div.content dl > *");

                if (elements.size() > 0) {
                    MensaDay day = null;
                    for (Element element : elements) {
                        String text = element.text();

                        Matcher menuMatcher = MENU_PATTERN.matcher(text); // (courseId,term)
                        if (menuMatcher.find()) {
                            if (day != null) {
                                List<MealCollection> menus = new ArrayList<>();
                                MealCollection currentMenu = null;
                                for (Node childNode : element.childNodes()) {
                                    if (childNode instanceof TextNode) {
                                        TextNode textNode = (TextNode) childNode;
                                        String nodeText = textNode.getWholeText();
                                        if (nodeText.matches(MENU_REGEX)) {
                                            currentMenu = new MealCollection(nodeText);
                                            menus.add(currentMenu);
                                        } else if (currentMenu != null) {
                                            currentMenu.addMeal(nodeText);
                                        }
                                    }
                                }

                                for (MealCollection menu : menus) {
                                    MensaMenu mensaMenu = menu.getMensaMenu();
                                    if (mensaMenu != null) {
                                        day.addMenu(mensaMenu);
                                    }
                                }
                            }
                        } else {
                            String[] dateValues = text.split(",");
                            if (dateValues.length == 2) {
                                Date date = parseDate(dateValues[1]);
                                if (date != null) {
                                    day = new MensaDay(date);
                                    mensa.addDay(day);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            AnalyticsHelper.sendException(context, e, false);
            return null;
        }


        return mensa;
    }

    @Override
    protected String getCacheKey() {
        return Mensen.MENSA_RAAB;
    }

    @Override
    protected String getUrl() {
        return MENSA_MENU_RAAB;
    }
}
