package org.voidsink.anewjkuapp.rss;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.voidsink.anewjkuapp.rss.lib.FeedEntry;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;

/**
 * Created by paul on 16.11.2014.
 */
public class RssCard extends Card {

    private final DisplayImageOptions options;

    public RssCard(Context c, FeedEntry entry, DisplayImageOptions options) {
        super(c);

        this.options = options;

        this.setTitle(entry.getTitle());

        // init header
        CardHeader header = new CardHeader(c);
        addCardHeader(header);
        header.setTitle(getTitle());

        String preview = entry.getShortDescription();
        /*
        if (preview != null) {
            int index = preview.indexOf("\n\n");
            if (index > 0) {
                preview = preview.substring(0, index);
            }

            if (preview.length() > 175) {
                preview = preview.substring(0, 175) + "...";
            }
        }
        */

        this.setTitle(preview);

        // add thumbnail
        CardThumbnail cardThumbnail = new UILCardThumbnail(mContext, entry.getImage());
        cardThumbnail.setExternalUsage(true);
        addCardThumbnail(cardThumbnail);
    }

    public RssCard(Context context) {
        this(context, null, null);
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
            imageLoader.init(ImageLoaderConfiguration.createDefault(mContext));

            try {
                //ignore some icons of share buttons and linked documents
                if (mImage != null &&
                        !mImage.isAbsolute() &&
                        !mImage.getPath().contains("contrib/service_links/images/") &&
                        !mImage.getPath().contains("file/icons/")) {
                    imageLoader.displayImage(mImage.toString(), (ImageView) viewImage, options);
                } else {
                    imageLoader.displayImage("http://oeh.jku.at/sites/default/files/styles/generic_thumbnail_medium/public/default_images/defaultimage-article_0.png", (ImageView) viewImage, options);
                }
            } catch (Exception e) {
                Log.e(TAG, "displayImage failed", e);
            }
        }
    }

}
