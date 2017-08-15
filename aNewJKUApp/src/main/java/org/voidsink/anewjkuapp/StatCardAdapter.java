/*
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
 *
 */

package org.voidsink.anewjkuapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.kusss.Assessment;
import org.voidsink.anewjkuapp.kusss.AssessmentType;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.LvaState;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.EctsSliceValue;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.AbstractChartData;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.ComboLineColumnChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.AbstractChartView;
import lecho.lib.hellocharts.view.ComboLineColumnChartView;
import lecho.lib.hellocharts.view.PieChartView;

public class StatCardAdapter extends RecyclerArrayAdapter<StatCard, StatCardAdapter.StatViewHolder> {

    private static final int ADDITIONAL_SPACE = 10;

    private final int mTextColorPrimary;
    private final int mTextSizePrimary;
    private final int mTextColorSecondary;
    private final int mTextSizeSecondary;

    public StatCardAdapter(Context context) {
        super(context);

        TypedArray themeArray = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary, android.R.attr.textColorSecondary, android.R.attr.textSize});
        this.mTextColorPrimary = themeArray.getColor(0, Color.BLACK);
        this.mTextColorSecondary = themeArray.getColor(1, Color.DKGRAY);
        this.mTextSizePrimary = (int) themeArray.getDimension(2, 12);
        this.mTextSizeSecondary = this.mTextSizePrimary;
        themeArray.recycle();
    }

    private void initLvaListItems(StatViewHolder holder, StatCard card) {
        List<LvaWithGrade> mLvas = card.getLvasWithGrades();
        List<LvaStatItem> lvaStats = new ArrayList<>();

        LvaStatItem lva = new LvaStatItem(LvaState.OPEN, AppUtils.getECTS(LvaState.OPEN, mLvas));
        if (lva.getEcts() > 0) {
            lvaStats.add(lva);
        }

        lva = new LvaStatItem(LvaState.DONE, AppUtils.getECTS(LvaState.DONE, mLvas));
        if (lva.getEcts() > 0) {
            lvaStats.add(lva);
        }

        lva = new LvaStatItem(LvaState.ALL, AppUtils.getECTS(LvaState.ALL, mLvas));
        if (lva.getEcts() > 0 && lvaStats.size() > 1) {
            lvaStats.add(lva);
        }

        holder.mItems.removeAllViews();

        for (LvaStatItem item : lvaStats) {
            final LayoutInflater mInflater = LayoutInflater.from(getContext());
            View view = mInflater.inflate(R.layout.stat_card_lva_list_entry, null, false);

            TextView type = (TextView) view.findViewById(R.id.stat_card_lva_list_entry_type);
            TextView ects = (TextView) view.findViewById(R.id.stat_card_lva_list_entry_ects);

            type.setText(getContext().getString(item.getType().getStringResID()));
            ects.setText(AppUtils.format(getContext(), "%.2f ECTS", item.getEcts()));

            holder.mItems.addView(view);
        }
    }

    private void updateLvaPlot(StatViewHolder holder, StatCard card) {
        List<LvaWithGrade> mLvas = card.getLvasWithGrades();
        double mOpenEcts = AppUtils.getECTS(LvaState.OPEN, mLvas);
        double mDoneEcts = AppUtils.getECTS(LvaState.DONE, mLvas);
        //double minEcts = (card.getTerms() != null) ? card.getTerms().size() * 30 : 0;

        if (holder.mBarChart.getVisibility() == View.VISIBLE) {
            ComboLineColumnChartData dataSet = new ComboLineColumnChartData();
            initBarChartDataSet(dataSet);

            ArrayList<Column> yVals = new ArrayList<>();
            ArrayList<AxisValue> axisValues = new ArrayList<>();

            // add series to bar chart
            addSerieToBarChart(yVals, axisValues, getContext().getString(LvaState.DONE.getStringResID()),
                    mDoneEcts * 100 / (mOpenEcts + mDoneEcts), mDoneEcts, Grade.G1.getColor());
            addSerieToBarChart(yVals, axisValues, getContext().getString(LvaState.OPEN.getStringResID()),
                    mOpenEcts * 100 / (mOpenEcts + mDoneEcts), mOpenEcts, Grade.G3.getColor());

            Axis xAxis = new Axis(axisValues);
            xAxis.setTextColor(mTextColorPrimary);
            xAxis.setLineColor(mTextColorSecondary);
            Axis yAxis = Axis.generateAxisFromRange(0, 110, 10).setHasLines(true).setName("%");
            yAxis.setTextColor(mTextColorPrimary);
            yAxis.setLineColor(mTextColorSecondary);

            dataSet.setAxisXBottom(xAxis);
            dataSet.setAxisYLeft(yAxis);

            dataSet.setColumnChartData(new ColumnChartData(yVals));

            holder.mBarChart.setComboLineColumnChartData(dataSet);
        }
        if (holder.mPieChart.getVisibility() == View.VISIBLE) {
            ArrayList<SliceValue> slices = new ArrayList<>();
            ArrayList<String> captions = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();

            // add series to pie chart
            addSerieToPieChart(getContext(), slices, captions, colors, getContext().getString(LvaState.DONE.getStringResID()),
                    mDoneEcts * 100 / (mOpenEcts + mDoneEcts), mDoneEcts, null, Grade.G1.getColor());
            addSerieToPieChart(getContext(), slices, captions, colors, getContext().getString(LvaState.OPEN.getStringResID()),
                    mOpenEcts * 100 / (mOpenEcts + mDoneEcts), mOpenEcts, null, Grade.G3.getColor());

            PieChartData dataSet = new PieChartData();
            initPieChartDataSet(dataSet);
            dataSet.setValues(slices);

            holder.mPieChart.setPieChartData(dataSet);
        }
    }

    private void initBarChartDataSet(ComboLineColumnChartData dataSet) {
        initBaseChartDataSet(dataSet);
    }

    private void initPieChartDataSet(PieChartData dataSet) {
        initBaseChartDataSet(dataSet);

        dataSet.setCenterText1Color(mTextColorPrimary);
        dataSet.setCenterText1FontSize(mTextSizePrimary);
        dataSet.setCenterText2Color(mTextColorSecondary);
        dataSet.setCenterText2FontSize(mTextSizeSecondary);
        dataSet.setHasCenterCircle(true);
        dataSet.setHasLabels(true);
        dataSet.setHasLabelsOnlyForSelected(false);
    }

    private void initBaseChartDataSet(AbstractChartData dataSet) {
    }

    private void addSerieToBarChart(List<Column> values, List<AxisValue> axisValues, String category,
                                    double percent, double ects, int color) {

        if (percent > 0) {
            axisValues.add(new AxisValue(axisValues.size()).setLabel(category));

            ArrayList<SubcolumnValue> subColumns = new ArrayList<>();
            SubcolumnValue subColumn = new SubcolumnValue((float) percent, color);
            subColumn.setLabel(AppUtils.format(getContext(), "%.2f ECTS", ects));
            subColumns.add(subColumn);
            Column column = new Column(subColumns);
            column.setHasLabels(false);
            column.setHasLabelsOnlyForSelected(true);
            values.add(column);
        }
    }

    private static void addSerieToPieChart(Context context, List<SliceValue> slices, List<String> captions, List<Integer> colors, String category,
                                           double percent, double ects, Grade grade, int color) {
        if (percent > 0) {
            colors.add(color);

            captions.add(category);

            SliceValue slice = new EctsSliceValue((float) percent, (float) ects, grade, color);
            slice.setLabel(AppUtils.format(context, "%.2f %%", percent));
            slices.add(slice);
        }
    }

    private void initLvaPlot(final StatViewHolder holder) {
        if (PreferenceWrapper.getUseLvaBarChart(getContext())) {
            holder.mBarChart.setVisibility(View.VISIBLE);
            initBarChart(holder.mBarChart);
            holder.mPieChart.setVisibility(View.GONE);
        } else {
            holder.mPieChart.setVisibility(View.VISIBLE);
            initPieChart(holder.mPieChart);
            holder.mBarChart.setVisibility(View.GONE);
        }
    }

    private void initGradeListItems(StatViewHolder holder, StatCard card) {
        List<GradeStatItem> gradeStats = new ArrayList<>();

        GradeStatItem grade = new GradeStatItem(AssessmentType.INTERIM_COURSE_ASSESSMENT, card.getAssessments(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(AssessmentType.FINAL_COURSE_ASSESSMENT, card.getAssessments(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(AssessmentType.RECOGNIZED_COURSE_CERTIFICATE, card.getAssessments(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(AssessmentType.RECOGNIZED_EXAM, card.getAssessments(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(AssessmentType.RECOGNIZED_ASSESSMENT, card.getAssessments(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(AssessmentType.FINAL_EXAM, card.getAssessments(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(AssessmentType.ALL, card.getAssessments(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0 && gradeStats.size() > 1) {
            gradeStats.add(grade);
        }

        if (gradeStats.size() == 0) {
            gradeStats.add(new GradeStatItem(AssessmentType.NONE_AVAILABLE, null, card.isWeighted(), card.isPositiveOnly()));
        }

        holder.mItems.removeAllViews();

        for (GradeStatItem item : gradeStats) {
            final LayoutInflater mInflater = LayoutInflater.from(getContext());

            View view = mInflater.inflate(R.layout.stat_card_grade_list_entry, null, false);
            TextView type = (TextView) view.findViewById(R.id.stat_card_grade_list_entry_type);
            TextView avgGrade = (TextView) view.findViewById(R.id.stat_card_grade_list_entry_grade);

            type.setText(getContext().getString(item.getType().getStringResID()));
            avgGrade.setText(AppUtils.format(getContext(), "ø %.2f", item.getAvgGrade()));

            holder.mItems.addView(view);
        }
    }

    private void updateGradePlot(StatViewHolder holder, StatCard card) {
        if (holder.mBarChart.getVisibility() == View.VISIBLE) {
            ArrayList<Column> yVals = new ArrayList<>();
            ArrayList<AxisValue> axisValues = new ArrayList<>();

            // add series to bar chart
            addSerieToBarChart(yVals, axisValues, getContext().getString(Grade.G1.getStringResID()), AppUtils.getGradePercent(card.getAssessments(), Grade.G1, card.isWeighted()), AppUtils.getGradeEcts(card.getAssessments(), Grade.G1), Grade.G1.getColor());
            addSerieToBarChart(yVals, axisValues, getContext().getString(Grade.G2.getStringResID()), AppUtils.getGradePercent(card.getAssessments(), Grade.G2, card.isWeighted()), AppUtils.getGradeEcts(card.getAssessments(), Grade.G2), Grade.G2.getColor());
            addSerieToBarChart(yVals, axisValues, getContext().getString(Grade.G3.getStringResID()), AppUtils.getGradePercent(card.getAssessments(), Grade.G3, card.isWeighted()), AppUtils.getGradeEcts(card.getAssessments(), Grade.G3), Grade.G3.getColor());
            addSerieToBarChart(yVals, axisValues, getContext().getString(Grade.G4.getStringResID()), AppUtils.getGradePercent(card.getAssessments(), Grade.G4, card.isWeighted()), AppUtils.getGradeEcts(card.getAssessments(), Grade.G4), Grade.G4.getColor());
            if (!card.isPositiveOnly()) {
                addSerieToBarChart(yVals, axisValues, getContext().getString(Grade.G5.getStringResID()), AppUtils.getGradePercent(card.getAssessments(), Grade.G5, card.isWeighted()), AppUtils.getGradeEcts(card.getAssessments(), Grade.G5), Grade.G5.getColor());
            }


            ComboLineColumnChartData dataSet = new ComboLineColumnChartData();
            initBarChartDataSet(dataSet);

            Axis xAxis = new Axis(axisValues);
            xAxis.setTextColor(mTextColorPrimary);
            xAxis.setLineColor(mTextColorSecondary);
            Axis yAxis = Axis.generateAxisFromRange(0, 110, 10).setHasLines(true).setName("%");
            yAxis.setTextColor(mTextColorPrimary);
            yAxis.setLineColor(mTextColorSecondary);

            dataSet.setAxisXBottom(xAxis);
            dataSet.setAxisYLeft(yAxis);

            dataSet.setColumnChartData(new ColumnChartData(yVals));

            holder.mBarChart.setComboLineColumnChartData(dataSet);
        }
        if (holder.mPieChart.getVisibility() == View.VISIBLE) {
            ArrayList<SliceValue> slices = new ArrayList<>();
            ArrayList<String> captions = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();

            // add series to pie chart
            addSerieToPieChart(getContext(), slices, captions, colors,
                    getContext().getString(Grade.G1.getStringResID()),
                    AppUtils.getGradePercent(card.getAssessments(), Grade.G1, card.isWeighted()),
                    AppUtils.getGradeEcts(card.getAssessments(), Grade.G1),
                    Grade.G1,
                    Grade.G1.getColor());
            addSerieToPieChart(getContext(), slices, captions, colors,
                    getContext().getString(Grade.G2.getStringResID()),
                    AppUtils.getGradePercent(card.getAssessments(), Grade.G2, card.isWeighted()),
                    AppUtils.getGradeEcts(card.getAssessments(), Grade.G2),
                    Grade.G2,
                    Grade.G2.getColor());
            addSerieToPieChart(getContext(), slices, captions, colors,
                    getContext().getString(Grade.G3.getStringResID()),
                    AppUtils.getGradePercent(card.getAssessments(), Grade.G3, card.isWeighted()),
                    AppUtils.getGradeEcts(card.getAssessments(), Grade.G3),
                    Grade.G3,
                    Grade.G3.getColor());
            addSerieToPieChart(getContext(), slices, captions, colors,
                    getContext().getString(Grade.G4.getStringResID()),
                    AppUtils.getGradePercent(card.getAssessments(), Grade.G4, card.isWeighted()),
                    AppUtils.getGradeEcts(card.getAssessments(), Grade.G4),
                    Grade.G4,
                    Grade.G4.getColor());
            if (!card.isPositiveOnly()) {
                addSerieToPieChart(getContext(), slices, captions, colors,
                        getContext().getString(Grade.G5.getStringResID()),
                        AppUtils.getGradePercent(card.getAssessments(), Grade.G5, card.isWeighted()),
                        AppUtils.getGradeEcts(card.getAssessments(), Grade.G5),
                        Grade.G5,
                        Grade.G5.getColor());
            }

            PieChartData dataSet = new PieChartData();
            initPieChartDataSet(dataSet);
            dataSet.setValues(slices);

            holder.mPieChart.setPieChartData(dataSet);
        }
    }

    private void initGradePlot(final StatViewHolder holder) {
        if (PreferenceWrapper.getUseLvaBarChart(getContext())) {
            holder.mBarChart.setVisibility(View.VISIBLE);
            initBarChart(holder.mBarChart);
            holder.mPieChart.setVisibility(View.GONE);
        } else {
            holder.mPieChart.setVisibility(View.VISIBLE);
            initPieChart(holder.mPieChart);
            holder.mBarChart.setVisibility(View.GONE);
        }
    }

    private void initBarChart(ComboLineColumnChartView barChart) {
        initBaseChart(barChart);
    }

    private void initPieChart(final PieChartView pieChart) {
        initBaseChart(pieChart);

        pieChart.setChartRotationEnabled(false);
        pieChart.setChartRotation(180, false);

        pieChart.setOnValueTouchListener(new PieChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int arcIndex, SliceValue value) {
                SliceValue slice = pieChart.getPieChartData().getValues().get(arcIndex);
                if (slice instanceof EctsSliceValue) {
                    pieChart.getPieChartData().setCenterText1(AppUtils.format(getContext(), "%.2f ECTS", ((EctsSliceValue) slice).getEcts()));
                    Grade grade = ((EctsSliceValue) slice).getGrade();
                    if (grade != null) {
                        pieChart.getPieChartData().setCenterText2(getContext().getString(grade.getStringResID()));
                    }
                }
            }

            @Override
            public void onValueDeselected() {
                pieChart.getPieChartData().setCenterText1(null);
                pieChart.getPieChartData().setCenterText2(null);
            }
        });
    }

    private void initBaseChart(AbstractChartView chart) {
        chart.setScrollEnabled(false);
        chart.setZoomEnabled(false);
        chart.setValueSelectionEnabled(true);
    }

    @Override
    public StatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stat_card, parent, false);
        return new StatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(StatViewHolder holder, int position) {
        StatCard item = getItem(position);
        switch (getItemViewType(position)) {
            case StatCard.TYPE_GRADE: {
                if (item.isWeighted()) {
                    holder.mTitle.setText(getContext().getString(R.string.stat_title_grade_weighted));
                } else {
                    holder.mTitle.setText(getContext().getString(R.string.stat_title_grade));
                }

                initGradeListItems(holder, item);
                initGradePlot(holder);
                updateGradePlot(holder, item);

                break;
            }
            case StatCard.TYPE_LVA: {
                holder.mTitle.setText(getContext().getString(R.string.stat_title_lva));

                initLvaListItems(holder, item);
                initLvaPlot(holder);
                updateLvaPlot(holder, item);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).getType();
    }

    private static class LvaStatItem {

        private final LvaState mType;
        private final double mEcts;

        public LvaStatItem(LvaState type, double ects) {
            this.mType = type;
            this.mEcts = ects;
        }

        public LvaState getType() {
            return mType;
        }

        public double getEcts() {
            return mEcts;
        }
    }

    private static class GradeStatItem {
        private final AssessmentType mType;
        private final double mAvgGrade;

        public GradeStatItem(AssessmentType type, List<Assessment> grades, boolean isWeighted, boolean positiveOnly) {
            this.mType = type;
            this.mAvgGrade = AppUtils.getAvgGrade(grades, isWeighted, type, positiveOnly);
        }

        public AssessmentType getType() {
            return mType;
        }

        public double getAvgGrade() {
            return mAvgGrade;
        }
    }

    public static class StatViewHolder extends RecyclerView.ViewHolder {

        public final Toolbar mToolbar;
        public final TextView mTitle;
        public final LinearLayout mItems;
        public final ComboLineColumnChartView mBarChart;
        public final PieChartView mPieChart;

        public StatViewHolder(View itemView) {
            super(itemView);

            mToolbar = (Toolbar) itemView.findViewById(R.id.stat_card_toolbar);
            mTitle = (TextView) itemView.findViewById(R.id.stat_card_title);
            mItems = (LinearLayout) itemView.findViewById(R.id.stat_card_items);
            mBarChart = (ComboLineColumnChartView) itemView.findViewById(R.id.stat_card_diagram_bar);
            mPieChart = (PieChartView) itemView.findViewById(R.id.stat_card_diagram_pie);
        }
    }
}
