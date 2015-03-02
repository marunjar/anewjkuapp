package org.voidsink.anewjkuapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.utils.UIUtils;

public class LvaListAdapter extends RecyclerArrayAdapter<LvaWithGrade, LvaListAdapter.LvaViewHolder> implements StickyRecyclerHeadersAdapter<LvaListAdapter.LvaHeaderHolder> {

    private final Context mContext;

    public LvaListAdapter(Context context) {
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

        holder.mTitle.setText(lva.getLva().getTitle());
        UIUtils.setTextAndVisibility(holder.mTeacher, lva.getLva().getTeacher());
        holder.mLvaNr.setText(lva.getLva().getLvaNr());
        if (lva.getLva().getSKZ() > 0) {
            holder.mSkz.setText(String.format("[%d]", lva.getLva().getSKZ()));
            holder.mSkz.setVisibility(View.VISIBLE);
        } else {
            holder.mSkz.setVisibility(View.GONE);
        }
        holder.mCode.setText(lva.getLva().getCode());

        ExamGrade grade = lva.getGrade();
        holder.mChipBack.setBackgroundColor(UIUtils.getChipGradeColor(grade));
        holder.mChipGrade.setText(UIUtils.getChipGradeText(grade));
        holder.mChipEcts.setText(UIUtils.getChipGradeEcts(lva.getLva().getEcts()));
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
        private final TextView mLvaNr;
        private final TextView mCode;
        private final TextView mSkz;
        private final TextView mTeacher;
        private final View mChipBack;
        private final TextView mChipEcts;
        private final TextView mChipGrade;

        public LvaViewHolder(View itemView) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.lva_list2_item_title);
            mLvaNr = (TextView) itemView.findViewById(R.id.lva_list2_item_lvanr);
            mSkz = (TextView) itemView.findViewById(R.id.lva_list2_item_skz);
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
