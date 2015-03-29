/*******************************************************************************
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
 ******************************************************************************/

package org.voidsink.anewjkuapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.UIUtils;

import java.text.DateFormat;

public class AssessmentListAdapter extends RecyclerArrayAdapter<Assessment, AssessmentListAdapter.GradeViewHolder> implements StickyRecyclerHeadersAdapter<AssessmentListAdapter.GradeHeaderHolder> {

    private final Context mContext;

    public AssessmentListAdapter(Context context) {
        super();
        this.mContext = context;
    }

    @Override
    public GradeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grade_list_item, parent, false);
        return new GradeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(GradeViewHolder holder, int position) {
        Assessment assessment = getItem(position);
        holder.mTitle.setText(assessment.getTitle());

        UIUtils.setTextAndVisibility(holder.mCourseId, assessment.getCourseId());
        UIUtils.setTextAndVisibility(holder.mTerm, AppUtils.termToString(assessment.getTerm()));

        if (assessment.getCid() > 0) {
            holder.mCid.setText(String.format("[%d]", assessment.getCid()));
            holder.mCid.setVisibility(View.VISIBLE);
        } else {
            holder.mCid.setVisibility(View.GONE);
        }

        holder.mChipBack.setBackgroundColor(UIUtils.getChipGradeColor(assessment));
        holder.mChipGrade.setText(UIUtils.getChipGradeText(assessment));
        holder.mChipInfo.setText(UIUtils.getChipGradeEcts(assessment.getEcts()));

        holder.mDate.setText(DateFormat.getDateInstance().format(assessment.getDate()));
        holder.mGrade.setText(mContext.getString(assessment.getGrade()
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
    public GradeHeaderHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_header, viewGroup, false);
        return new GradeHeaderHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(GradeHeaderHolder gradeHeaderHolder, int position) {
        Assessment grade = getItem(position);
        if (grade != null) {
            gradeHeaderHolder.mText.setText(mContext.getString(grade.getAssessmentType().getStringResID()));

        }
    }

    public static class GradeViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTitle;
        public final TextView mGrade;
        public final TextView mCourseId;
        public final TextView mTerm;
        public final TextView mCid;
        public final TextView mDate;
        public final View mChipBack;
        public final TextView mChipGrade;
        public final TextView mChipInfo;

        public GradeViewHolder(View itemView) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.grade_list_grade_title);
            mCourseId = (TextView) itemView.findViewById(R.id.grade_list_grade_courseId);
            mTerm = (TextView) itemView.findViewById(R.id.grade_list_grade_term);
            mCid = (TextView) itemView.findViewById(R.id.grade_list_grade_cid);
            mDate = (TextView) itemView.findViewById(R.id.grade_list_grade_date);
            mGrade = (TextView) itemView.findViewById(R.id.grade_list_grade_grade);
            mChipBack = itemView.findViewById(R.id.grade_chip);
            mChipInfo = (TextView) itemView.findViewById(R.id.grade_chip_info);
            mChipGrade = (TextView) itemView.findViewById(R.id.grade_chip_grade);
        }
    }

    protected static class GradeHeaderHolder extends RecyclerView.ViewHolder {
        public final TextView mText;

        public GradeHeaderHolder(View itemView) {
            super(itemView);

            mText = (TextView) itemView.findViewById(R.id.list_header_text);
        }
    }
}
