/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2020 Paul "Marunjar" Pretsch
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

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SectionedRecyclerViewAdapter<VH extends RecyclerView.ViewHolder, HVH extends RecyclerView.ViewHolder, BA extends RecyclerView.Adapter<VH> & SectionedAdapter<HVH>> extends SectionedRecyclerViewBaseAdapter<VH, HVH, BA> {

    public SectionedRecyclerViewAdapter(RecyclerView recyclerView, BA baseAdapter) {
        super(recyclerView, baseAdapter);
    }

    @Override
    protected Section[] createSections() {
        List<Section> sections = new ArrayList<>();

        long sectionId;
        long lastSectionId = 0;
        for (int i = 0; i < mBaseAdapter.getItemCount(); i++) {
            sectionId = mBaseAdapter.getHeaderId(i);
            if (sectionId != lastSectionId) {
                lastSectionId = sectionId;
                sections.add(new Section(i, Integer.toString(i)));
            }
        }
        return sections.toArray(new Section[0]);
    }

    @NonNull
    @Override
    protected HVH onCreateHeaderViewHolder(ViewGroup parent) {
        return mBaseAdapter.onCreateHeaderViewHolder(parent);
    }

    @Override
    protected void onBindHeaderViewHolder(Section section, @NonNull HVH sectionViewHolder, int position) {
        mBaseAdapter.onBindHeaderViewHolder(sectionViewHolder, section.getFirstPosition());
    }
}
