package org.voidsink.anewjkuapp.rss;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.RssFeedEntryActivity;
import org.voidsink.anewjkuapp.base.GridWithHeaderAdapter;
import org.voidsink.anewjkuapp.base.ListWithHeaderAdapter;
import org.voidsink.anewjkuapp.calendar.CalendarListEvent;
import org.voidsink.anewjkuapp.calendar.CalendarListItem;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.rss.lib.FeedEntry;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by paul on 23.11.2014.
 */
public class RssListAdapter extends GridWithHeaderAdapter<FeedEntry> {

    private static final DateFormat df = SimpleDateFormat.getDateInstance();
    private static final String TAG = RssListAdapter.class.getSimpleName();
    private static final String EMPTY_IMAGE_URL = "http://oeh.jku.at/sites/default/files/styles/generic_thumbnail_medium/public/default_images/defaultimage-article_0.png";

    private final LayoutInflater inflater;
    private final DisplayImageOptions mOptions;

    public RssListAdapter(Context context, DisplayImageOptions options) {
        super(context, R.layout.rss_feed_item);

        this.inflater = LayoutInflater.from(context);
        this.mOptions = options;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        FeedEntry item = this.getItem(position);
        if (item != null) {
            view = getFeedEntryView(convertView, parent, item);
        } else {
            view = null;
        }
        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position) != null;
    }

    private View getFeedEntryView(View convertView, ViewGroup parent, FeedEntry item) {
        FeedEntryHolder feedEntryHolder = null;
        final FeedEntry entry = item;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.rss_feed_item, parent,
                    false);

            //Set onClick listener
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startFeedDetailView(entry);
                }
            });

            feedEntryHolder = new FeedEntryHolder();

            feedEntryHolder.title = (TextView) convertView.findViewById(R.id.rss_feed_item_title);
            feedEntryHolder.description = (TextView) convertView.findViewById(R.id.rss_feed_item_description);
            feedEntryHolder.image = (ImageView) convertView.findViewById(R.id.rss_feed_item_image);

            convertView.setTag(feedEntryHolder);
        }

        if (feedEntryHolder == null) {
            feedEntryHolder = (FeedEntryHolder) convertView.getTag();
        }

        feedEntryHolder.title.setText(item.getTitle());
        feedEntryHolder.description.setText(item.getShortDescription());

        ImageLoader imageLoader = ImageLoader.getInstance();

        Uri mImage = item.getImage();
        try {
            //ignore some icons of share buttons and linked documents
            if (mImage != null &&
                    !mImage.getPath().contains("contrib/service_links/images/") &&
                    !mImage.getPath().contains("file/icons/")) {
                imageLoader.displayImage(mImage.toString(), (ImageView) feedEntryHolder.image, mOptions);
            } else {
                imageLoader.displayImage(EMPTY_IMAGE_URL, (ImageView) feedEntryHolder.image, mOptions);
            }
        } catch (Exception e) {
            Log.e(TAG, "displayImage failed", e);
        }

        return convertView;
    }

    private static class FeedEntryHolder {
        private TextView title;
        private TextView description;
        public ImageView image;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
        // Build your custom HeaderView
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        final View headerView = mInflater.inflate(R.layout.list_header, null);

        final TextView tvHeaderTitle = (TextView) headerView.findViewById(R.id.list_header_text);
        FeedEntry card = getItem(position);
        Date date = card.getPubDate();
        if (date != null) {
            tvHeaderTitle.setText(DateFormat.getDateInstance().format(date));
        }
        return headerView;
    }

    @Override
    public long getHeaderId(int position) {
        FeedEntry card = getItem(position);
        Date date = card.getPubDate();
        if (date != null) {
            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTimeInMillis(date.getTime());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        }
        return 0;
    }

    private void startFeedDetailView(FeedEntry entry) {
        Intent i = new Intent(getContext(), RssFeedEntryActivity.class);

        i.putExtra(Consts.ARG_FEED_ENTRY, entry);

        getContext().startActivity(i);
    }
}
