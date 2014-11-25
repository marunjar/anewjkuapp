package org.voidsink.anewjkuapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
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

import org.voidsink.anewjkuapp.base.ThemedCardExpand;
import org.voidsink.anewjkuapp.base.ThemedCardWithList;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.kusss.LvaState;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by paul on 14.09.2014.
 */
public class StatCardLva extends ThemedCardWithList {

    private List<LvaWithGrade> mLvas;
    private List<String> mTerms;

    public StatCardLva(Context context) {
        super(context);
        mLvas = new ArrayList<>();
        mTerms = new ArrayList<>();
    }

    public void setValues(List<String> terms, List<Lva> lvas, List<ExamGrade> grades) {
        this.mLvas = AppUtils.getLvasWithGrades(terms, lvas, grades);
        this.mTerms = terms;

        List<ListObject> objects = initChildren();
        getLinearListAdapter().clear();
        getLinearListAdapter().addAll(objects);

        updateProgressBar(true, true);
    }

    @Override
    protected CardHeader initCardHeader() {
        CardHeader header = new CardHeader(getContext());
        header.setTitle(getContext().getString(R.string.stat_title_lva));

        //init custom expand button
        header.setOtherButtonVisible(true);
        header.setOtherButtonDrawable(R.drawable.ic_insert_chart_grey600_36dp);
        header.setOtherButtonClickListener(new CardHeader.OnClickCardHeaderOtherButtonListener() {
            @Override
            public void onButtonItemClick(Card card, View view) {
                CardExpand ce = getCardExpand();
                if (ce instanceof LvaDiagramCardExpand) {
                    ((LvaDiagramCardExpand) ce).updatePlot();
                }

                doToogleExpand();
            }
        });

        //Add Header to card
        addCardHeader(header);

        //This provides a simple (and useless) expand area
        addCardExpand(new LvaDiagramCardExpand(getContext()));

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

        type.setText(getContext().getString(lva.getType().getStringResIDExt()));
        ects.setText(String.format("%.2f ECTS", lva.getEcts()));

        return convertView;
    }

    @Override
    public int getChildLayoutId() {
        return R.layout.stat_card_lva_list_entry;
    }

    private class LvaDiagramCardExpand extends ThemedCardExpand {

        private XYPlot barChart;
        private PieChart pieChart;

        public LvaDiagramCardExpand(Context context) {
            super(context, R.layout.stat_card_lva_diagram);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            super.setupInnerViewElements(parent, view);

            barChart = (XYPlot) view.findViewById(R.id.stat_card_lva_diagram_bar);
            pieChart = (PieChart) view.findViewById(R.id.stat_card_lva_diagram_pie);

            if (PreferenceWrapper.getUseLvaBarChart(getContext())) {
                barChart.setVisibility(View.VISIBLE);
                pieChart.setVisibility(View.GONE);

                // workaround to center ects bar
                barChart.setDomainBoundaries(0, 2, BoundaryMode.FIXED);
                // do not display domain
                barChart.getDomainLabelWidget().setVisible(false);
            } else {
                pieChart.setVisibility(View.VISIBLE);
                barChart.setVisibility(View.GONE);
            }

//            updatePlot();
        }

        public void updatePlot() {
            double mOpenEcts = AppUtils.getECTS(LvaState.OPEN, mLvas);
            double mDoneEcts = AppUtils.getECTS(LvaState.DONE, mLvas);
            double minEcts = (mTerms != null) ? mTerms.size() * 30 : 0;

            if (barChart.getVisibility() == View.VISIBLE) {
                // clear chart
                // remove all series from each plot
                Iterator<XYSeries> i = barChart.getSeriesSet().iterator();
                while (i.hasNext()) {
                    XYSeries setElement = i.next();
                    barChart.removeSeries(setElement);
                }
                // remove all marker
                barChart.removeMarkers();

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

                    barChart.addMarker(ectsMarker);
                }

                // calculate range
                double rangeTopMax = ((mTerms != null) ? mTerms.size() : 1) * 30;
                if ((mDoneEcts + mOpenEcts) > rangeTopMax) {
                    rangeTopMax = (Math.ceil((mDoneEcts + mOpenEcts + 10) / 10) * 10);
                } else {
                    rangeTopMax = rangeTopMax + 10;
                }

                // calc steps
                double rangeStep = Math.ceil((rangeTopMax / 10) / 10) * 10;

                barChart.setRangeTopMin(minEcts);
                barChart.setRangeBoundaries(0, BoundaryMode.FIXED, rangeTopMax,
                        BoundaryMode.FIXED);
                barChart.setRangeStep(XYStepMode.INCREMENT_BY_VAL, rangeStep);

                // add series to bar chart
                addSerieToBarChart(barChart, getContext().getString(LvaState.DONE.getStringResIDExt()),
                        mDoneEcts, Grade.G1.getColor());
                addSerieToBarChart(barChart, getContext().getString(LvaState.OPEN.getStringResIDExt()),
                        mOpenEcts, Grade.G3.getColor());

                // Setup the BarRenderer with our selected options
                BarRenderer<?> renderer = ((BarRenderer<?>) barChart
                        .getRenderer(BarRenderer.class));
                if (renderer != null) {
                    renderer.setBarRenderStyle(BarRenderer.BarRenderStyle.STACKED);
                    renderer.setBarWidthStyle(BarRenderer.BarWidthStyle.VARIABLE_WIDTH);
                    renderer.setBarGap(25);
                }

                barChart.redraw();
            }

            if (pieChart.getVisibility() == View.VISIBLE) {
                // clear chart
                // remove all series from each plot
                Iterator<Segment> i = pieChart.getSeriesSet().iterator();
                while (i.hasNext()) {
                    Segment setElement = i.next();
                    pieChart.removeSegment(setElement);
                }

                // add series to pie chart
                if (minEcts > 0) {
                    double missingECTS = minEcts - (mDoneEcts + mOpenEcts);
                    if (missingECTS > 0) {
                        AppUtils.addSerieToPieChart(pieChart, "",
                                missingECTS, Color.GRAY);
                    }
                }
                AppUtils.addSerieToPieChart(pieChart, getContext().getString(LvaState.OPEN.getStringResIDExt()),
                        mOpenEcts, Grade.G3.getColor());
                AppUtils.addSerieToPieChart(pieChart, getContext().getString(LvaState.DONE.getStringResIDExt()),
                        mDoneEcts, Grade.G1.getColor());

                pieChart.redraw();
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
