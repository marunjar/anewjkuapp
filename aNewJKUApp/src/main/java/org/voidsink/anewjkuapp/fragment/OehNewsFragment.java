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

package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;
import org.voidsink.anewjkuapp.rss.RssFeedTab;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.List;

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
