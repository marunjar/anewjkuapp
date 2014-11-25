package org.voidsink.anewjkuapp.rss.lib;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by paul on 17.11.2014.
 */
public interface FeedParser {

    public List<FeedEntry> parse(String xml);
    public List<FeedEntry> parse(URL url);
    public List<FeedEntry> parse(InputStream is);
}
