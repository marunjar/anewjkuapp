package org.voidsink.anewjkuapp;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.pie.PieChart;
import com.androidplot.ui.SeriesRenderer;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import org.voidsink.anewjkuapp.base.ThemedCardExpand;
import org.voidsink.anewjkuapp.base.ThemedCardWithList;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.GradeType;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by paul on 14.09.2014.
 */
public class StatCardGrade extends ThemedCardWithList {

    final boolean mEctsWeighting;
    final boolean mPositiveOnly;
    private List<ExamGrade> mGrades;

    public StatCardGrade(Context context, boolean ectsWeighting, boolean positiveOnly) {
        super(context);
        this.mEctsWeighting = ectsWeighting;
        this.mPositiveOnly = positiveOnly;
    }

    public void setValues(List<String> terms, List<ExamGrade> grades){
        this.mGrades = AppUtils.filterGrades(terms, grades);

        List<ListObject> objects = initChildren();
        getLinearListAdapter().clear();
        getLinearListAdapter().addAll(objects);

        updateProgressBar(true,true);
    }

    @Override
    protected CardHeader initCardHeader() {
        CardHeader header = new CardHeader(getContext());
        if (mEctsWeighting) {
            header.setTitle(getContext().getString(R.string.stat_title_grade_weighted));
        } else {
            header.setTitle(getContext().getString(R.string.stat_title_grade));
        }

        //Set visible the expand/collapse button
        header.setButtonExpandVisible(true);

        //Add Header to card
        addCardHeader(header);

        //This provides a simple (and useless) expand area
        addCardExpand(new GradeDiagramCardExpand(getContext(), this.mEctsWeighting, this.mPositiveOnly));

        return header;
    }

    @Override
    protected void initCard() {
        //Provide a custom view for the ViewStud EmptyView
        setEmptyViewViewStubLayoutId(R.layout.stat_card_empty);

        setUseProgressBar(true);
        updateProgressBar(false, false);
    }

    @Override
    protected List<ListObject> initChildren() {
        List<ListObject> gradeStats = new ArrayList<>();

        GradeStatItem grade = new GradeStatItem(this, GradeType.INTERIM_COURSE_ASSESSMENT, this.mGrades, this.mEctsWeighting, this.mPositiveOnly);
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(this, GradeType.FINAL_COURSE_ASSESSMENT, this.mGrades, this.mEctsWeighting, this.mPositiveOnly);
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(this, GradeType.RECOGNIZED_COURSE_CERTIFICATE, this.mGrades, this.mEctsWeighting, this.mPositiveOnly);
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(this, GradeType.RECOGNIZED_EXAM, this.mGrades, this.mEctsWeighting, this.mPositiveOnly);
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(this, GradeType.RECOGNIZED_ASSESSMENT, this.mGrades, this.mEctsWeighting, this.mPositiveOnly);
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(this, GradeType.FINAL_EXAM, this.mGrades, this.mEctsWeighting, this.mPositiveOnly);
        if (grade.getAvgGrade() > 0) {
            gradeStats.add(grade);
        }

        grade = new GradeStatItem(this, GradeType.ALL, this.mGrades, this.mEctsWeighting, this.mPositiveOnly);
        if (grade.getAvgGrade() > 0 && gradeStats.size() > 1) {
            gradeStats.add(grade);
        }

        if (gradeStats.size() == 0) {
            gradeStats.add(new GradeStatItem(this, GradeType.NONE_AVAILABLE, null, this.mEctsWeighting, this.mPositiveOnly));
        }

        return gradeStats;
    }

    private class GradeStatItem extends DefaultListObject {

        private final GradeType mType;
        private final double mAvgGrade;

        public GradeStatItem(Card parentCard, GradeType type, List<ExamGrade> grades, boolean ectsWeighting, boolean positiveOnly) {
            super(parentCard);

            this.mType = type;
            this.mAvgGrade = AppUtils.getAvgGrade(grades, ectsWeighting, type, positiveOnly);
        }

        public GradeType getType() {
            return mType;
        }

        public double getAvgGrade() {
            return mAvgGrade;
        }
    }

    @Override
    public View setupChildView(int i, ListObject object, View convertView, ViewGroup viewGroup) {
        //Setup the elements inside each row
        TextView type = (TextView) convertView.findViewById(R.id.stat_card_grade_list_entry_type);
        TextView avgGrade = (TextView) convertView.findViewById(R.id.stat_card_grade_list_entry_grade);

        GradeStatItem grade = (GradeStatItem) object;

        type.setText(getContext().getString(grade.getType().getStringResID()));
        avgGrade.setText(String.format("ø %.2f", grade.getAvgGrade()));

        return convertView;
    }

    class GradeBarFormatter extends BarFormatter {
        public GradeBarFormatter(int fillColor, int borderColor) {
            super(fillColor, borderColor);
        }

        @Override
        public Class<? extends SeriesRenderer> getRendererClass() {
            return GradeBarRenderer.class;
        }

        @Override
        public SeriesRenderer getRendererInstance(XYPlot plot) {
            return new GradeBarRenderer(plot);
        }
    }

    class GradeBarRenderer extends BarRenderer<GradeBarFormatter> {

        public GradeBarRenderer(XYPlot plot) {
            super(plot);
        }

