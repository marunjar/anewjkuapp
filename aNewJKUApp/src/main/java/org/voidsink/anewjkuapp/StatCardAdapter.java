package org.voidsink.anewjkuapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.ui.SeriesRenderer;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.XPositionMetric;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.androidplot.xy.YValueMarker;

import org.voidsink.anewjkuapp.base.BaseArrayAdapter;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.GradeType;
import org.voidsink.anewjkuapp.kusss.LvaState;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StatCardAdapter extends BaseArrayAdapter<StatCard> {

    public StatCardAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StatCard card = getItem(position);

        if (card == null) {
            return null;
        }

        switch (card.getType()) {
            case StatCard.TYPE_GRADE:
                return getGradeView(convertView, parent, card);
            case StatCard.TYPE_LVA:
                return getLvaView(convertView, parent, card);
            default:
                return null;
        }
    }

    private View getLvaView(View convertView, ViewGroup parent, StatCard card) {
        StatCardViews views = null;

        if (convertView == null) {
            final LayoutInflater mInflater = LayoutInflater.from(getContext());
            convertView = mInflater.inflate(R.layout.stat_card, parent, false);
            views = new StatCardViews();

            views.toolbar = (Toolbar) convertView.findViewById(R.id.stat_card_toolbar);
            views.title = (TextView) convertView.findViewById(R.id.stat_card_title);
            views.items = (GridLayout) convertView.findViewById(R.id.stat_card_items);
            views.barChart = (XYPlot) convertView.findViewById(R.id.stat_card_diagram_bar);
            views.pieChart = (PieChart) convertView.findViewById(R.id.stat_card_diagram_pie);

            convertView.setTag(views);
        }

        if (views == null) {
            views = (StatCardViews) convertView.getTag();
        }

        views.title.setText(getContext().getString(R.string.stat_title_lva));

        initLvaListItems(views, card);
        initLvaPlot(views);
        updateLvaPlot(views, card);

        return convertView;
    }

    private void initLvaListItems(StatCardViews views, StatCard card) {
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

        views.items.removeAllViews();

        for (LvaStatItem item : lvaStats) {
            final LayoutInflater mInflater = LayoutInflater.from(getContext());
            View view = mInflater.inflate(R.layout.stat_card_lva_list_entry, null, false);

            TextView type = (TextView) view.findViewById(R.id.stat_card_lva_list_entry_type);
            TextView ects = (TextView) view.findViewById(R.id.stat_card_lva_list_entry_ects);

            type.setText(getContext().getString(item.getType().getStringResIDExt()));
            ects.setText(String.format("%.2f ECTS", item.getEcts()));

            views.items.addView(view);
        }
    }

    private void updateLvaPlot(StatCardViews views, StatCard card) {
        List<LvaWithGrade> mLvas = card.getLvasWithGrades();
        double mOpenEcts = AppUtils.getECTS(LvaState.OPEN, mLvas);
        double mDoneEcts = AppUtils.getECTS(LvaState.DONE, mLvas);
        double minEcts = (card.getTerms() != null) ? card.getTerms().size() * 30 : 0;

        if (views.barChart.getVisibility() == View.VISIBLE) {
            // clear chart
            // remove all series from each plot
            Iterator<XYSeries> i = views.barChart.getSeriesSet().iterator();
            while (i.hasNext()) {
                XYSeries setElement = i.next();
                views.barChart.removeSeries(setElement);
            }
            // remove all marker
            views.barChart.removeMarkers();

            // add ects marker
            if (minEcts > 0) {
                YValueMarker ectsMarker = new YValueMarker(minEcts, // y-val to mark
                        String.format("%.2f ECTS", minEcts), // marker label
                        new XPositionMetric( // object instance to set text
                                // positioning
                                // on the marker
                                PixelUtils.dpToPix(5), // 5dp offset
                                XLayoutStyle.ABSOLUTE_FROM_RIGHT), // offset origin
                        Color.rgb(220, 0, 0), // line paint color
                        Color.rgb(220, 0, 0)); // text paint color

                ectsMarker.getTextPaint().setTextSize(PixelUtils.dpToPix(12));

                DashPathEffect dpe = new DashPathEffect(new float[]{
                        PixelUtils.dpToPix(2), PixelUtils.dpToPix(2)}, 0);

                ectsMarker.getLinePaint().setPathEffect(dpe);

                views.barChart.addMarker(ectsMarker);
            }

            // calculate range
            double rangeTopMax = ((card.getTerms() != null) ? card.getTerms().size() : 1) * 30;
            if ((mDoneEcts + mOpenEcts) > rangeTopMax) {
                rangeTopMax = (Math.ceil((mDoneEcts + mOpenEcts + 10) / 10) * 10);
            } else {
                rangeTopMax = rangeTopMax + 10;
            }

            // calc steps
            double rangeStep = Math.ceil((rangeTopMax / 10) / 10) * 10;

            views.barChart.setRangeTopMin(minEcts);
            views.barChart.setRangeBoundaries(0, BoundaryMode.FIXED, rangeTopMax,
                    BoundaryMode.FIXED);
            views.barChart.setRangeStep(XYStepMode.INCREMENT_BY_VAL, rangeStep);

            // add series to bar chart
            addSerieToBarChart(views.barChart, getContext().getString(LvaState.DONE.getStringResIDExt()),
                    mDoneEcts, Grade.G1.getColor());
            addSerieToBarChart(views.barChart, getContext().getString(LvaState.OPEN.getStringResIDExt()),
                    mOpenEcts, Grade.G3.getColor());

            // Setup the BarRenderer with our selected options
            BarRenderer<?> renderer = ((BarRenderer<?>) views.barChart
                    .getRenderer(BarRenderer.class));
            if (renderer != null) {
                renderer.setBarRenderStyle(BarRenderer.BarRenderStyle.STACKED);
                renderer.setBarWidthStyle(BarRenderer.BarWidthStyle.VARIABLE_WIDTH);
                renderer.setBarGap(25);
            }

            views.barChart.redraw();
        }
        if (views.pieChart.getVisibility() == View.VISIBLE) {
            // clear chart
            // remove all series from each plot
            Iterator<Segment> i = views.pieChart.getSeriesSet().iterator();
            while (i.hasNext()) {
                Segment setElement = i.next();
                views.pieChart.removeSegment(setElement);
            }

            // add series to pie chart
            if (minEcts > 0) {
                double missingECTS = minEcts - (mDoneEcts + mOpenEcts);
                if (missingECTS > 0) {
                    AppUtils.addSerieToPieChart(views.pieChart, "",
                            missingECTS, Color.GRAY);
                }
            }
            AppUtils.addSerieToPieChart(views.pieChart, getContext().getString(LvaState.OPEN.getStringResIDExt()),
                    mOpenEcts, Grade.G3.getColor());
            AppUtils.addSerieToPieChart(views.pieChart, getContext().getString(LvaState.DONE.getStringResIDExt()),
                    mDoneEcts, Grade.G1.getColor());

            views.pieChart.redraw();
        }
    }

    private void addSerieToBarChart(XYPlot barChart, String category,
                                    double value, int color) {
        if (value > 0) {
            List<Number> values = new ArrayList<Number>();
            values.add(null); // workaround to center ects bar
            values.add(value);
            values.add(null); // workaround to center ects bar

            SimpleXYSeries mSeries = new SimpleXYSeries(values,
                    SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, category);
            barChart.addSeries(mSeries,
                    new BarFormatter(color, Color.GRAY));
        }
    }

    private void initLvaPlot(StatCardViews views) {
        if (PreferenceWrapper.getUseLvaBarChart(getContext())) {
            views.barChart.setVisibility(View.VISIBLE);
            views.pieChart.setVisibility(View.GONE);

            // workaround to center ects bar
            views.barChart.setDomainBoundaries(0, 2, BoundaryMode.FIXED);
            // do not display domain
            views.barChart.getDomainLabelWidget().setVisible(false);
        } else {
            views.pieChart.setVisibility(View.VISIBLE);
            views.barChart.setVisibility(View.GONE);
        }
    }

    private View getGradeView(View convertView, ViewGroup parent, StatCard card) {
        StatCardViews views = null;
        if (convertView == null) {
            final LayoutInflater mInflater = LayoutInflater.from(getContext());
            convertView = mInflater.inflate(R.layout.stat_card, parent, false);
            views = new StatCardViews();

            views.toolbar = (Toolbar) convertView.findViewById(R.id.stat_card_toolbar);
            views.title = (TextView) convertView.findViewById(R.id.stat_card_title);
            views.items = (GridLayout) convertView.findViewById(R.id.stat_card_items);
            views.barChart = (XYPlot) convertView.findViewById(R.id.stat_card_diagram_bar);
            views.pieChart = (PieChart) convertView.findViewById(R.id.stat_card_diagram_pie);

            convertView.setTag(views);
        }

        if (views == null) {
            views = (StatCardViews) convertView.getTag();
        }

        if (card.isWeighted()) {
            views.title.setText(getContext().getString(R.string.stat_title_grade_weighted));
        } else {
            views.title.setText(getContext().getString(R.string.stat_title_grade));
        }

        initGradeListItems(views, card);
        initGradePlot(views);
        updateGradePlot(views, card);

        return convertView;
    }

    private void initGradeListItems(StatCardViews views, StatCard card) {
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

        views.items.removeAllViews();

        for (GradeStatItem item : gradeStats) {
            final LayoutInflater mInflater = LayoutInflater.from(getContext());

            View view = mInflater.inflate(R.layout.stat_card_grade_list_entry, null, false);
            TextView type = (TextView) view.findViewById(R.id.stat_card_grade_list_entry_type);
            TextView avgGrade = (TextView) view.findViewById(R.id.stat_card_grade_list_entry_grade);

            type.setText(getContext().getString(item.getType().getStringResID()));
            avgGrade.setText(String.format("ø %.2f", item.getAvgGrade()));

            views.items.addView(view);
        }
    }

    private void updateGradePlot(StatCardViews views, StatCard card) {
        if (views.barChart.getVisibility() == View.VISIBLE) {
            // clear chart
            // remove all series from each plot
            Iterator<XYSeries> i = views.barChart.getSeriesSet().iterator();
            while (i.hasNext()) {
                XYSeries setElement = i.next();
                views.barChart.removeSeries(setElement);
            }
            // remove all marker
            views.barChart.removeMarkers();

            // add grades (in percent)
            List<Double> values = new ArrayList<>();
            values.add(null); // workaround to start grades at 1
            values.add(AppUtils.getGradePercent(card.getGrades(), Grade.G1, card.isWeighted()));
            values.add(AppUtils.getGradePercent(card.getGrades(), Grade.G2, card.isWeighted()));
            values.add(AppUtils.getGradePercent(card.getGrades(), Grade.G3, card.isWeighted()));
            values.add(AppUtils.getGradePercent(card.getGrades(), Grade.G4, card.isWeighted()));
            if (!card.isPositiveOnly()) {
                values.add(AppUtils.getGradePercent(card.getGrades(), Grade.G5, card.isWeighted()));
            }

            // calculate range
            double rangeTopMax = 0;
            // find max %
            for (Double n : values) {
                if (n != null) {
                    rangeTopMax = Math.max(rangeTopMax, n);
                }
            }
            if (rangeTopMax > 0) {
                // add some free space
                rangeTopMax = (Math.ceil((rangeTopMax + 10) / 10) * 10);
            } else {
                // default 25%
                rangeTopMax = 25;
            }

            // max 100%
            if (rangeTopMax > 100) {
                rangeTopMax = 100;
            }

            views.barChart.setRangeTopMin(25);
            views.barChart.setRangeBoundaries(0, BoundaryMode.FIXED, rangeTopMax,
                    BoundaryMode.FIXED);
            views.barChart.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 10);

            SimpleXYSeries mSeries = new SimpleXYSeries(values,
                    SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, getContext().getString(R.string.stat_title_grade));
            views.barChart.addSeries(mSeries,
                    new GradeBarFormatter(Color.LTGRAY, Color.GRAY));

            // Setup the BarRenderer with our selected options
            GradeBarRenderer renderer = (GradeBarRenderer) views.barChart
                    .getRenderer(GradeBarRenderer.class);
            if (renderer != null) {
                renderer.setBarRenderStyle(BarRenderer.BarRenderStyle.SIDE_BY_SIDE);
                renderer.setBarWidthStyle(BarRenderer.BarWidthStyle.VARIABLE_WIDTH);
                renderer.setBarGap(25);
            }

            views.barChart.redraw();
        }
        if (views.pieChart.getVisibility() == View.VISIBLE) {
            // clear chart
            // remove all series from each plot
            Iterator<Segment> i = views.pieChart.getSeriesSet().iterator();
            while (i.hasNext()) {
                Segment setElement = i.next();
                views.pieChart.removeSegment(setElement);
            }

            // add series to pie chart
            AppUtils.addSerieToPieChart(views.pieChart,
                    getContext().getString(Grade.G1.getStringResID()),
                    AppUtils.getGradePercent(card.getGrades(), Grade.G1, card.isWeighted()),
                    Grade.G1.getColor());
            AppUtils.addSerieToPieChart(views.pieChart,
                    getContext().getString(Grade.G2.getStringResID()),
                    AppUtils.getGradePercent(card.getGrades(), Grade.G2, card.isWeighted()),
                    Grade.G2.getColor());
            AppUtils.addSerieToPieChart(views.pieChart,
                    getContext().getString(Grade.G3.getStringResID()),
                    AppUtils.getGradePercent(card.getGrades(), Grade.G3, card.isWeighted()),
                    Grade.G3.getColor());
            AppUtils.addSerieToPieChart(views.pieChart,
                    getContext().getString(Grade.G4.getStringResID()),
                    AppUtils.getGradePercent(card.getGrades(), Grade.G4, card.isWeighted()),
                    Grade.G4.getColor());
            if (!card.isPositiveOnly()) {
                AppUtils.addSerieToPieChart(views.pieChart,
                        getContext().getString(Grade.G5.getStringResID()),
                        AppUtils.getGradePercent(card.getGrades(), Grade.G5, card.isWeighted()),
                        Grade.G5.getColor());
            }

            // add gray dummy segment
            if (views.pieChart.getSeriesSet().size() == 0) {
                AppUtils.addSerieToPieChart(views.pieChart, "", 100, Color.GRAY);
            }

            views.pieChart.redraw();
        }
    }

    private void initGradePlot(StatCardViews views) {
        if (PreferenceWrapper.getUseLvaBarChart(getContext())) {
            views.barChart.setVisibility(View.VISIBLE);
            views.pieChart.setVisibility(View.GONE);

            views.barChart.getLegendWidget().setVisible(false);

            // workaround to center ects bar
            views.barChart.setDomainBoundaries(0, 6, BoundaryMode.FIXED);
            views.barChart.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);

            views.barChart.setDomainValueFormat(new NumberFormat() {

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
        } else {
            views.pieChart.setVisibility(View.VISIBLE);
            views.barChart.setVisibility(View.GONE);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).getType();
    }

    private class StatCardViews {
        public Toolbar toolbar;
        public TextView title;
        public GridLayout items;
        public XYPlot barChart;
        public PieChart pieChart;
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
}
