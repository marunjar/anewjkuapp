package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.base.ListWithHeaderAdapter;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.utils.UIUtils;

public class LvaListAdapter extends ListWithHeaderAdapter<LvaWithGrade> {

    public LvaListAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LvaWithGrade item = this.getItem(position);
        if (item == null) {
            return null;
        }
        return getLvaView(convertView, parent, item);
    }

    public View getLvaView(View convertView, ViewGroup parent,
                           LvaWithGrade item) {
        LvaWithGrade lva = item;
        LvaList2ItemHolder lvaHolder = null;

        if (convertView == null) {
            final LayoutInflater mInflater = LayoutInflater.from(getContext());
            convertView = mInflater.inflate(R.layout.lva_list_item, parent,
                    false);
            lvaHolder = new LvaList2ItemHolder();

            lvaHolder.title = (TextView) convertView.findViewById(R.id.lva_list2_item_title);
            lvaHolder.lvaNr = (TextView) convertView.findViewById(R.id.lva_list2_item_lvanr);
            lvaHolder.skz = (TextView) convertView.findViewById(R.id.lva_list2_item_skz);
            lvaHolder.code = (TextView) convertView.findViewById(R.id.lva_list2_item_code);
            lvaHolder.teacher = (TextView) convertView.findViewById(R.id.lva_list2_item_teacher);

            lvaHolder.chipBack = (View) convertView.findViewById(R.id.grade_chip);
            lvaHolder.chipEcts = (TextView) convertView.findViewById(R.id.grade_chip_info);
            lvaHolder.chipGrade = (TextView) convertView.findViewById(R.id.grade_chip_grade);

            convertView.setTag(lvaHolder);
        }

        if (lvaHolder == null) {
            lvaHolder = (LvaList2ItemHolder) convertView.getTag();
        }

        lvaHolder.title.setText(lva.getLva().getTitle());
        UIUtils.setTextAndVisibility(lvaHolder.teacher, lva.getLva().getTeacher());
        lvaHolder.lvaNr.setText(lva.getLva().getLvaNr());
        lvaHolder.skz.setText(String.format("[%s]", lva.getLva().getSKZ()));
        lvaHolder.code.setText(lva.getLva().getCode());

        ExamGrade grade = lva.getGrade();
        lvaHolder.chipBack.setBackgroundColor(UIUtils.getChipGradeColor(grade));
        lvaHolder.chipGrade.setText(UIUtils.getChipGradeText(grade));
        lvaHolder.chipEcts.setText(UIUtils.getChipGradeEcts(lva.getLva().getEcts()));

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

    private static class LvaList2ItemHolder {
        private TextView title;
        private TextView lvaNr;
        private TextView skz;
        private TextView code;
        private TextView teacher;
        private View chipBack;
        private TextView chipEcts;
        private TextView chipGrade;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
        // Build your custom HeaderView
        final LayoutInflater mInflater = LayoutInflater.from(getContext());
        final View headerView = mInflater.inflate(R.layout.list_header, null);
        final TextView tvHeaderTitle = (TextView) headerView.findViewById(R.id.list_header_text);

        LvaWithGrade lva = getItem(position);
        if (lva != null) {
            tvHeaderTitle.setText(getContext().getString(lva.getState().getStringResID()));
        }
        return headerView;
    }

    @Override
    public long getHeaderId(int position) {
        LvaWithGrade lva = getItem(position);
        if (lva != null) {
            return lva.getState().getStringResID();
        }
        return 0;
    }

}
