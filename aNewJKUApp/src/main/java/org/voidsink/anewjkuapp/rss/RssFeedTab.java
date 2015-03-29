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

package org.voidsink.anewjkuapp.rss;

import android.os.Bundle;

import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.utils.Consts;

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
