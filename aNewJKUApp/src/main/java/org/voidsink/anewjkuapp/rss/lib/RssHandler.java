/*******************************************************************************
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
 ******************************************************************************/

package org.voidsink.anewjkuapp.rss.lib;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class RssHandler implements PullParserHandler {

    private static final String[] TIMEZONES = {"MEST", "EST", "PST"};

    private static final String[] TIMEZONES_REPLACE = {"+0200", "-0500", "-0800"};

    private static final SimpleDateFormat[] PUBDATE_FORMATS = {
            new SimpleDateFormat("EEE', 'd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US),
            new SimpleDateFormat("d' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US),
            new SimpleDateFormat("EEE', 'd' 'MMM' 'yyyy' 'HH:mm:ss' 'z", Locale.US),

    };

    private final List<FeedEntry> mEntries = new ArrayList<>();
    private FeedInfo mInfo = null;

    public RssHandler() {
    }

    @Override
    public void processEndDocument(String tag, XmlPullParser mXmlParser) {

    }

    @Override
    public void processStartDocument(String tag, XmlPullParser mXmlParser) {

    }

    @Override
    public List<FeedEntry> getFeedEntries() {
        return mEntries;
    }

    @Override
    public FeedInfo getFeedInfo() {
        return mInfo;
    }

    @Override
    public PullParserElement createElement(String mTag, XmlPullParser mXmlParser) {
        if (mTag.equalsIgnoreCase("channel")) {
            return new FeedInfoElement(mTag);
        } else if (mTag.equalsIgnoreCase("item")) {
            return new FeedEntryElement(mTag);
        }
        return null;
    }

    @Override
    public void finishElement(String mTag, PullParserElement p) {
        if (mTag.equalsIgnoreCase("channel")) {
            mInfo = (FeedInfo) p;
        } else if (mTag.equalsIgnoreCase("item")) {
            mEntries.add((FeedEntry) p);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void finish() {
        for (FeedEntry entry : mEntries) {
            ((FeedEntryElement) entry).setFeedInfo(mInfo);
        }
    }

    private class FeedInfoElement extends FeedInfoImpl implements PullParserElement {

        private final String mTag;

        public FeedInfoElement(String tag) {
            super();
            this.mTag = tag;
        }

        @Override
        public String getTag() {
            return mTag;
        }

        @Override
        public void processEndElement(String mTag, XmlPullParser mXmlParser) throws IOException, XmlPullParserException {

        }

        @Override
        public void processStartElement(String mTag, XmlPullParser mXmlParser) throws IOException, XmlPullParserException {
            if (mTag.equalsIgnoreCase("title"))
                this.mTitle = mXmlParser.nextText();
            else if (mTag.equalsIgnoreCase("link"))
                this.mLink = Uri.parse(mXmlParser.nextText());
            else if (mTag.equalsIgnoreCase("description"))
                this.mDescription = mXmlParser.nextText();
                //article.setContent(xmlParser.getText().replaceAll("[<](/)?div[^>]*[>]", ""));
            else if (mTag.equalsIgnoreCase("ttl"))
                this.mTTL = Integer.parseInt(mXmlParser.nextText());
            else if (mTag.equalsIgnoreCase("image"))
                this.mImage = Uri.parse(mXmlParser.nextText());
        }

        @Override
        public void processText(String mTag, XmlPullParser mXmlParser) throws IOException, XmlPullParserException {

        }
    }

    private class FeedEntryElement extends FeedEntryImpl implements PullParserElement {

        private final String mTag;
        private final Pattern mPattern = Pattern.compile("-\\d{1,4}x\\d{1,4}");

        public FeedEntryElement(String tag) {
            super();
            this.mTag = tag;
        }

        @Override
        public String getTag() {
            return this.mTag;
        }

        @Override
        public void processEndElement(String mTag, XmlPullParser mXmlParser) throws IOException, XmlPullParserException {

        }

        @Override
        public void processStartElement(String mTag, XmlPullParser mXmlParser) throws IOException, XmlPullParserException {
            if (mTag.equalsIgnoreCase("title")) {
                this.title = mXmlParser.nextText();
            } else if (mTag.equalsIgnoreCase("link")) {
                this.link = Uri.parse(mXmlParser.nextText());
            } else if (mTag.equalsIgnoreCase("description")) {
                this.description = mXmlParser.nextText();
                this.mImage = Uri.parse(pullImageLink(this.description));
                //article.setContent(xmlParser.getText().replaceAll("[<](/)?div[^>]*[>]", ""));
            } else if (mTag.equalsIgnoreCase("author") || mTag.equalsIgnoreCase("dc:creator")) {
                if (TextUtils.isEmpty(this.author))
                    this.author = mXmlParser.nextText();
            } else if (mTag.equalsIgnoreCase("category")) {
                this.addCategory(mXmlParser.nextText());
            } else if (mTag.equalsIgnoreCase("comments")) {
                this.comments = Uri.parse(mXmlParser.nextText());
            } else if (mTag.equalsIgnoreCase("enclosure")) {
                this.enclosure = Uri.parse(mXmlParser.nextText());
            } else if (mTag.equalsIgnoreCase("guid")) {
                this.guid = mXmlParser.nextText();
            } else if (mTag.equalsIgnoreCase("pubDate")) {
                this.pubDate = parseDate(mXmlParser.nextText());
            } else if (mTag.equalsIgnoreCase("source")) {
                this.source = Uri.parse(mXmlParser.nextText());
            }
        }

        @Override
        public void processText(String mTag, XmlPullParser mXmlParser) throws IOException, XmlPullParserException {

        }

        public void setFeedInfo(FeedInfo feedInfo) {
            this.feedInfo = feedInfo;
        }

        private Date parseDate(String text) {
            for (int n = 0; n < TIMEZONES.length; n++) {
                text = text.replace(TIMEZONES[n], TIMEZONES_REPLACE[n]);
            }
            for (SimpleDateFormat PUBDATE_FORMAT : PUBDATE_FORMATS) {
                try {
                    return PUBDATE_FORMAT.parse(text);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private String pullImageLink(String encoded) {
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(encoded));
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && "img".equals(xpp.getName())) {
                        int count = xpp.getAttributeCount();
                        for (int x = 0; x < count; x++) {
                            if (xpp.getAttributeName(x).equalsIgnoreCase("src"))
                                return mPattern.matcher(xpp.getAttributeValue(x)).replaceAll("");
                        }
                    }
                    eventType = xpp.next();
                }
            } catch (Exception e) {
                Log.w(getClass().getSimpleName(), "pullImageLink failed", e);
            }

            return "";
        }
    }
}
