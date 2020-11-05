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
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.UIUtils;
import org.voidsink.sectionedrecycleradapter.SectionedAdapter;

public class CourseListAdapter extends RecyclerArrayAdapter<LvaWithGrade, CourseListAdapter.LvaViewHolder> implements SectionedAdapter<CourseListAdapter.LvaHeaderHolder> {

    public CourseListAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public LvaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lva_list_item, parent, false);
        return new LvaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LvaViewHolder holder, int position) {
        LvaWithGrade lva = getItem(position);

        holder.getTitle().setText(lva.getCourse().getTitle());
        UIUtils.setTextAndVisibility(holder.getTeacher(), lva.getCourse().getTeacher());
        holder.getCourseId().setText(lva.getCourse().getCourseId());
        if (lva.getCourse().getCid() > 0) {
            holder.getCid().setText(AppUtils.format(getContext(), "[%d]", lva.getCourse().getCid()));
            holder.getCid().setVisibility(View.VISIBLE);
        } else {
            holder.getCid().setVisibility(View.GONE);
        }
        holder.getCode().setText(lva.getCourse().getCode());
        UIUtils.setTextAndVisibility(holder.getTerm(), AppUtils.termToString(lva.getCourse().getTerm()));

        Assessment grade = lva.getGrade();
        holder.getChipBack().setBackgroundColor(UIUtils.getChipGradeColor(grade));
        holder.getChipGrade().setText(UIUtils.getChipGradeText(getContext(), grade));
        holder.getChipEcts().setText(UIUtils.getChipGradeEcts(getContext(), lva.getCourse().getEcts()));
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
    public LvaHeaderHolder onCreateHeaderViewHolder(@NonNull ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_header, viewGroup, false);
        return new LvaHeaderHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(LvaHeaderHolder lvaHeaderHolder, int position) {
        LvaWithGrade lva = getItem(position);
        if (lva != null) {
            lvaHeaderHolder.getText().setText(getContext().getString(lva.getState().getStringResID()));
        }
    }

    static class LvaViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mCourseId;
        private final TextView mCode;
        private final TextView mCid;
        private final TextView mTeacher;
        private final View mChipBack;
        private final TextView mChipEcts;
        private final TextView mChipGrade;
        private final TextView mTerm;

        LvaViewHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.lva_list2_item_title);
            mCourseId = itemView.findViewById(R.id.lva_list2_item_courseId);
            mCid = itemView.findViewById(R.id.lva_list2_item_cid);
            mCode = itemView.findViewById(R.id.lva_list2_item_code);
            mTeacher = itemView.findViewById(R.id.lva_list2_item_teacher);
            mTerm = itemView.findViewById(R.id.lva_list2_item_term);

            mChipBack = itemView.findViewById(R.id.grade_chip);
            mChipEcts = itemView.findViewById(R.id.grade_chip_info);
            mChipGrade = itemView.findViewById(R.id.grade_chip_grade);
        }

        public TextView getTitle() {
            return mTitle;
        }

        public TextView getCourseId() {
            return mCourseId;
        }

        public TextView getCode() {
            return mCode;
        }

        public TextView getCid() {
            return mCid;
        }

        public TextView getTeacher() {
            return mTeacher;
        }

        public View getChipBack() {
            return mChipBack;
        }

        public TextView getChipEcts() {
            return mChipEcts;
        }

        public TextView getChipGrade() {
            return mChipGrade;
        }

        public TextView getTerm() {
            return mTerm;
        }
    }

    static class LvaHeaderHolder extends RecyclerView.ViewHolder {
        private final TextView mText;

        LvaHeaderHolder(View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.list_header_text);
        }

        public TextView getText() {
            return mText;
        }
    }
}
