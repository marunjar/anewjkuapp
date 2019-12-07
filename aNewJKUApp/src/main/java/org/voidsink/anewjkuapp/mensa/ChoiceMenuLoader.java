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

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChoiceMenuLoader extends MensenMenuLoader {

    private static final SimpleDateFormat df = new SimpleDateFormat("EEEE, dd. MMMM yyyy", Locale.GERMAN);

    private static final String PATTER_EURO = "\\s*Euro";

    private static final Pattern betragEuroPattern = Pattern.compile(PATTERN_BETRAG + PATTER_EURO);
    private static final Pattern betrag2EuroPattern = Pattern.compile(PATTERN_BETRAG + "\\/" + PATTERN_BETRAG + PATTER_EURO);

    @Override
    protected void addCategories(Context c, MensaDay day, Elements categories) {
        for (Element category : categories) {
            // filter choice
            String categoryTitle = category.getElementsByClass("category-title").text();
            if ("Choice".equals(categoryTitle)) {
                Elements paragraphs = category.getElementsByTag("p");
                if (paragraphs.size() > 1) {
                    try {
                        Date date = df.parse(paragraphs.get(0).text().replace((char) 0xA0, ' ')); // parse after replacing &nbsp; with normal space

                        if (day.getDate().equals(date)) {
                            for (int i = 1; i < paragraphs.size(); i++) {
                                try {
                                    String name = null;

                                    StringBuilder meal = new StringBuilder();
                                    double price = 0;
                                    double priceBig = 0;
                                    boolean hasData = false;

                                    final List<Node> nodes = paragraphs.get(i).childNodes();

                                    for (int n = 0; n < nodes.size(); n++) {
                                        Node node = nodes.get(n);

                                        if (node instanceof TextNode) {
                                            TextNode textNode = (TextNode) node;

                                            String text = textNode.text().replace((char) 0xA0, ' ').trim();

                                            final NumberFormat nf = NumberFormat.getInstance(Locale.FRENCH);

                                            Matcher betrag2EuroMatcher = betrag2EuroPattern.matcher(text);
                                            if (betrag2EuroMatcher.find()) {
                                                Matcher betragMatcher = betragPattern.matcher(text.substring(betrag2EuroMatcher.start(), betrag2EuroMatcher.end()));
                                                if (betragMatcher.find()) {
                                                    price = nf.parse(betragMatcher.group()).doubleValue();
                                                }
                                                if (betragMatcher.find()) {
                                                    priceBig = nf.parse(betragMatcher.group()).doubleValue();
                                                }
                                                text = text.substring(0, betrag2EuroMatcher.start()) + text.substring(betrag2EuroMatcher.end());
                                            }

                                            Matcher betragEuroMatcher = betragEuroPattern.matcher(text);
                                            if (betragEuroMatcher.find()) {
                                                Matcher betragMatcher = betragPattern.matcher(text.substring(betragEuroMatcher.start(), betragEuroMatcher.end()));
                                                if (betragMatcher.find()) {
                                                    price = nf.parse(betragMatcher.group()).doubleValue();
                                                }

                                                text = text.substring(0, betragEuroMatcher.start()) + text.substring(betragEuroMatcher.end());
                                            }

                                            meal.append(" ");
                                            meal.append(text.trim());
                                            hasData = true;
                                        } else if (node instanceof Element) {
                                            Element elementNode = (Element) node;
                                            switch (elementNode.tag().toString()) {
                                                case "br":
                                                    if (hasData) {
                                                        day.addMenu(new MensaMenu(name, null, meal.toString().trim(), price, priceBig, 0));
                                                        meal.setLength(0);
                                                        price = 0;
                                                        priceBig = 0;
                                                        hasData = false;
                                                    }
                                                    break;
                                                case "strong":
                                                    name = elementNode.text().replace((char) 0xA0, ' ').trim();
                                                    break;
                                            }
                                        }

                                        if (hasData && (n == nodes.size() - 1)) {
                                            day.addMenu(new MensaMenu(name, null, meal.toString().trim(), price, priceBig, 0));
                                            meal.setLength(0);
                                            price = 0;
                                            priceBig = 0;
                                            hasData = false;
                                        }
                                    }
                                } catch (Exception e) {
                                    Analytics.sendException(c, e, false, paragraphs.get(i).text());
                                }
                            }
                        }
                    } catch (ParseException e) {
                        Analytics.sendException(c, e, false, category.text());
                    }
                }
            }
        }
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
