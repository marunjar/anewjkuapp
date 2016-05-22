/*
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
 *
 */

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

import com.bumptech.glide.Glide;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.RssFeedEntryActivity;
import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.rss.lib.FeedEntry;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.List;

public class RssListAdapter extends RecyclerArrayAdapter<FeedEntry, RssListAdapter.FeedEntryViewHolder> {

    private static final String TAG = RssListAdapter.class.getSimpleName();
    private static final String EMPTY_IMAGE_URL = "http://oeh.jku.at/sites/default/files/styles/generic_thumbnail_medium/public/default_images/defaultimage-article_0.png";

    private OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int viewType, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public RssListAdapter(Context context, List<FeedEntry> dataset) {
        super(context);

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
                    mItemClickListener.onItemClick(v, vh.getItemViewType(), vh.getAdapterPosition());
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

        Uri mImage = item.getImage();
        try {
            //ignore some icons of share buttons and linked documents
            if (mImage != null &&
                    !mImage.getPath().contains("contrib/service_links/images/") &&
                    !mImage.getPath().contains("file/icons/")) {
                Glide.with(holder.mImage.getContext())
                        .load(mImage)
                        .fitCenter()
                        .into(holder.mImage);
            } else {
                Glide.with(holder.mImage.getContext())
                        .load(EMPTY_IMAGE_URL)
                        .fitCenter()
                        .into(holder.mImage);
            }
        } catch (Exception e) {
            Log.e(TAG, "displayImage failed", e);
        }
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
            Intent i = new Intent(getContext(), RssFeedEntryActivity.class);

            i.putExtra(Consts.ARG_FEED_ENTRY, entry);

            getContext().startActivity(i);
        }
    }
}
