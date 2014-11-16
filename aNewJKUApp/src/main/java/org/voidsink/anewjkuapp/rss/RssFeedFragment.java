package org.voidsink.anewjkuapp.rss;

import android.os.Bundle;

import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;
import org.voidsink.anewjkuapp.utils.Consts;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by paul on 16.11.2014.
 */
public class RssFeedFragment extends BaseFragment {

    private URL mUrl = null;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        try {
            mUrl = new URL(args.getString(Consts.ARG_FEED_URL));
        } catch (MalformedURLException e) {
            mUrl = null;
        }
    }
}
