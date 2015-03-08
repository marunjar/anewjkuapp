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
import org.voidsink.anewjkuapp.utils.UIUtils;

import java.text.DateFormat;

public class GradeListAdapter extends RecyclerArrayAdapter<Assessment, GradeListAdapter.GradeViewHolder> implements StickyRecyclerHeadersAdapter<GradeListAdapter.GradeHeaderHolder> {

    private final Context mContext;

    public GradeListAdapter(Context context) {
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
        Assessment grade = getItem(position);
        holder.mTitle.setText(grade.getTitle());

        UIUtils.setTextAndVisibility(holder.mLvaNr, grade.getLvaNr());
        UIUtils.setTextAndVisibility(holder.mTerm, grade.getTerm());

        if (grade.getSkz() > 0) {
            holder.mSkz.setText(String.format("[%d]", grade.getSkz()));
            holder.mSkz.setVisibility(View.VISIBLE);
        } else {
            holder.mSkz.setVisibility(View.GONE);
        }

        holder.mChipBack.setBackgroundColor(UIUtils.getChipGradeColor(grade));
        holder.mChipGrade.setText(UIUtils.getChipGradeText(grade));
        holder.mChipInfo.setText(UIUtils.getChipGradeEcts(grade.getEcts()));

        holder.mDate.setText(DateFormat.getDateInstance().format(grade.getDate()));
        holder.mGrade.setText(mContext.getString(grade.getGrade()
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
        public final TextView mLvaNr;
        public final TextView mTerm;
        public final TextView mSkz;
        public final TextView mDate;
        public final View mChipBack;
        public final TextView mChipGrade;
        public final TextView mChipInfo;

        public GradeViewHolder(View itemView) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.grade_list_grade_title);
            mLvaNr = (TextView) itemView.findViewById(R.id.grade_list_grade_lvanr);
            mTerm = (TextView) itemView.findViewById(R.id.grade_list_grade_term);
            mSkz = (TextView) itemView.findViewById(R.id.grade_list_grade_skz);
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
