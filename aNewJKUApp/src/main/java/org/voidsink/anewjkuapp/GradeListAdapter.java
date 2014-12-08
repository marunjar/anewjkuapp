package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.base.GridWithHeaderAdapter;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.utils.UIUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class GradeListAdapter extends GridWithHeaderAdapter<ExamGrade> {

    private static final DateFormat df = SimpleDateFormat.getDateInstance();
    private LayoutInflater inflater;

    public GradeListAdapter(Context context) {
        super(context, R.layout.grade_list_item);

        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ExamGrade item = this.getItem(position);
        if (item == null) {
            return null;
        }
        return getGradeView(convertView, parent, item);
    }

    private View getGradeView(View convertView, ViewGroup parent,
                              ExamGrade item) {
        GradeListGradeHolder gradeItemHolder = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grade_list_item, parent,
                    false);
            gradeItemHolder = new GradeListGradeHolder();
            gradeItemHolder.title = (TextView) convertView
                    .findViewById(R.id.grade_list_grade_title);
            gradeItemHolder.lvaNr = (TextView) convertView
                    .findViewById(R.id.grade_list_grade_lvanr);
            gradeItemHolder.term = (TextView) convertView
                    .findViewById(R.id.grade_list_grade_term);
            gradeItemHolder.skz = (TextView) convertView
                    .findViewById(R.id.grade_list_grade_skz);
            gradeItemHolder.date = (TextView) convertView
                    .findViewById(R.id.grade_list_grade_date);
            gradeItemHolder.grade = (TextView) convertView
                    .findViewById(R.id.grade_list_grade_grade);
            gradeItemHolder.chipBack = (View) convertView
                    .findViewById(R.id.grade_chip);
            gradeItemHolder.chipInfo = (TextView) convertView.findViewById(R.id.grade_chip_info);
            gradeItemHolder.chipGrade = (TextView) convertView.findViewById(R.id.grade_chip_grade);

            convertView.setTag(gradeItemHolder);
        }

        if (gradeItemHolder == null) {
            gradeItemHolder = (GradeListGradeHolder) convertView.getTag();
        }

        gradeItemHolder.title.setText(item.getTitle());

        UIUtils.setTextAndVisibility(gradeItemHolder.lvaNr, item.getLvaNr());
        UIUtils.setTextAndVisibility(gradeItemHolder.term, item.getTerm());

        if (item.getSkz() > 0) {
            gradeItemHolder.skz.setText(String.format("[%d]", item.getSkz()));
            gradeItemHolder.skz.setVisibility(View.VISIBLE);
        } else {
            gradeItemHolder.skz.setVisibility(View.GONE);
        }

        gradeItemHolder.chipBack.setBackgroundColor(UIUtils.getChipGradeColor(item));
        gradeItemHolder.chipGrade.setText(UIUtils.getChipGradeText(item));
        gradeItemHolder.chipInfo.setText(UIUtils.getChipGradeEcts(item.getEcts()));

        final DateFormat df = DateFormat.getDateInstance();

        gradeItemHolder.date.setText(df.format(item.getDate()));
        gradeItemHolder.grade.setText(getContext().getString(item.getGrade()
                .getStringResID()));

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
        // Build your custom HeaderView
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        final View headerView = mInflater.inflate(R.layout.list_header, null);
        final TextView tvHeaderTitle = (TextView) headerView.findViewById(R.id.list_header_text);

        ExamGrade grade = getItem(position);
        if (grade != null) {
            tvHeaderTitle.setText(getContext().getString(grade.getGradeType().getStringResID()));

        }
        return headerView;
    }

    @Override
    public long getHeaderId(int position) {
        ExamGrade grade = getItem(position);
        if (grade != null) {
            return grade.getGradeType().getStringResID();
        }
        return 0;
    }

    private static class GradeListGradeHolder {
        public TextView grade;
        public TextView date;
        public TextView term;
        public View chipBack;
        public TextView chipInfo;
        public TextView chipGrade;
        private TextView title;
        private TextView lvaNr;
        private TextView skz;
    }
}
