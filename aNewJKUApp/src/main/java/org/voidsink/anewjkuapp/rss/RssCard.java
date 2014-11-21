package org.voidsink.anewjkuapp.rss;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.RssFeedEntryActivity;
import org.voidsink.anewjkuapp.base.ThemedCard;
import org.voidsink.anewjkuapp.rss.lib.FeedEntry;
import org.voidsink.anewjkuapp.utils.Consts;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;

/**
 * Created by paul on 16.11.2014.
 */
public class RssCard extends ThemedCard {

    private final String EMPTY_IMAGE_URL = "http://oeh.jku.at/sites/default/files/styles/generic_thumbnail_medium/public/default_images/defaultimage-article_0.png";

    private final DisplayImageOptions mOptions;
    private final FeedEntry mEntry;

    public RssCard(Context c, FeedEntry entry, DisplayImageOptions options) {
        super(c);

        setInnerLayout(R.layout.rss_feed_item);

        this.mOptions = options;
        this.mEntry = entry;

        this.setTitle(entry.getTitle());

        // init header
        CardHeader header = new CardHeader(c);
        addCardHeader(header);
        header.setTitle(getTitle());

        String preview = entry.getShortDescription();

        this.setTitle(preview);

        // add thumbnail
        CardThumbnail cardThumbnail = new UILCardThumbnail(mContext, entry.getImage());
        cardThumbnail.setExternalUsage(true);
        addCardThumbnail(cardThumbnail);

        //Set onClick listener
        this.setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                ((RssCard) card).startFeedDetailView(mEntry);
            }
        });
    }


    public RssCard(Context context) {
        this(context, null, null);
    }

    private void startFeedDetailView(FeedEntry entry) {
        Intent i = new Intent(getContext(), RssFeedEntryActivity.class);

        i.putExtra(Consts.ARG_FEED_ENTRY, entry);

        getContext().startActivity(i);
    }

    class UILCardThumbnail extends CardThumbnail {

        private final Uri mImage;

        public UILCardThumbnail(Context context, Uri image) {
            super(context);
            this.mImage = image;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View viewImage) {

            /*
             * If your cardthumbnail uses external library you have to provide how to load the image.
             * If your cardthumbnail doesn't use an external library it will use a built-in method
             */

            //It is just an example.
            //In real case you should config better the imageLoader
            ImageLoader imageLoader = ImageLoader.getInstance();

            try {
                //ignore some icons of share buttons and linked documents
                if (mImage != null &&
                        !mImage.getPath().contains("contrib/service_links/images/") &&
                        !mImage.getPath().contains("file/icons/")) {
                    imageLoader.displayImage(mImage.toString(), (ImageView) viewImage, mOptions);
                } else {
                    imageLoader.displayImage(EMPTY_IMAGE_URL, (ImageView) viewImage, mOptions);
                }
            } catch (Exception e) {
                Log.e(TAG, "displayImage failed", e);
            }
        }
    }
}