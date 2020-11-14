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

package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.base.SimpleTextViewHolder;
import org.voidsink.anewjkuapp.kusss.Curriculum;
import org.voidsink.sectionedrecycleradapter.SectionedAdapter;

import java.text.DateFormat;

public class CurriculaAdapter extends RecyclerArrayAdapter<Curriculum, CurriculaAdapter.CurriculumViewHolder> implements SectionedAdapter<SimpleTextViewHolder> {

    private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);

    public CurriculaAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public CurriculumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.curricula_list_item, parent, false);
        return new CurriculumViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CurriculumViewHolder holder, int position) {
        Curriculum item = getItem(Curriculum.class, position);

        holder.getIsStandard().setText(item.isStandard() ? getContext().getString(R.string.curriculum_is_standard_yes) : getContext().getString(R.string.curriculum_is_standard_no));
        holder.getCid().setText(item.getCid());
        holder.getTitle().setText(item.getTitle());
        holder.getSteopDone().setText(item.isSteopDone() ? getContext().getString(R.string.curriculum_steop_done_yes) : getContext().getString(R.string.curriculum_steop_done_no));
        holder.getActiveStatus().setText(item.isActive() ? getContext().getString(R.string.curriculum_active_status_yes) : getContext().getString(R.string.curriculum_active_status_no));
        if (item.getDtStart() != null) {
            holder.getDtStart().setText(dateFormat.format(item.getDtStart()));
        }
        if (item.getDtEnd() != null) {
            holder.getDtEnd().setText(dateFormat.format(item.getDtEnd()));
        }
    }

    @Override
    public long getHeaderId(int i) {
        Curriculum curriculum = getItem(Curriculum.class, i);
        if (curriculum != null) {
            return (long) curriculum.getUni().hashCode() + (long) Integer.MAX_VALUE; // header id has to be > 0???
        }
        return 0;
    }

    @Override
    public SimpleTextViewHolder onCreateHeaderViewHolder(@NonNull ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_header, viewGroup, false);
        return new SimpleTextViewHolder(v, R.id.list_header_text);
    }

    @Override
    public void onBindHeaderViewHolder(SimpleTextViewHolder sectionViewHolder, int position) {
        Curriculum curriculum = getItem(Curriculum.class, position);
        sectionViewHolder.getText().setText(curriculum.getUni());
    }

    static class CurriculumViewHolder extends RecyclerView.ViewHolder {
        private final TextView isStandard;
        private final TextView cid;
        private final TextView title;
        private final TextView steopDone;
        private final TextView activeStatus;
        private final TextView dtStart;
        private final TextView dtEnd;

        CurriculumViewHolder(View itemView) {
            super(itemView);

            isStandard = itemView.findViewById(R.id.curriculum_is_standard);
            cid = itemView.findViewById(R.id.curriculum_id);
            title = itemView.findViewById(R.id.curriculum_title);
            steopDone = itemView.findViewById(R.id.curriculum_steop_done);
            activeStatus = itemView.findViewById(R.id.curriculum_active_status);
            dtStart = itemView.findViewById(R.id.curriculum_dt_start);
            dtEnd = itemView.findViewById(R.id.curriculum_dt_end);
        }

        public TextView getIsStandard() {
            return isStandard;
        }

        public TextView getCid() {
            return cid;
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getSteopDone() {
            return steopDone;
        }

        public TextView getActiveStatus() {
            return activeStatus;
        }

        public TextView getDtStart() {
            return dtStart;
        }

        public TextView getDtEnd() {
            return dtEnd;
        }
    }
}