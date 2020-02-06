/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2018 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.sectionedrecycleradapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class SectionedRecyclerViewSimpleAdapter extends SectionedRecyclerViewBaseAdapter {

    private final SectionCreator mDelegate;
    private final int mSectionResourceId;
    private final int mTextResourceId;
    private final Context mContext;

    public SectionedRecyclerViewSimpleAdapter(Context context, RecyclerView recyclerView, RecyclerView.Adapter baseAdapter, SectionCreator delegate) {
        this(context, android.R.layout.simple_list_item_1, android.R.id.text1, recyclerView, baseAdapter, delegate);
    }

    public SectionedRecyclerViewSimpleAdapter(Context context, int sectionResourceId, int textResourceId, RecyclerView recyclerView, RecyclerView.Adapter baseAdapter, SectionCreator delegate) {
        super(recyclerView, baseAdapter);

        mContext = context;
        mSectionResourceId = sectionResourceId;
        mTextResourceId = textResourceId;
        mDelegate = delegate;
    }

    @Override
    protected Section[] createSections() {
        return mDelegate.createSections(mBaseAdapter);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        final View view = LayoutInflater.from(mContext).inflate(mSectionResourceId, parent, false);
        return new SectionViewHolder(view, mTextResourceId);
    }

    @Override
    protected void onBindHeaderViewHolder(Section section, RecyclerView.ViewHolder sectionViewHolder, int position) {
        ((SectionViewHolder) sectionViewHolder).title.setText(section.title);
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;

        SectionViewHolder(View view, int mTextResourceId) {
            super(view);
            title = view.findViewById(mTextResourceId);
        }
    }
}
