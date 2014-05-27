package org.voidsink.anewjkuapp.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.voidsink.anewjkuapp.AppUtils;
import org.voidsink.anewjkuapp.LvaListAdapter;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
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

@SuppressLint("ValidFragment")
public class LvaDetailFragment extends BaseFragment {

	private static final String TAG = LvaDetailFragment.class.getSimpleName();

	private List<String> mTerms;

	private List<LvaWithGrade> mOpenLvas;
	private List<LvaWithGrade> mDoneLvas;
	private ExpandableListView expListView;

	private LvaListAdapter adapter;

	public LvaDetailFragment() {
		this("", new ArrayList<Lva>(), new ArrayList<ExamGrade>());
	}

	public LvaDetailFragment(List<String> terms, List<Lva> lvas,
			List<ExamGrade> grades) {
		this.mTerms = terms;

		this.mOpenLvas = new ArrayList<LvaWithGrade>();
		this.mDoneLvas = new ArrayList<LvaWithGrade>();

		// Log.i(TAG, this.mTerms.toString());
		for (Lva lva : lvas) {
			if (mTerms.contains(lva.getTerm())) {
				ExamGrade grade = findGrade(grades, lva);
				if (grade == null || grade.getGrade() == Grade.G5) {
					this.mOpenLvas.add(new LvaWithGrade(lva, grade));
					// Log.i(TAG, "open: " + lva.getKey() + " - " +
					// lva.getECTS());
				} else {
					this.mDoneLvas.add(new LvaWithGrade(lva, grade));
					// Log.i(TAG, "done: " + lva.getKey() + " - " +
					// lva.getECTS());
				}
			}
		}
		// remove duplicates
		AppUtils.removeDuplicates(this.mDoneLvas, this.mOpenLvas);
	}

	public LvaDetailFragment(String term, List<Lva> lvas, List<ExamGrade> grades) {
		this(Arrays.asList(new String[] { term }), lvas, grades);
	}

	private ExamGrade findGrade(List<ExamGrade> grades, Lva lva) {
		ExamGrade finalGrade = null;

		for (ExamGrade grade : grades) {
			if (grade.getCode().equals(lva.getCode())) {
				if (finalGrade == null || finalGrade.getGrade() == Grade.G5) {
					finalGrade = grade;
				}
			}
		}
		if (finalGrade == null) {
			Log.d(TAG, "findByLvaNr: " + lva.getLvaNr() + "/" + lva.getTitle());
			for (ExamGrade grade : grades) {
				if (grade.getLvaNr() == lva.getLvaNr()) {
					if (finalGrade == null || finalGrade.getGrade() == Grade.G5) {
						finalGrade = grade;
					}
				}
			}
		}

		return finalGrade;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_lva_detail, container,
				false);

		double mDoneEcts = AppUtils.getECTS(mDoneLvas);
		double mOpenEcts = AppUtils.getECTS(mOpenLvas);

		expListView = (ExpandableListView) view
				.findViewById(R.id.stat_lva_lists);
		adapter = new LvaListAdapter(getContext(), this.mDoneLvas,
				this.mOpenLvas);
		expListView.setAdapter((ExpandableListAdapter) adapter);

		double minEcts = mTerms.size() * 30;

		YValueMarker ectsMarker = new YValueMarker(minEcts, // y-val to mark
				String.format("%.2f ECTS", minEcts), // marker label
				new XPositionMetric( // object instance to set text positioning
										// on the marker
						PixelUtils.dpToPix(5), // 5dp offset
						XLayoutStyle.ABSOLUTE_FROM_RIGHT), // offset origin
				Color.rgb(220, 0, 0), // line paint color
				Color.rgb(220, 0, 0)); // text paint color

		ectsMarker.getTextPaint().setTextSize(PixelUtils.dpToPix(12));

		DashPathEffect dpe = new DashPathEffect(new float[] {
				PixelUtils.dpToPix(2), PixelUtils.dpToPix(2) }, 0);

		ectsMarker.getLinePaint().setPathEffect(dpe);

		// calc range manually
		double rangeTopMax = mTerms.size() * 30;
		if (mDoneEcts + mOpenEcts > (rangeTopMax * .9)) {
			rangeTopMax = (Math.ceil((mDoneEcts + mOpenEcts) * 1.1 / 10) * 10);
		}

		// init bar chart
		XYPlot barChart = (XYPlot) view.findViewById(R.id.bar_chart);
		barChart.setRangeTopMin(mTerms.size() * 30);
		barChart.setRangeBoundaries(0, BoundaryMode.FIXED, rangeTopMax,
				BoundaryMode.FIXED);
		barChart.setRangeStep(XYStepMode.SUBDIVIDE, 10);

		addSerieToBarChart(barChart, getString(R.string.lva_done), mDoneEcts,
				Color.rgb(0, 220, 0));
		addSerieToBarChart(barChart, getString(R.string.lva_open), mOpenEcts,
				Color.rgb(220, 220, 0));

		barChart.addMarker(ectsMarker);

		// Setup the BarRenderer with our selected options
		BarRenderer<?> renderer = ((BarRenderer<?>) barChart
				.getRenderer(BarRenderer.class));
		renderer.setBarRenderStyle(BarRenderer.BarRenderStyle.STACKED);
		renderer.setBarGap(20);
		renderer.setBarWidthStyle(BarRenderer.BarWidthStyle.VARIABLE_WIDTH);

		// init pie chart
		PieChart pieChart = (PieChart) view.findViewById(R.id.pie_chart);

		addSerieToPieChart(pieChart, getString(R.string.lva_done), mDoneEcts,
				Color.rgb(0, 220, 0));
		addSerieToPieChart(pieChart, getString(R.string.lva_open), mOpenEcts,
				Color.rgb(220, 220, 0));

		if (pieChart.getSeriesSet().size() > 0) {
			pieChart.setVisibility(View.GONE);
		} else {
			pieChart.setVisibility(View.GONE);
		}

		double maxECTS = AppUtils.getECTS(mDoneLvas)
				+ AppUtils.getECTS(mOpenLvas);
		int rangeMax = mTerms.size() * 30;
		if (maxECTS > rangeMax) {
			rangeMax = (int) Math.ceil((maxECTS / 10)) * 10;
		}

		return view;
	}

	private void addSerieToBarChart(XYPlot barChart, String category,
			double value, int color) {
		List<Number> values = new ArrayList<Number>();
		values.add(value);

		SimpleXYSeries mSeries = new SimpleXYSeries(values,
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, category);
		barChart.addSeries(mSeries,
				new BarFormatter(color, Color.rgb(0, 80, 0)));
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.lva, menu);
	}

	private void addSerieToPieChart(PieChart chart, String category,
			double value, int color) {
		if (value > 0) {
			EmbossMaskFilter emf = new EmbossMaskFilter(
					new float[] { 1, 1, 1 }, 0.4f, 10, 3f);
			Segment segment = new Segment(category, value);
			SegmentFormatter formatter = new SegmentFormatter(color,
					Color.BLACK, Color.BLACK, Color.DKGRAY);
			formatter.getFillPaint().setMaskFilter(emf);

			chart.addSegment(segment, formatter);
		}
	}
}
