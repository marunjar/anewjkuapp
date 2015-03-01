package org.voidsink.anewjkuapp.rss;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
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
import org.voidsink.anewjkuapp.rss.lib.FeedEntry;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.List;

public class RssListAdapter extends RecyclerView.Adapter<RssListAdapter.FeedEntryViewHolder> {

    private static final String TAG = RssListAdapter.class.getSimpleName();
    private static final String EMPTY_IMAGE_URL = "http://oeh.jku.at/sites/default/files/styles/generic_thumbnail_medium/public/default_images/defaultimage-article_0.png";

    private final DisplayImageOptions mOptions;
    private final Context mContext;
    private final List<FeedEntry> mDataSet;
    private OnItemClickListener mItemClickListener;

    public void clear() {
        mDataSet.clear();
    }

    public void add(FeedEntry mFeedEntry) {
        mDataSet.add(mFeedEntry);
    }

    public void addAll(List<FeedEntry> mFeedEntries) {
        mDataSet.addAll(mFeedEntries);
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int viewType, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public RssListAdapter(Context context, List<FeedEntry> dataset, DisplayImageOptions options) {
        super();

        this.mOptions = options;
        this.mContext = context;
        this.mDataSet = new ArrayList<>();

        SetOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int viewType, int position) {
                FeedEntry feedEntry = getItem(position);
                startFeedDetailView(feedEntry);
            }
        });

        addAll(dataset);
    }

    @Override
    public FeedEntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rss_feed_item, parent, false);
        final FeedEntryViewHolder vh = new FeedEntryViewHolder(v);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(v, vh.getItemViewType(), vh.getPosition());
                }
            }
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(FeedEntryViewHolder holder, int position) {

        FeedEntry item = getItem(position);

        holder.mTitle.setText(item.getTitle());
        holder.mDescription.setText(item.getShortDescription());

        ImageLoader imageLoader = ImageLoader.getInstance();

        Uri mImage = item.getImage();
        try {
            //ignore some icons of share buttons and linked documents
            if (mImage != null &&
                    !mImage.getPath().contains("contrib/service_links/images/") &&
                    !mImage.getPath().contains("file/icons/")) {
                imageLoader.displayImage(mImage.toString(), holder.mImage, mOptions);
            } else {
                imageLoader.displayImage(EMPTY_IMAGE_URL, holder.mImage, mOptions);
            }
        } catch (Exception e) {
            Log.e(TAG, "displayImage failed", e);
        }
    }

    private FeedEntry getItem(int position) {
        if (mDataSet.isEmpty()) {
            return null;
        } ;

        return mDataSet.get(position);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class FeedEntryViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public final TextView mTitle;
        public final TextView mDescription;
        public final ImageView mImage;

        public FeedEntryViewHolder(View v) {
            super(v);

            mTitle = (TextView) v.findViewById(R.id.rss_feed_item_title);
            mDescription = (TextView) v.findViewById(R.id.rss_feed_item_description);
            mImage = (ImageView) v.findViewById(R.id.rss_feed_item_image);
        }
    }

    private void startFeedDetailView(FeedEntry entry) {
        if (entry != null) {
            Intent i = new Intent(mContext, RssFeedEntryActivity.class);

            i.putExtra(Consts.ARG_FEED_ENTRY, entry);

            mContext.startActivity(i);
        }
    }
}
