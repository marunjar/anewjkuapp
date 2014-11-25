package org.voidsink.anewjkuapp.rss;

import android.graphics.Color;
import android.os.Bundle;

import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.utils.Consts;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by paul on 16.11.2014.
 */
public class RssFeedTab extends SlidingTabItem {

    private final String mUrl;
    private final Integer mId;

    public RssFeedTab(String title, Integer id, String url) {
        super(title, RssFeedFragment.class);
        this.mId = id;
        this.mUrl = url;
    }

    @Override
    protected Bundle getArguments() {
        Bundle b = new Bundle();
        b.putInt(Consts.ARG_FEED_ID, mId);
        b.putString(Consts.ARG_FEED_URL, mUrl);

        return b;
    }
}
