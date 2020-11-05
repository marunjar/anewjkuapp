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
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.UIUtils;
import org.voidsink.sectionedrecycleradapter.SectionedAdapter;

import java.text.DateFormat;

public class AssessmentListAdapter extends RecyclerArrayAdapter<Assessment, AssessmentListAdapter.GradeViewHolder> implements SectionedAdapter<AssessmentListAdapter.GradeHeaderHolder> {

    public AssessmentListAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grade_list_item, parent, false);
        return new GradeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        Assessment assessment = getItem(position);
        holder.getTitle().setText((assessment.getTitle() + " " + assessment.getLvaType()).trim());

        UIUtils.setTextAndVisibility(holder.getCourseId(), assessment.getCourseId());
        UIUtils.setTextAndVisibility(holder.getTerm(), AppUtils.termToString(assessment.getTerm()));

        if (assessment.getCid() > 0) {
            holder.getCid().setText(AppUtils.format(getContext(), "[%d]", assessment.getCid()));
            holder.getCid().setVisibility(View.VISIBLE);
        } else {
            holder.getCid().setVisibility(View.GONE);
        }

        holder.getChipBack().setBackgroundColor(UIUtils.getChipGradeColor(assessment));
        holder.getChipGrade().setText(UIUtils.getChipGradeText(getContext(), assessment));
        holder.getChipInfo().setText(UIUtils.getChipGradeEcts(getContext(), assessment.getEcts()));

        holder.getDate().setText(DateFormat.getDateInstance().format(assessment.getDate()));
        holder.getGrade().setText(getContext().getString(assessment.getGrade()
                .getStringResID()));
    }

    @Override
    public long getHeaderId(int position) {
        Assessment grade = getItem(position);
        if (grade != null) {
            return grade.getAssessmentType().getStringResID();
        }
        return 0;
    }

    @Override
    public GradeHeaderHolder onCreateHeaderViewHolder(@NonNull ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_header, viewGroup, false);
        return new GradeHeaderHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(GradeHeaderHolder gradeHeaderHolder, int position) {
        Assessment grade = getItem(position);
        if (grade != null) {
            gradeHeaderHolder.getText().setText(getContext().getString(grade.getAssessmentType().getStringResID()));

        }
    }

    static class GradeViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mGrade;
        private final TextView mCourseId;
        private final TextView mTerm;
        private final TextView mCid;
        private final TextView mDate;
        private final View mChipBack;
        private final TextView mChipGrade;
        private final TextView mChipInfo;

        GradeViewHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.grade_list_grade_title);
            mCourseId = itemView.findViewById(R.id.grade_list_grade_courseId);
            mTerm = itemView.findViewById(R.id.grade_list_grade_term);
            mCid = itemView.findViewById(R.id.grade_list_grade_cid);
            mDate = itemView.findViewById(R.id.grade_list_grade_date);
            mGrade = itemView.findViewById(R.id.grade_list_grade_grade);
            mChipBack = itemView.findViewById(R.id.grade_chip);
            mChipInfo = itemView.findViewById(R.id.grade_chip_info);
            mChipGrade = itemView.findViewById(R.id.grade_chip_grade);
        }

        public TextView getTitle() {
            return mTitle;
        }

        public TextView getGrade() {
            return mGrade;
        }

        public TextView getCourseId() {
            return mCourseId;
        }

        public TextView getTerm() {
            return mTerm;
        }

        public TextView getCid() {
            return mCid;
        }

        public TextView getDate() {
            return mDate;
        }

        public View getChipBack() {
            return mChipBack;
        }

        public TextView getChipGrade() {
            return mChipGrade;
        }

        public TextView getChipInfo() {
            return mChipInfo;
        }
    }

    static class GradeHeaderHolder extends RecyclerView.ViewHolder {
        private final TextView mText;

        GradeHeaderHolder(View itemView) {
            super(itemView);

            mText = itemView.findViewById(R.id.list_header_text);
        }

        public TextView getText() {
            return mText;
        }
    }
}
