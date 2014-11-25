package org.voidsink.anewjkuapp.rss.lib;

import android.net.Uri;
import android.os.Parcelable;

/**
 * Created by paul on 17.11.2014.
 */
public interface FeedInfo extends Parcelable {

    String getTitle();

    Uri getLink();

    String getDescription();

    int getTTL();

    Uri getImage();
}
