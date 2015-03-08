package org.voidsink.anewjkuapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.text.DateFormat;
import java.util.Calendar;

public class ExamListAdapter extends RecyclerArrayAdapter<ExamListExam, ExamListAdapter.ExamViewHolder> implements StickyRecyclerHeadersAdapter<ExamListAdapter.DateHeaderHolder> {

    private final Context mContext;

    public ExamListAdapter(Context context) {
        super();

        this.mContext = context;
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
                            KusssHandler.getInstance().showExamInBrowser(mContext, exam.getLvaNr());
                            return true;
                        }
                    }
                    return false;
                }
            });

            holder.mTitle.setText(exam.getTitle());
            if (!exam.getDescription().isEmpty()) {
                holder.mDescription.setText(exam.getDescription());
                holder.mDescription.setVisibility(View.VISIBLE);
            } else {
                holder.mDescription.setVisibility(View.GONE);
            }
            if (!exam.getInfo().isEmpty()) {
                holder.mInfo.setText(exam.getInfo());
                holder.mInfo.setVisibility(View.VISIBLE);
            } else {
                holder.mInfo.setVisibility(View.GONE);
            }
            holder.mLvaNr.setText(exam.getLvaNr());
            holder.mTerm.setText(exam.getTerm());

            if (exam.getSkz() > 0) {
                holder.mSkz.setText(String.format("[%d]", exam.getSkz()));
                holder.mSkz.setVisibility(View.VISIBLE);
            } else {
                holder.mSkz.setVisibility(View.GONE);
            }
            holder.mTime.setText(AppUtils.getTimeString(exam.getDtStart(), exam.getDtEnd()));
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
        public final TextView mSkz;
        public final TextView mTerm;
        public final TextView mLvaNr;
        public final TextView mInfo;
        public final TextView mDescription;
        public final TextView mTitle;

        public ExamViewHolder(View itemView) {
            super(itemView);

            mToolbar = (Toolbar) itemView.findViewById(R.id.exam_list_item_toolbar);
            mToolbar.inflateMenu(R.menu.exam_card_popup_menu);

            mTitle = (TextView) itemView.findViewById(R.id.exam_list_item_title);
            mDescription = (TextView) itemView.findViewById(R.id.exam_list_item_description);
            mInfo = (TextView) itemView.findViewById(R.id.exam_list_item_info);
            mLvaNr = (TextView) itemView.findViewById(R.id.exam_list_item_lvanr);
            mTerm = (TextView) itemView.findViewById(R.id.exam_list_item_term);
            mSkz = (TextView) itemView.findViewById(R.id.exam_list_item_skz);
            mTime = (TextView) itemView.findViewById(R.id.exam_list_item_time);
            mLocation = (TextView) itemView.findViewById(R.id.exam_list_item_location);
            mChip = itemView.findViewById(R.id.empty_chip_background);
        }
    }

    protected static class DateHeaderHolder extends RecyclerView.ViewHolder {
        public final TextView mText;

        public DateHeaderHolder(View itemView) {
            super(itemView);

            mText = (TextView) itemView.findViewById(R.id.list_header_text);
        }
    }

}
