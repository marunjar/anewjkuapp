/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2017 Paul "Marunjar" Pretsch
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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.UIUtils;
import org.voidsink.sectionedrecycleradapter.SectionedAdapter;

import java.text.DateFormat;
import java.util.Calendar;

public class ExamListAdapter extends RecyclerArrayAdapter<ExamListExam, ExamListAdapter.ExamViewHolder> implements SectionedAdapter<ExamListAdapter.DateHeaderHolder> {

    public ExamListAdapter(Context context) {
        super(context);
    }

    @Override
    public ExamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.exam_list_item, parent, false);

        return new ExamViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ExamViewHolder holder, int position) {
        final ExamListExam exam = getItem(position);

        if (exam != null) {
            holder.mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.menu_exam_register: {
                            KusssHelper.showExamInBrowser(getContext(), exam.getCourseId());
                            return true;
                        }
                    }
                    return false;
                }
            });

            holder.mTitle.setText(exam.getTitle());
            UIUtils.setTextAndVisibility(holder.mDescription, exam.getDescription());
            UIUtils.setTextAndVisibility(holder.mInfo, exam.getInfo());
            holder.mCourseId.setText(exam.getCourseId());
            UIUtils.setTextAndVisibility(holder.mTerm, AppUtils.termToString(exam.getTerm()));

            if (exam.getCid() > 0) {
                holder.mCid.setText(AppUtils.format(getContext(),"[%d]", exam.getCid()));
                holder.mCid.setVisibility(View.VISIBLE);
            } else {
                holder.mCid.setVisibility(View.GONE);
            }
            holder.mTime.setText(AppUtils.getTimeString(getContext(), exam.getDtStart(), exam.getDtEnd(), false));
            holder.mLocation.setText(exam.getLocation());
        }
    }

    @Override
    public long getHeaderId(int position) {
        ExamListExam exam = getItem(position);

        if (exam != null) {
            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTime(exam.getDtStart());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        }
        return 0;
    }

    @Override
    public DateHeaderHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_header, viewGroup, false);
        return new DateHeaderHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(DateHeaderHolder dateHeaderHolder, int position) {
        ExamListExam exam = getItem(position);

        if (exam != null) {
            dateHeaderHolder.mText.setText(DateFormat.getDateInstance().format(exam.getDtStart()));
        } else {
            dateHeaderHolder.mText.setText("");
        }
    }

    public static class ExamViewHolder extends RecyclerView.ViewHolder {
        public final Toolbar mToolbar;
        public final View mChip;
        public final TextView mLocation;
        public final TextView mTime;
        public final TextView mCid;
        public final TextView mTerm;
        public final TextView mCourseId;
        public final TextView mInfo;
        public final TextView mDescription;
        public final TextView mTitle;

        public ExamViewHolder(View itemView) {
            super(itemView);

            mToolbar = itemView.findViewById(R.id.exam_list_item_toolbar);
            mToolbar.inflateMenu(R.menu.exam_card_popup_menu);

            mTitle = itemView.findViewById(R.id.exam_list_item_title);
            mDescription = itemView.findViewById(R.id.exam_list_item_description);
            mInfo = itemView.findViewById(R.id.exam_list_item_info);
            mCourseId = itemView.findViewById(R.id.exam_list_item_courseId);
            mTerm = itemView.findViewById(R.id.exam_list_item_term);
            mCid = itemView.findViewById(R.id.exam_list_item_cid);
            mTime = itemView.findViewById(R.id.exam_list_item_time);
            mLocation = itemView.findViewById(R.id.exam_list_item_location);
            mChip = itemView.findViewById(R.id.empty_chip_background);
        }
    }

    protected static class DateHeaderHolder extends RecyclerView.ViewHolder {
        public final TextView mText;

        public DateHeaderHolder(View itemView) {
            super(itemView);

            mText = itemView.findViewById(R.id.list_header_text);
        }
    }

}
