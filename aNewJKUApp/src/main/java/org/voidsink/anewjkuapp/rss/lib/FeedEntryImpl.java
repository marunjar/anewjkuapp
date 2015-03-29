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

package org.voidsink.anewjkuapp.rss.lib;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeedEntryImpl implements FeedEntry {

    protected Uri source;
    protected FeedInfo feedInfo;
    protected String title = "";
    protected Uri link;
    protected String description = "";
    protected String author = "";
    protected Date pubDate;
    protected String guid = "";
    protected Uri enclosure;
    protected Uri comments;
    protected List<String> categories = new ArrayList<>();
    protected Uri mImage;

    public FeedEntryImpl() {

    }

    @Override
    public FeedInfo getFeedInfo() {
        return feedInfo;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Uri getLink() {
        return link;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getShortDescription() {
        String shortDescr = htmlToStr(getDescription());

        try {
            Document doc = Jsoup.parse(shortDescr);

            Element body = doc.body();
            if (body != null) {
                List<TextNode> textNodes = body.textNodes();
                if (textNodes.size() > 0) {
                    shortDescr = textNodes.get(0).getWholeText();
                } else {
                    List<Element> children = body.children();
                    if (children.size() > 0) {
                        shortDescr = children.get(0).text();
                    } else {
                        shortDescr = doc.text();
                    }
                }
            } else {
                shortDescr = doc.text();
            }

            shortDescr = shortDescr.trim();

            Pattern p = Pattern.compile("(\\D\\.|\\?|\\!)(\\s{1,})");
            Matcher m = p.matcher(shortDescr);

            if (m.find()) {
                shortDescr = shortDescr.substring(0, m.end());
            }
            if (shortDescr.length() > 350) {
                shortDescr = shortDescr.substring(0, 175).trim() + "...";
            }

        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "gsd failed", e);
        }

        return shortDescr.trim();
    }

    private String htmlToStr(String description) {
        description = description.replaceAll("<\\s*(br)\\s*>", "\n"); // replace <br> with \n
        description = description.replaceAll("<\\s*(p)\\s*>", "\n"); // replace <p> with \n
        description = description.replaceAll("(\\\\n[\\s]*){2,}", "\\n\\n"); // replace all occurences of more than 2 \n in a row with \n\n
        description = description.replaceAll("[<](\\/)?\\w[^>]*[>]", ""); // replace all words in < > with ""
        return description;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public List<String> getCategories() {
        return categories;
    }

    public void addCategory(String category) {
        this.categories.add(category);
    }

    @Override
    public Uri getComments() {
        return comments;
    }

    @Override
    public Uri getEnclosure() {
        return enclosure;
    }

    @Override
    public String getGUID() {
        return guid;
    }

    @Override
    public Date getPubDate() {
        return pubDate;
    }

    @Override
    public Uri getSource() {
        return source;
    }

    @Override
    public Uri getImage() {
        if (mImage != null && mImage.isAbsolute()) {
            return mImage;
        }
        if (feedInfo != null) {
            return feedInfo.getImage();
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        if (source != null) {
            out.writeInt(1);
            out.writeParcelable(source, flags);
        } else {
            out.writeInt(0);
        }
        if (feedInfo != null) {
            out.writeInt(1);
            out.writeParcelable(feedInfo, flags);
        } else {
            out.writeInt(0);
        }
        out.writeString(title);
        if (link != null) {
            out.writeInt(1);
            out.writeParcelable(link, flags);
        } else {
            out.writeInt(0);
        }
        out.writeString(description);
        out.writeString(author);
        if (pubDate != null) {
            out.writeInt(1);
            out.writeLong(pubDate.getTime());
        } else {
            out.writeInt(0);
        }
        out.writeString(guid);
        if (enclosure != null) {
            out.writeInt(1);
            out.writeParcelable(enclosure, flags);
        } else {
            out.writeInt(0);
        }
        if (comments != null) {
            out.writeInt(1);
            out.writeParcelable(comments, flags);
        } else {
            out.writeInt(0);
        }
        out.writeStringList(categories);
        if (mImage != null) {
            out.writeInt(1);
            out.writeParcelable(mImage, flags);
        } else {
            out.writeInt(0);
        }
    }

    private FeedEntryImpl(Parcel in) {
        if (in.readInt() == 1) source = in.readParcelable(Uri.class.getClassLoader());
        if (in.readInt() == 1) feedInfo = in.readParcelable(FeedInfoImpl.class.getClassLoader());
        title = in.readString();
        if (in.readInt() == 1) link = in.readParcelable(Uri.class.getClassLoader());
        description = in.readString();
        author = in.readString();
        if (in.readInt() == 1) pubDate = new Date(in.readLong());
        guid = in.readString();
        if (in.readInt() == 1) enclosure = in.readParcelable(Uri.class.getClassLoader());
        if (in.readInt() == 1) comments = in.readParcelable(Uri.class.getClassLoader());
        categories = new ArrayList<>();
        in.readStringList(categories);
        if (in.readInt() == 1) mImage = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Parcelable.Creator<FeedEntryImpl> CREATOR
            = new Parcelable.Creator<FeedEntryImpl>() {
        public FeedEntryImpl createFromParcel(Parcel in) {
            return new FeedEntryImpl(in);
        }

        public FeedEntryImpl[] newArray(int size) {
            return new FeedEntryImpl[size];
        }
    };

}
