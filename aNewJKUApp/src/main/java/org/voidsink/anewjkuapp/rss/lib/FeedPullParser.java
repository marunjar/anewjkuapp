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

package org.voidsink.anewjkuapp.rss.lib;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Stack;

public class FeedPullParser implements FeedParser {

    private final XmlPullParser mXmlParser;

    public FeedPullParser() {
        // Initialize XmlPullParser object with a common configuration
        XmlPullParser parser = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            parser = factory.newPullParser();
        } catch (XmlPullParserException e) {
            Log.e(getClass().getSimpleName(), "ctor failed", e);
        }
        mXmlParser = parser;
    }

    @Override
    public List<FeedEntry> parse(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                connection.setRequestProperty("Cache-Control", "public, max-age=" + 7200);
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);

                return parse(new BufferedInputStream(connection.getInputStream()));
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "parse failed", e);
            return null;
        }
    }

    @Override
    public List<FeedEntry> parse(String xml) {
        return parse(new ByteArrayInputStream(xml.getBytes()));
    }

    @Override
    public synchronized List<FeedEntry> parse(InputStream is) {
        List<FeedEntry> entries;

        final Stack<PullParserElement> mParseItems = new Stack<>();
        PullParserHandler handler = new RssHandler();

        handler.start();
        try {
            mXmlParser.setInput(is, null);

            int eventType = mXmlParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                try {
                    final String mTag = mXmlParser.getName();
                    PullParserElement p = null;
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            handler.processStartDocument(mTag, mXmlParser);
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            handler.processEndDocument(mTag, mXmlParser);
                            break;
                        case XmlPullParser.START_TAG:
                            p = handler.createElement(mTag, mXmlParser);
                            if (p != null) {
                                mParseItems.push(p);
                            } else if (!mParseItems.empty()) {
                                p = mParseItems.peek();
                            }

                            if (p != null) {
                                p.processStartElement(mTag, mXmlParser);
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if (!mParseItems.empty()) {
                                p = mParseItems.peek();
                            }
                            if (p != null) {
                                p.processEndElement(mTag, mXmlParser);

                                if (mTag.equalsIgnoreCase(p.getTag())) {
                                    handler.finishElement(mTag, p);
                                    mParseItems.pop();
                                }
                            }
                            break;
                        case XmlPullParser.TEXT:
                            if (!mParseItems.empty()) {
                                p = mParseItems.peek();
                            }
                            if (p != null) {
                                p.processText(mTag, mXmlParser);
                            }
                            break;
                    }
                } catch (IOException | XmlPullParserException e) {
                    Log.e(getClass().getSimpleName(), "parse element failed", e);
                }
                eventType = mXmlParser.next();
            }

            handler.finish();

            entries = handler.getFeedEntries();
        } catch (XmlPullParserException | IOException e) {
            Log.e(getClass().getSimpleName(), "parse next failed", e);
            entries = null;
        }

        return entries;
    }

}
