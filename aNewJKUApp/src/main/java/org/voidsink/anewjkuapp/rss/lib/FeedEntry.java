package org.voidsink.anewjkuapp.rss.lib;

import android.net.Uri;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

/**
 * Created by paul on 17.11.2014.
 */
public interface FeedEntry extends Parcelable {

    FeedInfo getFeedInfo();

    String getTitle();

    Uri getLink();

    String getDescription();

    String getShortDescription();

    String getAuthor();

    List<String> getCategories();

    Uri getComments();

    Uri getEnclosure();

    String getGUID();

    Date getPubDate();

    Uri getSource();

    Uri getImage();
}
