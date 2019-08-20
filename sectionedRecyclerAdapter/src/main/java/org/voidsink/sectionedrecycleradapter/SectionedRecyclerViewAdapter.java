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

import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class SectionedRecyclerViewAdapter extends SectionedRecyclerViewBaseAdapter {

    public SectionedRecyclerViewAdapter(RecyclerView recyclerView, RecyclerView.Adapter baseAdapter) {
        super(recyclerView, baseAdapter);
    }

    @Override
    protected Section[] createSections() {
        List<Section> sections = new ArrayList<>();

        if (mBaseAdapter instanceof SectionedAdapter) {
            long sectionId, lastSectionId = 0;
            SectionedAdapter mDelegate = (SectionedAdapter) mBaseAdapter;
            for (int i = 0; i < mDelegate.getItemCount(); i++) {
                sectionId = mDelegate.getHeaderId(i);
                if (sectionId != lastSectionId) {
                    lastSectionId = sectionId;
                    sections.add(new Section(i, Integer.toString(i)));
                }
            }
        }

        return sections.toArray(new Section[0]);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return ((SectionedAdapter) mBaseAdapter).onCreateHeaderViewHolder(parent);
    }

    @Override
    protected void onBindHeaderViewHolder(Section section, RecyclerView.ViewHolder sectionViewHolder, int position) {
        if (mBaseAdapter instanceof SectionedAdapter) {
            ((SectionedAdapter) mBaseAdapter).onBindHeaderViewHolder(sectionViewHolder, section.firstPosition);
        }
    }
}
