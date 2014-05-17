package org.voidsink.anewjkuapp.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.Lva;

import edu.emory.mathcs.backport.java.util.Arrays;

@SuppressLint("ValidFragment")
public class StatisticDetailFragment extends BaseFragment {

	private static final String TAG = StatisticDetailFragment.class
			.getSimpleName();

	private List<String> mTerms;
	private List<Lva> mLvas;
	private List<ExamGrade> mGrades;
	private List<Lva> mOpenLvas;
	private List<Lva> mDoneLvas;
	private List<Lva> mFailedLvas;

	public StatisticDetailFragment() {
		this("", new ArrayList<Lva>(), new ArrayList<ExamGrade>());
	}

	public StatisticDetailFragment(List<String> terms, List<Lva> lvas,
			List<ExamGrade> grades) {
		this.mTerms = terms;
		this.mLvas = lvas;
		this.mGrades = grades;

		this.mOpenLvas = new ArrayList<Lva>();
		this.mDoneLvas = new ArrayList<Lva>();
		this.mFailedLvas = new ArrayList<Lva>();

		Log.i(TAG, this.mTerms.toString());
		for (Lva lva : this.mLvas) {
			if (mTerms.contains(lva.getTerm())) {
				ExamGrade grade = findGrade(this.mGrades, lva);
				if (grade == null) {
					this.mOpenLvas.add(lva);
					Log.i(TAG, "open: " + lva.getKey() + " - " + lva.getECTS());
				} else if (grade.getGrade() == Grade.G5) {
					this.mFailedLvas.add(lva);
					Log.i(TAG,
							"failed: " + lva.getKey() + " - " + lva.getECTS());
				} else {
					this.mDoneLvas.add(lva);
					Log.i(TAG, "done: " + lva.getKey() + " - " + lva.getECTS());
				}
			}
		}
	}

	public StatisticDetailFragment(String term, List<Lva> lvas,
			List<ExamGrade> grades) {
		this(Arrays.asList(new String[] { term }), lvas, grades);
	}

	private ExamGrade findGrade(List<ExamGrade> grades, Lva lva) {
		ExamGrade finalGrade = null;
		for (ExamGrade grade : grades) {
			if (grade.getLvaNr() == lva.getLvaNr()) {
				if (finalGrade == null || finalGrade.getGrade() == Grade.G5) {
					finalGrade = grade;
				}
			}
		}
		if (finalGrade == null) {
			Log.w(TAG, "findByTitle: " + lva.getTitle());
			for (ExamGrade grade : grades) {
				if (grade.getTitle() == lva.getTitle()) {
					if (finalGrade == null || finalGrade.getGrade() == Grade.G5) {
						finalGrade = grade;
					}
				}
			}
		}

		return finalGrade;
	}

	private double getECTS(List<Lva> lvas) {
		double sum = 0;
		for (Lva lva : lvas) {
			sum += lva.getECTS();
		}
		return sum;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_statistic_detail,
				container, false);

		((TextView) view.findViewById(R.id.stat_ects_done)).setText(String
				.format("%.2f", getECTS(mDoneLvas)));
		((TextView) view.findViewById(R.id.stat_ects_open)).setText(String
				.format("%.2f", getECTS(mOpenLvas)));
		((TextView) view.findViewById(R.id.stat_ects_failed)).setText(String
				.format("%.2f", getECTS(mFailedLvas)));
		((TextView) view.findViewById(R.id.stat_ects_all)).setText(String
				.format("%.2f", getECTS(mDoneLvas) + getECTS(mOpenLvas)
						+ getECTS(mFailedLvas)));

		final LinearLayout mCharts = (LinearLayout) view
				.findViewById(R.id.charts);
		final CategorySeries mSeries = new CategorySeries("");
		final DefaultRenderer mRenderer = new DefaultRenderer();

		mRenderer.setStartAngle(180);
		mRenderer.setDisplayValues(true);
		mRenderer.setZoomEnabled(false);
		mRenderer.setPanEnabled(false);
		mRenderer.setInScroll(true);
		mRenderer.setLabelsTextSize(20);

		addSerieToChart(mSeries, mRenderer, "done", getECTS(mDoneLvas),
				Color.GREEN);
		addSerieToChart(mSeries, mRenderer, "open", getECTS(mOpenLvas),
				Color.YELLOW);
		addSerieToChart(mSeries, mRenderer, "failed", getECTS(mFailedLvas),
				Color.RED);

		final GraphicalView mChartView = ChartFactory.getPieChartView(
				getActivity(), mSeries, mRenderer);
		mRenderer.setClickEnabled(true);
		mChartView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SeriesSelection seriesSelection = mChartView
						.getCurrentSeriesAndPoint();
				if (seriesSelection == null) {
					// Toast.makeText(getActivity(),
					// "No chart element selected",
					// Toast.LENGTH_SHORT).show();
				} else {
					for (int i = 0; i < mSeries.getItemCount(); i++) {
						mRenderer.getSeriesRendererAt(i).setHighlighted(
								i == seriesSelection.getPointIndex());
					}
					mChartView.repaint();
					Toast.makeText(
							getActivity(),
							mSeries.getCategory(seriesSelection.getPointIndex())
									+ ": "
									+ String.format("%.2f",
											seriesSelection.getValue()),
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		mCharts.addView(mChartView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		return view;
	}

	private void addSerieToChart(CategorySeries series,
			DefaultRenderer renderer, String category, double value, int color) {
		if (value > 0) {
			series.add(category, value);
			SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
			seriesRenderer.setColor(color);
			renderer.addSeriesRenderer(seriesRenderer);
		}
	}

}
