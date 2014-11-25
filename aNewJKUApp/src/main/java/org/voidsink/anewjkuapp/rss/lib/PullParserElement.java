package org.voidsink.anewjkuapp.rss.lib;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by paul on 17.11.2014.
 */
public interface PullParserElement {
    public String getTag();

    public void processEndElement(String mTag, XmlPullParser mXmlParser) throws IOException, XmlPullParserException;

    public void processStartElement(String mTag, XmlPullParser mXmlParser) throws IOException, XmlPullParserException;

    public void processText(String mTag, XmlPullParser mXmlParser) throws IOException, XmlPullParserException;
}
