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

package org.voidsink.anewjkuapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.ThemedActivity;
import org.voidsink.anewjkuapp.rss.lib.FeedEntry;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.SimpleDateFormat;

public class RssFeedEntryActivity extends ThemedActivity {

    private TextView mDate;
    private WebView mContent;
    private TextView mAuthor;
    private FeedEntry mFeedEntry;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rss_feed_entry);
        this.mDate = (TextView) findViewById(R.id.rss_feed_date);
        this.mAuthor = (TextView) findViewById(R.id.rss_feed_author);
        this.mContent = (WebView) findViewById(R.id.rss_feed_content);

        Intent i = getIntent();
        if (i != null && i.hasExtra(Consts.ARG_FEED_ENTRY)) {
            this.mFeedEntry = i.getParcelableExtra(Consts.ARG_FEED_ENTRY);
        }

        if (this.mFeedEntry != null) {
            if (this.mFeedEntry.getPubDate() != null) {
                this.mDate.setText(SimpleDateFormat.getDateTimeInstance().format(this.mFeedEntry.getPubDate()));
            } else {
                this.mDate.setText("");
            }
            this.mAuthor.setText(this.mFeedEntry.getAuthor());
            Document d = Jsoup.parse(this.mFeedEntry.getDescription());
            d.outputSettings().charset("ASCII");
            this.mContent.loadData(d.html(), "text/html", null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Analytics.sendScreen(this, Consts.SCREEN_RSS_FEED_ENTRY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rss_feed_entry, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_rss_entry_in_browser: {
                if (mFeedEntry != null && mFeedEntry.getLink() != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, mFeedEntry.getLink()));
                } else {
                    //Toast...
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_RSS_FEED_ENTRY;
    }
}
