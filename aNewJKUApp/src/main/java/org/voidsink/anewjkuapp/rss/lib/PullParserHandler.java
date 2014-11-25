package org.voidsink.anewjkuapp.rss.lib;

import org.xmlpull.v1.XmlPullParser;

import java.util.List;

/**
 * Created by paul on 17.11.2014.
 */
public interface PullParserHandler {

    public void processEndDocument(String tag, XmlPullParser mXmlParser);

    public void processStartDocument(String tag, XmlPullParser mXmlParser);

    public List<FeedEntry> getFeedEntries();

    public FeedInfo getFeedInfo();

    public PullParserElement createElement(String mTag, XmlPullParser mXmlParser);

    public void finishElement(String mTag, PullParserElement p);

    public void start();

    public void finish();
}
