/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2023 Paul "Marunjar" Pretsch
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

import com.google.android.material.appbar.MaterialToolbar;

import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.base.SimpleTextViewHolder;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.UIUtils;
import org.voidsink.sectionedrecycleradapter.SectionedAdapter;

import java.text.DateFormat;
import java.util.Calendar;

public class ExamListAdapter extends RecyclerArrayAdapter<ExamListExam, ExamListAdapter.ExamViewHolder> implements SectionedAdapter<SimpleTextViewHolder> {

    public ExamListAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.exam_list_item, parent, false);

        return new ExamViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        final ExamListExam exam = getItem(ExamListExam.class, position);

        if (exam != null) {
            holder.getToolbar().setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.menu_exam_register) {
                    KusssHelper.showExamInBrowser(getContext(), exam.getCourseId());
                    return true;
                }
                return false;
            });

            holder.getTitle().setText(exam.getTitle());
            UIUtils.setTextAndVisibility(holder.getDescription(), exam.getDescription());
            UIUtils.setTextAndVisibility(holder.getInfo(), exam.getInfo());
            holder.getCourseId().setText(exam.getCourseId());
            UIUtils.setTextAndVisibility(holder.getTerm(), AppUtils.termToString(exam.getTerm()));

            if (exam.getCid() > 0) {
                holder.getCid().setText(AppUtils.format(getContext(), "[%d]", exam.getCid()));
                holder.getCid().setVisibility(View.VISIBLE);
            } else {
                holder.getCid().setVisibility(View.GONE);
            }
            holder.getTime().setText(AppUtils.getTimeString(getContext(), exam.getDtStart(), exam.getDtEnd(), false));
            holder.getLocation().setText(exam.getLocation());
        }
    }

    @Override
    public long getHeaderId(int position) {
        ExamListExam exam = getItem(ExamListExam.class, position);

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
    public SimpleTextViewHolder onCreateHeaderViewHolder(@NonNull ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_header, viewGroup, false);
        return new SimpleTextViewHolder(v, R.id.list_header_text);
    }

    @Override
    public void onBindHeaderViewHolder(SimpleTextViewHolder sectionViewHolder, int position) {
        ExamListExam exam = getItem(ExamListExam.class, position);

        if (exam != null) {
            sectionViewHolder.getText().setText(DateFormat.getDateInstance().format(exam.getDtStart()));
        } else {
            sectionViewHolder.getText().setText("");
        }
    }

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        private final MaterialToolbar mToolbar;
        private final TextView mLocation;
        private final TextView mTime;
        private final TextView mCid;
        private final TextView mTerm;
        private final TextView mCourseId;
        private final TextView mInfo;
        private final TextView mDescription;
        private final TextView mTitle;

        ExamViewHolder(View itemView) {
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
        }

        public MaterialToolbar getToolbar() {
            return mToolbar;
        }

        public TextView getLocation() {
            return mLocation;
        }

        public TextView getTime() {
            return mTime;
        }

        public TextView getCid() {
            return mCid;
        }

        public TextView getTerm() {
            return mTerm;
        }

        public TextView getCourseId() {
            return mCourseId;
        }

        public TextView getInfo() {
            return mInfo;
        }

        public TextView getDescription() {
            return mDescription;
        }

        public TextView getTitle() {
            return mTitle;
        }
    }
}
