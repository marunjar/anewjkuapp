package org.voidsink.anewjkuapp.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.DashPathEffect;
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
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;

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

@SuppressLint("ValidFragment")
public class LvaDetailFragment extends BaseFragment {

	private static final String TAG = LvaDetailFragment.class.getSimpleName();

	private List<String> mTerms;

	private List<LvaWithGrade> mOpenLvas;
	private List<LvaWithGrade> mDoneLvas;
	private ExpandableListView mExpListView;

	private LvaListAdapter mAdapter;

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

		mExpListView = (ExpandableListView) view
				.findViewById(R.id.lva_lists);
		mAdapter = new LvaListAdapter(getContext(), this.mDoneLvas,
				this.mOpenLvas);
		mExpListView.setAdapter((ExpandableListAdapter) mAdapter);

		double minEcts = mTerms.size() * 30;

		XYPlot barChart = (XYPlot) view.findViewById(R.id.lva_bar_chart);
		PieChart pieChart = (PieChart) view.findViewById(R.id.lva_pie_chart);

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

			DashPathEffect dpe = new DashPathEffect(new float[] {
					PixelUtils.dpToPix(2), PixelUtils.dpToPix(2) }, 0);

			ectsMarker.getLinePaint().setPathEffect(dpe);

			// calc range manually
			double rangeTopMax = mTerms.size() * 30;
			if (mDoneEcts + mOpenEcts > (rangeTopMax * .9)) {
				rangeTopMax = (Math.ceil((mDoneEcts + mOpenEcts) * 1.1 / 10) * 10);
			}

			// calc steps
			double rangeStep = Math.ceil((rangeTopMax / 10) / 10) * 10;

			// init bar chart
			addSerieToBarChart(barChart, getString(R.string.lva_done),
					mDoneEcts, Color.rgb(0, 220, 0));
			addSerieToBarChart(barChart, getString(R.string.lva_open),
					mOpenEcts, Color.rgb(220, 220, 0));

			barChart.setRangeTopMin(mTerms.size() * 30);
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
			AppUtils.addSerieToPieChart(pieChart, getString(R.string.lva_done),
					mDoneEcts, Color.rgb(0, 220, 0));
			AppUtils.addSerieToPieChart(pieChart, getString(R.string.lva_open),
					mOpenEcts, Color.rgb(220, 220, 0));

			if (pieChart.getSeriesSet().size() > 0) {
				pieChart.setVisibility(View.VISIBLE);
			} else {
				pieChart.setVisibility(View.GONE);
			}
		}

		return view;
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
					new BarFormatter(color, Color.rgb(0, 80, 0)));
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.lva, menu);
	}
}