        /**
         * Implementing this method to allow us to inject our
         * special selection formatter.
         *
         * @param index  index of the point being rendered.
         * @param series XYSeries to which the point being rendered belongs.
         * @return
         */
        @Override
        public GradeBarFormatter getFormatter(int index, XYSeries series) {
            switch (index) {
                case 1:
                    return new GradeBarFormatter(Grade.G1.getColor(), Color.GRAY);
                case 2:
                    return new GradeBarFormatter(Grade.G2.getColor(), Color.GRAY);
                case 3:
                    return new GradeBarFormatter(Grade.G3.getColor(), Color.GRAY);
                case 4:
                    return new GradeBarFormatter(Grade.G4.getColor(), Color.GRAY);
                case 5:
                    return new GradeBarFormatter(Grade.G5.getColor(), Color.GRAY);
                default:
                    return getFormatter(series);
            }
        }
    }

    @Override
    public int getChildLayoutId() {
        return R.layout.stat_card_grade_list_entry;
    }

    private class GradeDiagramCardExpand extends ThemedCardExpand {

        private final boolean mEctsWeighting;
        private final boolean mPositiveOnly;

        public GradeDiagramCardExpand(Context context, boolean ectsWeighting, boolean positiveOnly) {
            super(context, R.layout.stat_card_grade_diagram);
            this.mEctsWeighting = ectsWeighting;
            this.mPositiveOnly = positiveOnly;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            XYPlot barChart = (XYPlot) view.findViewById(R.id.stat_card_grade_diagram_bar);
            PieChart pieChart = (PieChart) view.findViewById(R.id.stat_card_grade_diagram_pie);

            if (PreferenceWrapper.getUseLvaBarChart(getContext())) {
                pieChart.setVisibility(View.GONE);

                List<Number> values = new ArrayList<Number>();
                values.add(null); // workaround to start grades at 1
                values.add(AppUtils.getGradePercent(mGrades, Grade.G1, this.mEctsWeighting));
                values.add(AppUtils.getGradePercent(mGrades, Grade.G2, this.mEctsWeighting));
                values.add(AppUtils.getGradePercent(mGrades, Grade.G3, this.mEctsWeighting));
                values.add(AppUtils.getGradePercent(mGrades, Grade.G4, this.mEctsWeighting));
                if (!mPositiveOnly) {
                    values.add(AppUtils.getGradePercent(mGrades, Grade.G5, this.mEctsWeighting));
                }

                SimpleXYSeries mSeries = new SimpleXYSeries(values,
                        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, getContext().getString(R.string.stat_title_grade));
                barChart.addSeries(mSeries,
                        new GradeBarFormatter(Color.LTGRAY, Color.GRAY));

                barChart.getLegendWidget().setVisible(false);

                barChart.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 5);
                barChart.setRangeBoundaries(0, BoundaryMode.FIXED, 50, BoundaryMode.GROW);

                // workaround to center ects bar
                barChart.setDomainBoundaries(0, 6, BoundaryMode.FIXED);
                barChart.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);

                barChart.setDomainValueFormat(new NumberFormat() {

                    @Override
                    public StringBuffer format(double v, StringBuffer stringBuffer, FieldPosition fieldPosition) {
                        int grade = (int) v;
                        if (grade >= 1 && grade <= 5) {
                            return new StringBuffer(getContext().getString(Grade.parseGradeType(grade - 1).getStringResID()));
                        } else {
                            return new StringBuffer("");
                        }
                    }

                    @Override
                    public StringBuffer format(long l, StringBuffer stringBuffer, FieldPosition fieldPosition) {
                        throw new UnsupportedOperationException("not implemented");
                    }

                    @Override
                    public Number parse(String s, ParsePosition parsePosition) {
                        throw new UnsupportedOperationException("not implemented");
                    }
                });

                // Setup the BarRenderer with our selected options
                GradeBarRenderer renderer = (GradeBarRenderer) barChart
                        .getRenderer(GradeBarRenderer.class);
                if (renderer != null) {
                    renderer.setBarRenderStyle(BarRenderer.BarRenderStyle.SIDE_BY_SIDE);
                    renderer.setBarWidthStyle(BarRenderer.BarWidthStyle.VARIABLE_WIDTH);
                    renderer.setBarGap(25);
                }

                if (barChart.getSeriesSet().size() > 0) {
                    barChart.setVisibility(View.VISIBLE);
                } else {
                    barChart.setVisibility(View.GONE);
                }
            } else {
                barChart.setVisibility(View.GONE);

                // init pie chart
                AppUtils.addSerieToPieChart(pieChart,
                        getContext().getString(Grade.G1.getStringResID()),
                        AppUtils.getGradePercent(mGrades, Grade.G1, this.mEctsWeighting),
                        Grade.G1.getColor());
                AppUtils.addSerieToPieChart(pieChart,
                        getContext().getString(Grade.G2.getStringResID()),
                        AppUtils.getGradePercent(mGrades, Grade.G2, this.mEctsWeighting),
                        Grade.G2.getColor());
                AppUtils.addSerieToPieChart(pieChart,
                        getContext().getString(Grade.G3.getStringResID()),
                        AppUtils.getGradePercent(mGrades, Grade.G3, this.mEctsWeighting),
                        Grade.G3.getColor());
                AppUtils.addSerieToPieChart(pieChart,
                        getContext().getString(Grade.G4.getStringResID()),
                        AppUtils.getGradePercent(mGrades, Grade.G4, this.mEctsWeighting),
                        Grade.G4.getColor());
                if (!mPositiveOnly) {
                    AppUtils.addSerieToPieChart(pieChart,
                            getContext().getString(Grade.G5.getStringResID()),
                            AppUtils.getGradePercent(mGrades, Grade.G5, this.mEctsWeighting),
                            Grade.G5.getColor());
                }

                if (pieChart.getSeriesSet().size() > 0) {
                    pieChart.setVisibility(View.VISIBLE);
                } else {
                    pieChart.setVisibility(View.GONE);
                }
            }
        }
    }

}
