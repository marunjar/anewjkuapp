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

import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adaption of SimpleSectionedListAdapter from google io 2014
 * This is used to create sections for RecyclerView and LayoutManager
 * <p>
 * https://github.com/google/iosched/blob/master/android/src/main/java/com/google/samples/apps/iosched/ui/SimpleSectionedListAdapter.java
 */
public abstract class SectionedRecyclerViewBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int SECTION_TYPE = 0;
    private boolean mValid = true;
    final RecyclerView.Adapter mBaseAdapter;
    private final SparseArray<Section> mSections = new SparseArray<>();

    public SectionedRecyclerViewBaseAdapter(RecyclerView recyclerView,
                                            RecyclerView.Adapter baseAdapter) {

        mBaseAdapter = baseAdapter;

        mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mValid = mBaseAdapter.getItemCount() > 0;
                rebuildSections();
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount() > 0;
                rebuildSections();
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount() > 0;
                rebuildSections();
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                mValid = mBaseAdapter.getItemCount() > 0;
                rebuildSections();
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });

        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return (isSectionHeaderPosition(position)) ? layoutManager.getSpanCount() : 1;
                }
            });
        }
    }

    private void rebuildSections() {
        mSections.clear();

        setSections(createSections());
    }

    protected abstract Section[] createSections();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int typeView) {
        if (typeView == SECTION_TYPE) {
            return onCreateHeaderViewHolder(parent);
        } else {
            return mBaseAdapter.onCreateViewHolder(parent, typeView - 1);
        }
    }

    protected abstract RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent);

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder sectionViewHolder, int position) {
        if (isSectionHeaderPosition(position)) {
            onBindHeaderViewHolder(mSections.get(position), sectionViewHolder, position);
        } else {
            mBaseAdapter.onBindViewHolder(sectionViewHolder, sectionedPositionToPosition(position));
        }
    }

    protected abstract void onBindHeaderViewHolder(Section section, RecyclerView.ViewHolder sectionViewHolder, int position);

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position)
                ? SECTION_TYPE
                : mBaseAdapter.getItemViewType(sectionedPositionToPosition(position)) + 1;
    }

    private void setSections(Section[] sections) {
        mSections.clear();

        Arrays.sort(sections, (lhs, rhs) -> Integer.compare(lhs.firstPosition, rhs.firstPosition));

        int offset = 0; // offset positions for the headers we're adding
        for (Section section : sections) {
            section.sectionedPosition = section.firstPosition + offset;
            mSections.append(section.sectionedPosition, section);
            ++offset;
        }
    }

    public int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).firstPosition > position) {
                break;
            }
            ++offset;
        }
        return position + offset;
    }

    private int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return RecyclerView.NO_POSITION;
        }

        int offset = 0;
        for (int i = 0; i < mSections.size(); i++) {
            if (mSections.valueAt(i).sectionedPosition > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    public boolean isSectionHeaderPosition(int position) {
        return mSections.get(position) != null;
    }


    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition(position)
                ? Integer.MAX_VALUE - mSections.indexOfKey(position)
                : mBaseAdapter.getItemId(sectionedPositionToPosition(position));
    }

    @Override
    public int getItemCount() {
        return (mValid ? mBaseAdapter.getItemCount() + mSections.size() : 0);
    }
}
