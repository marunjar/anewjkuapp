package org.voidsink.anewjkuapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.interfaces.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Legend;
import com.github.mikephil.charting.utils.LimitLine;
import com.github.mikephil.charting.utils.XLabels;
import com.github.mikephil.charting.utils.YLabels;

import org.voidsink.anewjkuapp.base.BaseArrayAdapter;
import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.GradeType;
import org.voidsink.anewjkuapp.kusss.LvaState;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.EctsEntry;

import java.util.ArrayList;
import java.util.List;

public class StatCardAdapter extends RecyclerArrayAdapter<StatCard, StatCardAdapter.StatViewHolder> {

    private static final int ANIMATION_DURATION = 1250;

    private final int mBackgroundColor;
    private final int mTextColorPrimary;
    private final int mTextColorSecondary;
    private final Context mContext;

    public StatCardAdapter(Context context) {
        super();
        this.mContext = context;

        TypedArray themeArray = context.getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowBackground, android.R.attr.textColorPrimary, android.R.attr.textColorSecondary});
        this.mBackgroundColor = themeArray.getColor(0, Color.WHITE);
        this.mTextColorPrimary = themeArray.getColor(1, Color.BLACK);
        this.mTextColorSecondary = themeArray.getColor(2, Color.DKGRAY);
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
            final LayoutInflater mInflater = LayoutInflater.from(mContext);
            View view = mInflater.inflate(R.layout.stat_card_lva_list_entry, null, false);

            TextView type = (TextView) view.findViewById(R.id.stat_card_lva_list_entry_type);
            TextView ects = (TextView) view.findViewById(R.id.stat_card_lva_list_entry_ects);

            type.setText(mContext.getString(item.getType().getStringResIDExt()));
            ects.setText(String.format("%.2f ECTS", item.getEcts()));

            holder.mItems.addView(view);
        }
    }

    private void updateLvaPlot(StatViewHolder holder, StatCard card) {
        List<LvaWithGrade> mLvas = card.getLvasWithGrades();
        double mOpenEcts = AppUtils.getECTS(LvaState.OPEN, mLvas);
        double mDoneEcts = AppUtils.getECTS(LvaState.DONE, mLvas);
        double minEcts = (card.getTerms() != null) ? card.getTerms().size() * 30 : 0;

        if (holder.mBarChart.getVisibility() == View.VISIBLE) {
            // clear chart
            holder.mBarChart.clear();

            // calculate range
            double rangeTopMax = ((card.getTerms() != null) ? card.getTerms().size() : 1) * 30;
            if ((mDoneEcts + mOpenEcts) > (rangeTopMax - 5)) {
                if ((mDoneEcts + mOpenEcts) > rangeTopMax) {
                    rangeTopMax = (Math.ceil((mDoneEcts + mOpenEcts) / 10) * 10) + 5;
                } else {
                    rangeTopMax = rangeTopMax + 5;
                }
            }
            if (rangeTopMax <= minEcts) {
                rangeTopMax = minEcts + 5;
            }

            holder.mBarChart.setYRange(0, (float) rangeTopMax, true);

            ArrayList<BarEntry> yVals = new ArrayList<>();
            ArrayList<String> captions = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();

            // add series to bar chart
            addSerieToBarChart(yVals, captions, colors, mContext.getString(LvaState.DONE.getStringResIDExt()),
                    mDoneEcts, Grade.G1.getColor());
            addSerieToBarChart(yVals, captions, colors, mContext.getString(LvaState.OPEN.getStringResIDExt()),
                    mOpenEcts, Grade.G3.getColor());

            BarDataSet dataSet = new BarDataSet(yVals, "");
            dataSet.setColors(colors);
            BarData barData = new BarData(captions, dataSet);

            if (minEcts > 0) {
                LimitLine limitLine = new LimitLine((float) minEcts);
                limitLine.setDrawValue(true);
                limitLine.setLineColor(Color.RED);
                limitLine.enableDashedLine(20, 10, 0);
                limitLine.setLineWidth(2);
                limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT);
                barData.addLimitLine(limitLine);
            }

            holder.mBarChart.setData(barData);
            // undo all highlights
            holder.mBarChart.highlightValues(null);
            holder.mBarChart.getLegend().setTextColor(mTextColorPrimary);
            holder.mBarChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
            holder.mBarChart.animateY(ANIMATION_DURATION);
        }
        if (holder.mPieChart.getVisibility() == View.VISIBLE) {
            // clear chart
            holder.mPieChart.clear();

            ArrayList<Entry> yVals = new ArrayList<>();
            ArrayList<String> captions = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();

            // add series to pie chart
            AppUtils.addSerieToPieChart(yVals, captions, colors, mContext.getString(LvaState.DONE.getStringResIDExt()),
                    mDoneEcts, mDoneEcts, Grade.G1.getColor());
            AppUtils.addSerieToPieChart(yVals, captions, colors, mContext.getString(LvaState.OPEN.getStringResIDExt()),
                    mOpenEcts, mOpenEcts, Grade.G3.getColor());

            PieDataSet dataSet = new PieDataSet(yVals, "");
            dataSet.setColors(colors);
            PieData pieData = new PieData(captions, dataSet);

            holder.mPieChart.setData(pieData);
            // undo all highlights
            holder.mPieChart.highlightValues(null);
            holder.mPieChart.getLegend().setTextColor(mTextColorPrimary);
            holder.mPieChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
            holder.mPieChart.animateXY(ANIMATION_DURATION, ANIMATION_DURATION);
        }
    }

    private void addSerieToBarChart(List<BarEntry> values, List<String> captions, List<Integer> colors, String category,
                                    double value, int color) {
        if (value > 0) {
            values.add(new BarEntry((float)value, values.size()));
            captions.add(category);
            colors.add(color);
        }
    }

    private void initLvaPlot(final StatViewHolder holder) {
        if (PreferenceWrapper.getUseLvaBarChart(mContext)) {
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

        GradeStatItem grade = new GradeStatItem(GradeType.INTERIM_COURSE_ASSESSMENT, card.getGrades(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(GradeType.FINAL_COURSE_ASSESSMENT, card.getGrades(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(GradeType.RECOGNIZED_COURSE_CERTIFICATE, card.getGrades(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(GradeType.RECOGNIZED_EXAM, card.getGrades(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(GradeType.RECOGNIZED_ASSESSMENT, card.getGrades(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(GradeType.FINAL_EXAM, card.getGrades(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(GradeType.ALL, card.getGrades(), card.isWeighted(), card.isPositiveOnly());
        if (grade.getAvgGrade() > 0 && gradeStats.size() > 1) {
            gradeStats.add(grade);
        }

        if (gradeStats.size() == 0) {
            gradeStats.add(new GradeStatItem(GradeType.NONE_AVAILABLE, null, card.isWeighted(), card.isPositiveOnly()));
        }

        holder.mItems.removeAllViews();

        for (GradeStatItem item : gradeStats) {
            final LayoutInflater mInflater = LayoutInflater.from(mContext);

            View view = mInflater.inflate(R.layout.stat_card_grade_list_entry, null, false);
            TextView type = (TextView) view.findViewById(R.id.stat_card_grade_list_entry_type);
            TextView avgGrade = (TextView) view.findViewById(R.id.stat_card_grade_list_entry_grade);

            type.setText(mContext.getString(item.getType().getStringResID()));
            avgGrade.setText(String.format("ø %.2f", item.getAvgGrade()));

            holder.mItems.addView(view);
        }
    }

    private void updateGradePlot(StatViewHolder holder, StatCard card) {
        if (holder.mBarChart.getVisibility() == View.VISIBLE) {
            // clear chart
            holder.mBarChart.clear();

            ArrayList<BarEntry> yVals = new ArrayList<>();
            ArrayList<String> captions = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();

            // add series to bar chart
            addSerieToBarChart(yVals, captions, colors, mContext.getString(Grade.G1.getStringResID()), AppUtils.getGradePercent(card.getGrades(), Grade.G1, card.isWeighted()), Grade.G1.getColor());
            addSerieToBarChart(yVals, captions, colors, mContext.getString(Grade.G2.getStringResID()), AppUtils.getGradePercent(card.getGrades(), Grade.G2, card.isWeighted()), Grade.G2.getColor());
            addSerieToBarChart(yVals, captions, colors, mContext.getString(Grade.G3.getStringResID()), AppUtils.getGradePercent(card.getGrades(), Grade.G3, card.isWeighted()), Grade.G3.getColor());
            addSerieToBarChart(yVals, captions, colors, mContext.getString(Grade.G4.getStringResID()), AppUtils.getGradePercent(card.getGrades(), Grade.G4, card.isWeighted()), Grade.G4.getColor());
            if (!card.isPositiveOnly()) {
                addSerieToBarChart(yVals, captions, colors, mContext.getString(Grade.G5.getStringResID()), AppUtils.getGradePercent(card.getGrades(), Grade.G5, card.isWeighted()), Grade.G5.getColor());
            }

            // calculate range
            double rangeTopMax = 0;
            // find max %
            for (Entry n : yVals) {
                if (n != null) {
                    rangeTopMax = Math.max(rangeTopMax, n.getVal());
                }
            }

            if (rangeTopMax > 0) {
                // add some free space
                rangeTopMax = (Math.ceil(rangeTopMax / 10) * 10) + 5;
            } else {
                // default 25%
                rangeTopMax = 25;
            }

            // max 100%
            if (rangeTopMax > 100) {
                rangeTopMax = 100;
            }

            holder.mBarChart.setYRange(0, (float)rangeTopMax, true);

            BarDataSet dataSet = new BarDataSet(yVals, "");
            dataSet.setColors(colors);
            BarData barData = new BarData(captions, dataSet);

            holder.mBarChart.setData(barData);
            // undo all highlights
            holder.mBarChart.highlightValues(null);
            holder.mBarChart.getLegend().setTextColor(mTextColorPrimary);
            holder.mBarChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
            holder.mBarChart.animateY(ANIMATION_DURATION);
        }
        if (holder.mPieChart.getVisibility() == View.VISIBLE) {
            // clear chart
            holder.mPieChart.clear();

            ArrayList<Entry> yVals = new ArrayList<>();
            ArrayList<String> captions = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();

            // add series to pie chart
            AppUtils.addSerieToPieChart(yVals, captions, colors,
                    mContext.getString(Grade.G1.getStringResID()),
                    AppUtils.getGradePercent(card.getGrades(), Grade.G1, card.isWeighted()),
                    AppUtils.getGradeEcts(card.getGrades(), Grade.G1),
                    Grade.G1.getColor());
            AppUtils.addSerieToPieChart(yVals, captions, colors,
                    mContext.getString(Grade.G2.getStringResID()),
                    AppUtils.getGradePercent(card.getGrades(), Grade.G2, card.isWeighted()),
                    AppUtils.getGradeEcts(card.getGrades(), Grade.G2),
                    Grade.G2.getColor());
            AppUtils.addSerieToPieChart(yVals, captions, colors,
                    mContext.getString(Grade.G3.getStringResID()),
                    AppUtils.getGradePercent(card.getGrades(), Grade.G3, card.isWeighted()),
                    AppUtils.getGradeEcts(card.getGrades(), Grade.G3),
                    Grade.G3.getColor());
            AppUtils.addSerieToPieChart(yVals, captions, colors,
                    mContext.getString(Grade.G4.getStringResID()),
                    AppUtils.getGradePercent(card.getGrades(), Grade.G4, card.isWeighted()),
                    AppUtils.getGradeEcts(card.getGrades(), Grade.G4),
                    Grade.G4.getColor());
            if (!card.isPositiveOnly()) {
                AppUtils.addSerieToPieChart(yVals, captions, colors,
                        mContext.getString(Grade.G5.getStringResID()),
                        AppUtils.getGradePercent(card.getGrades(), Grade.G5, card.isWeighted()),
                        AppUtils.getGradeEcts(card.getGrades(), Grade.G5),
                        Grade.G5.getColor());
            }

            PieDataSet dataSet = new PieDataSet(yVals, "");
            dataSet.setColors(colors);
            PieData pieData = new PieData(captions, dataSet);

            holder.mPieChart.setData(pieData);
            // undo all highlights
            holder.mPieChart.highlightValues(null);
            holder.mPieChart.getLegend().setTextColor(mTextColorPrimary);
            holder.mPieChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
            holder.mPieChart.animateXY(ANIMATION_DURATION, ANIMATION_DURATION);
        }
    }

    private void initGradePlot(final StatViewHolder holder) {
        if (PreferenceWrapper.getUseLvaBarChart(mContext)) {
            holder.mBarChart.setVisibility(View.VISIBLE);
            initBarChart(holder.mBarChart);
            holder.mPieChart.setVisibility(View.GONE);
        } else {
            holder.mPieChart.setVisibility(View.VISIBLE);
            initPieChart(holder.mPieChart);
            holder.mBarChart.setVisibility(View.GONE);
        }
    }

    private void initBarChart(BarChart barChart) {
        barChart.setPinchZoom(false);
        barChart.setHighlightEnabled(false);
        barChart.setDescription("");
        barChart.setDrawLegend(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawYValues(false);

        XLabels xl = barChart.getXLabels();
        xl.setPosition(XLabels.XLabelPosition.BOTTOM);
        xl.setCenterXLabelText(true);
        xl.setTextColor(mTextColorPrimary);

        YLabels yl = barChart.getYLabels();
        yl.setTextColor(mTextColorPrimary);

        barChart.setDrawGridBackground(false);
        barChart.setGridColor(mTextColorSecondary);
//        barChart.setDrawBorder(true);
//        barChart.setBorderColor(mTextColorSecondary);
    }

    private void initPieChart(final PieChart pieChart) {
        pieChart.setDrawXValues(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDescription("");
        pieChart.setHoleRadius(45);
        pieChart.setTransparentCircleRadius(50);
        pieChart.setHoleColor(mBackgroundColor);
        pieChart.setRotationEnabled(false);
        pieChart.setRotationAngle(180);
        pieChart.setValueTextColor(mTextColorPrimary);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, int i) {
                if (entry instanceof EctsEntry) {
                    float ects = ((EctsEntry) entry).getEcts();
                    if (ects > 0) {
                        pieChart.setDrawCenterText(true);
                        pieChart.setCenterText(String.format("%.2f\nECTS", ects));
                    }
                }
            }

            @Override
            public void onNothingSelected() {
                pieChart.setDrawCenterText(false);
            }
        });

        Paint mCenterTextPaint = pieChart.getPaint(Chart.PAINT_CENTER_TEXT);
        if (mCenterTextPaint != null) {
            mCenterTextPaint.setColor(mTextColorPrimary);
        }
    }

    @Override
    public StatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stat_card, parent, false);
        return new StatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(StatViewHolder holder, int position) {
        StatCard item = getItem(position);
        switch(getItemViewType(position)) {
            case StatCard.TYPE_GRADE: {
                if (item.isWeighted()) {
                    holder.mTitle.setText(mContext.getString(R.string.stat_title_grade_weighted));
                } else {
                    holder.mTitle.setText(mContext.getString(R.string.stat_title_grade));
                }

                initGradeListItems(holder, item);
                initGradePlot(holder);
                updateGradePlot(holder, item);

                break;
            }
            case StatCard.TYPE_LVA: {
                holder.mTitle.setText(mContext.getString(R.string.stat_title_lva));

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

    private class LvaStatItem {

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

    private class GradeStatItem {
        private final GradeType mType;
        private final double mAvgGrade;

        public GradeStatItem(GradeType type, List<ExamGrade> grades, boolean isWeighted, boolean positiveOnly) {
            this.mType = type;
            this.mAvgGrade = AppUtils.getAvgGrade(grades, isWeighted, type, positiveOnly);
        }

        public GradeType getType() {
            return mType;
        }

        public double getAvgGrade() {
            return mAvgGrade;
        }
    }

    public class StatViewHolder extends RecyclerView.ViewHolder {

        public Toolbar mToolbar;
        public TextView mTitle;
        public GridLayout mItems;
        public BarChart mBarChart;
        public PieChart mPieChart;

        public StatViewHolder(View itemView) {
            super(itemView);

            mToolbar = (Toolbar) itemView.findViewById(R.id.stat_card_toolbar);
            mTitle = (TextView) itemView.findViewById(R.id.stat_card_title);
            mItems = (GridLayout) itemView.findViewById(R.id.stat_card_items);
            mBarChart = (BarChart) itemView.findViewById(R.id.stat_card_diagram_bar);
            mPieChart = (PieChart) itemView.findViewById(R.id.stat_card_diagram_pie);

        }
    }
}
