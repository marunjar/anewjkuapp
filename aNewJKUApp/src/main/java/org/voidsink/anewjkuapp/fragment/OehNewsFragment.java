package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;
import org.voidsink.anewjkuapp.rss.RssFeedTab;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.List;

/**
 * Created by paul on 16.11.2014.
 */
public class OehNewsFragment extends SlidingTabsFragment {

    @Override
    protected void fillTabs(List<SlidingTabItem> mTabs) {
        mTabs.add(new RssFeedTab(getString(R.string.feed_title_oeh), Consts.FEED_ID_OEH, Consts.FEED_URL_OEH));
        mTabs.add(new RssFeedTab(getString(R.string.feed_title_rewi), Consts.FEED_ID_REWI, Consts.FEED_URL_REWI));
        mTabs.add(new RssFeedTab(getString(R.string.feed_title_sowi), Consts.FEED_ID_SOWI, Consts.FEED_URL_SOWI));
        mTabs.add(new RssFeedTab(getString(R.string.feed_title_tnf), Consts.FEED_ID_TNF, Consts.FEED_URL_TNF));
        mTabs.add(new RssFeedTab(getString(R.string.feed_title_med), Consts.FEED_ID_MED, Consts.FEED_URL_MED));
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_RSS_FEED;
    }

}
