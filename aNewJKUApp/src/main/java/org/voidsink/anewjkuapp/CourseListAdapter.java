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
 */

package org.voidsink.anewjkuapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.utils.UIUtils;
import org.voidsink.sectionedrecycleradapter.SectionedAdapter;

public class CourseListAdapter extends RecyclerArrayAdapter<LvaWithGrade, CourseListAdapter.LvaViewHolder> implements SectionedAdapter<CourseListAdapter.LvaHeaderHolder> {

    private final Context mContext;

    public CourseListAdapter(Context context) {
        super();
        this.mContext = context;
    }

    @Override
    public LvaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lva_list_item, parent, false);
        return new LvaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LvaViewHolder holder, int position) {
        LvaWithGrade lva = getItem(position);

        holder.mTitle.setText(lva.getCourse().getTitle());
        UIUtils.setTextAndVisibility(holder.mTeacher, lva.getCourse().getTeacher());
        holder.mCourseId.setText(lva.getCourse().getCourseId());
        if (lva.getCourse().getCid() > 0) {
            holder.mCid.setText(String.format("[%d]", lva.getCourse().getCid()));
            holder.mCid.setVisibility(View.VISIBLE);
        } else {
            holder.mCid.setVisibility(View.GONE);
        }
        holder.mCode.setText(lva.getCourse().getCode());

        Assessment grade = lva.getGrade();
        holder.mChipBack.setBackgroundColor(UIUtils.getChipGradeColor(grade));
        holder.mChipGrade.setText(UIUtils.getChipGradeText(grade));
        holder.mChipEcts.setText(UIUtils.getChipGradeEcts(lva.getCourse().getEcts()));
    }

    @Override
    public long getHeaderId(int position) {
        LvaWithGrade lva = getItem(position);
        if (lva != null) {
            return lva.getState().getStringResID();
        }
        return 0;
    }

    @Override
    public LvaHeaderHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_header, viewGroup, false);
        return new LvaHeaderHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(LvaHeaderHolder lvaHeaderHolder, int position) {
        LvaWithGrade lva = getItem(position);
        if (lva != null) {
            lvaHeaderHolder.mText.setText(mContext.getString(lva.getState().getStringResID()));
        }
    }

    protected static class LvaViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mCourseId;
        private final TextView mCode;
        private final TextView mCid;
        private final TextView mTeacher;
        private final View mChipBack;
        private final TextView mChipEcts;
        private final TextView mChipGrade;

        public LvaViewHolder(View itemView) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.lva_list2_item_title);
            mCourseId = (TextView) itemView.findViewById(R.id.lva_list2_item_courseId);
            mCid = (TextView) itemView.findViewById(R.id.lva_list2_item_cid);
            mCode = (TextView) itemView.findViewById(R.id.lva_list2_item_code);
            mTeacher = (TextView) itemView.findViewById(R.id.lva_list2_item_teacher);

            mChipBack = itemView.findViewById(R.id.grade_chip);
            mChipEcts = (TextView) itemView.findViewById(R.id.grade_chip_info);
            mChipGrade = (TextView) itemView.findViewById(R.id.grade_chip_grade);
        }
    }

    protected static class LvaHeaderHolder extends RecyclerView.ViewHolder {
        public TextView mText;

        public LvaHeaderHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView.findViewById(R.id.list_header_text);
        }
    }
}
