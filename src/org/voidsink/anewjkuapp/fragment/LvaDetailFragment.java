package org.voidsink.anewjkuapp.fragment;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.voidsink.anewjkuapp.AppUtils;
import org.voidsink.anewjkuapp.LvaListAdapter;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;

import edu.emory.mathcs.backport.java.util.Arrays;

@SuppressLint("ValidFragment")
public class LvaDetailFragment extends BaseFragment {

	private static final String TAG = LvaDetailFragment.class.getSimpleName();

	private List<String> mTerms;
	private List<ExamGrade> mGrades;
	private List<LvaWithGrade> mOpenLvas;
	private List<LvaWithGrade> mDoneLvas;
	private List<LvaWithGrade> mFailedLvas;
	private ExpandableListView expListView;

	private LvaListAdapter adapter;

	public LvaDetailFragment() {
		this("", new ArrayList<Lva>(), new ArrayList<ExamGrade>());
	}

	public LvaDetailFragment(List<String> terms, List<Lva> lvas,
			List<ExamGrade> grades) {
		this.mTerms = terms;
		this.mGrades = grades;

		this.mOpenLvas = new ArrayList<LvaWithGrade>();
		this.mDoneLvas = new ArrayList<LvaWithGrade>();
		this.mFailedLvas = new ArrayList<LvaWithGrade>();

		Log.i(TAG, this.mTerms.toString());
		for (Lva lva : lvas) {
			if (mTerms.contains(lva.getTerm())) {
				ExamGrade grade = findGrade(this.mGrades, lva);
				if (grade == null) {
					this.mOpenLvas.add(new LvaWithGrade(lva, grade));
					// Log.i(TAG, "open: " + lva.getKey() + " - " +
					// lva.getECTS());
				} else if (grade.getGrade() == Grade.G5) {
					this.mFailedLvas.add(new LvaWithGrade(lva, grade));
					// Log.i(TAG, "failed: " + lva.getKey() + " - " +
					// lva.getECTS());
				} else {
					this.mDoneLvas.add(new LvaWithGrade(lva, grade));
					// Log.i(TAG, "done: " + lva.getKey() + " - " +
					// lva.getECTS());
				}
			}
		}
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
			Log.i(TAG, "findByLvaNr: " + lva.getTitle());
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
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_lva_detail, container,
				false);

		expListView = (ExpandableListView) view
				.findViewById(R.id.stat_lva_lists);
		adapter = new LvaListAdapter(mContext, this.mDoneLvas, this.mOpenLvas,
				this.mFailedLvas);
		expListView.setAdapter((ExpandableListAdapter) adapter);

		final LinearLayout mCharts = (LinearLayout) view
				.findViewById(R.id.stat_charts);
		final CategorySeries mSeries = new CategorySeries("");
		final DefaultRenderer mRenderer = new DefaultRenderer();

		mRenderer.setStartAngle(180);
		mRenderer.setDisplayValues(true);
		mRenderer.setZoomEnabled(false);
		mRenderer.setPanEnabled(false);
		mRenderer.setInScroll(true);
		
		if (PreferenceWrapper.getUseLightDesign(mContext)) {
			mRenderer.setShowLegend(false);
			mRenderer.setShowLabels(true);
		} else {
			mRenderer.setShowLegend(true);
			mRenderer.setShowLabels(true);
		}
		
		TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[] {android.R.attr.textColorPrimary});
		mRenderer.setLabelsColor(a.getColor(0, Color.GRAY));
		
		mRenderer.setLabelsTextSize(getResources().getDimensionPixelSize(
				R.dimen.text_size_small));
		mRenderer.setLegendTextSize(getResources().getDimensionPixelSize(
				R.dimen.text_size_small));
		
		addSerieToChart(mSeries, mRenderer, getString(R.string.lva_done),
				AppUtils.getECTS(mDoneLvas), Color.GREEN);
		addSerieToChart(mSeries, mRenderer, getString(R.string.lva_open),
				AppUtils.getECTS(mOpenLvas), Color.YELLOW);
		addSerieToChart(mSeries, mRenderer, getString(R.string.lva_failed),
				AppUtils.getECTS(mFailedLvas), Color.RED);

		final GraphicalView mChartView = ChartFactory.getPieChartView(
				getActivity(), mSeries, mRenderer);
		mRenderer.setClickEnabled(true);
		if (mSeries.getItemCount() > 0) {
			mCharts.setVisibility(View.VISIBLE);
			mCharts.addView(mChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		} else {
			mCharts.setVisibility(View.GONE);
		}

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.lva, menu);
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
