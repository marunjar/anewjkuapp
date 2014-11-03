package org.voidsink.anewjkuapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.pie.PieChart;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.XPositionMetric;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.androidplot.xy.YValueMarker;

import org.voidsink.anewjkuapp.base.ThemedCardExpand;
import org.voidsink.anewjkuapp.base.ThemedCardWithList;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.kusss.LvaState;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by paul on 14.09.2014.
 */
public class StatCardLva extends ThemedCardWithList {

    private final List<LvaWithGrade> mLvas;
    private final List<String> mTerms;

    public StatCardLva(Context context, List<String> terms, List<Lva> lvas, List<ExamGrade> grades) {
        super(context);
        this.mLvas = AppUtils.getLvasWithGrades(terms, lvas, grades);
        this.mTerms = terms;
    }

    @Override
    protected CardHeader initCardHeader() {
        CardHeader header = new CardHeader(getContext());
        header.setTitle("LVAs");
        //Set visible the expand/collapse button
        header.setButtonExpandVisible(this.mLvas.size() > 0);

        //Add Header to card
        addCardHeader(header);

        //This provides a simple (and useless) expand area
        CardExpand expand = new LvaDiagramCardExpand(getContext(), this.mTerms, this.mLvas);

        addCardExpand(expand);

        return header;
    }

    @Override
    protected void initCard() {
        //Provide a custom view for the ViewStud EmptyView
        setEmptyViewViewStubLayoutId(R.layout.stat_card_empty);

        setUseProgressBar(true);
    }

    @Override
    protected List<ListObject> initChildren() {
        List<ListObject> lvaStats = new ArrayList<>();

        LvaStatItem lva = new LvaStatItem(this, LvaState.OPEN, AppUtils.getECTS(LvaState.OPEN, mLvas));
        if (lva.getEcts() > 0) {
            lvaStats.add(lva);
        }

        lva = new LvaStatItem(this, LvaState.DONE, AppUtils.getECTS(LvaState.DONE, mLvas));
        if (lva.getEcts() > 0) {
            lvaStats.add(lva);
        }

        lva = new LvaStatItem(this, LvaState.ALL, AppUtils.getECTS(LvaState.ALL, mLvas));
        if (lva.getEcts() > 0 && lvaStats.size() > 1) {
            lvaStats.add(lva);
        }

        return lvaStats;
    }

    private class LvaStatItem extends DefaultListObject {

        private final LvaState mType;
        private final double mEcts;

        public LvaStatItem(Card parentCard, LvaState type, double ects) {
            super(parentCard);

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

    @Override
    public View setupChildView(int childPosition, ListObject object, View convertView, ViewGroup parent) {
        //Setup the elements inside each row
        TextView type = (TextView) convertView.findViewById(R.id.stat_card_lva_list_entry_type);
        TextView ects = (TextView) convertView.findViewById(R.id.stat_card_lva_list_entry_ects);

        LvaStatItem lva = (LvaStatItem) object;

        type.setText(getContext().getString(lva.getType().getStringResID()));
        ects.setText(String.format("%.2f ECTS", lva.getEcts()));

        return convertView;
    }

    @Override
    public int getChildLayoutId() {
        return R.layout.stat_card_lva_list_entry;
    }

    private class LvaDiagramCardExpand extends ThemedCardExpand {

        private final List<String> mTerms;
        private final List<LvaWithGrade> mLvas;

        public LvaDiagramCardExpand(Context context, List<String> terms, List<LvaWithGrade> lvas) {
            super(context, R.layout.stat_card_lva_diagram);
            this.mTerms = terms;
            this.mLvas = lvas;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            super.setupInnerViewElements(parent, view);

            double mOpenEcts = AppUtils.getECTS(LvaState.OPEN, this.mLvas);
            double mDoneEcts = AppUtils.getECTS(LvaState.DONE, this.mLvas);
            double minEcts = this.mTerms.size() * 30;

            XYPlot barChart = (XYPlot) view.findViewById(R.id.stat_card_lva_diagram_bar);
            PieChart pieChart = (PieChart) view.findViewById(R.id.stat_card_lva_diagram_pie);

            if (PreferenceWrapper.getUseLvaBarChart(getContext())) {
                pieChart.setVisibility(View.GONE);

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

                // calc range manually
                double rangeTopMax = this.mTerms.size() * 30;
                if (mDoneEcts + mOpenEcts > (rangeTopMax * .9)) {
                    rangeTopMax = (Math.ceil((mDoneEcts + mOpenEcts) * 1.1 / 10) * 10);
                }

                // calc steps
                double rangeStep = Math.ceil((rangeTopMax / 10) / 10) * 10;

                // init bar chart
                addSerieToBarChart(barChart, getContext().getString(R.string.lva_done),
                        mDoneEcts, Grade.G1.getColor());
                addSerieToBarChart(barChart, getContext().getString(R.string.lva_open),
                        mOpenEcts, Grade.G3.getColor());

                barChart.setRangeTopMin(this.mTerms.size() * 30);
                barChart.setRangeBoundaries(0, BoundaryMode.FIXED, rangeTopMax,
                        BoundaryMode.FIXED);
                barChart.setRangeStep(XYStepMode.INCREMENT_BY_VAL, rangeStep);

                // workaround to center ects bar
                barChart.setDomainBoundaries(0, 2, BoundaryMode.FIXED);
                barChart.addMarker(ectsMarker);

                // Setup the BarRenderer with our selected options
                BarRenderer<?> renderer = ((BarRenderer<?>) barChart
                        .getRenderer(BarRenderer.class));
                if (renderer != null) {
                    renderer.setBarRenderStyle(BarRenderer.BarRenderStyle.STACKED);
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
                AppUtils.addSerieToPieChart(pieChart, getContext().getString(R.string.lva_done),
                        mDoneEcts, Grade.G1.getColor());
                AppUtils.addSerieToPieChart(pieChart, getContext().getString(R.string.lva_open),
                        mOpenEcts, Grade.G3.getColor());

                double missingECTS = minEcts - (mDoneEcts + mOpenEcts);
                if (missingECTS > 0) {
                    AppUtils.addSerieToPieChart(pieChart, "",
                            missingECTS, Color.GRAY);
                }

                if (pieChart.getSeriesSet().size() > 0) {
                    pieChart.setVisibility(View.VISIBLE);
                } else {
                    pieChart.setVisibility(View.GONE);
                }
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
    }
}
