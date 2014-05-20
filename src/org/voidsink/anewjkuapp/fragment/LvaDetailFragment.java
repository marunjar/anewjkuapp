package org.voidsink.anewjkuapp.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
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

import edu.emory.mathcs.backport.java.util.Arrays;

@SuppressLint("ValidFragment")
public class LvaDetailFragment extends BaseFragment {

	private static final String TAG = LvaDetailFragment.class.getSimpleName();

	private List<String> mTerms;

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

		this.mOpenLvas = new ArrayList<LvaWithGrade>();
		this.mDoneLvas = new ArrayList<LvaWithGrade>();
		this.mFailedLvas = new ArrayList<LvaWithGrade>();

		// Log.i(TAG, this.mTerms.toString());
		for (Lva lva : lvas) {
			if (mTerms.contains(lva.getTerm())) {
				ExamGrade grade = findGrade(grades, lva);
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
		// remove duplicates
		AppUtils.removeDuplicates(this.mDoneLvas, this.mOpenLvas,
				this.mFailedLvas);
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

		expListView = (ExpandableListView) view
				.findViewById(R.id.stat_lva_lists);
		adapter = new LvaListAdapter(getContext(), this.mDoneLvas,
				this.mOpenLvas, this.mFailedLvas);
		expListView.setAdapter((ExpandableListAdapter) adapter);

		PieChart pieChart = (PieChart) view.findViewById(R.id.pie_chart);

		addSerieToChart(pieChart, getString(R.string.lva_done),
				AppUtils.getECTS(mDoneLvas), Color.rgb(0, 220, 0));
		addSerieToChart(pieChart, getString(R.string.lva_open),
				AppUtils.getECTS(mOpenLvas), Color.rgb(220, 220, 0));
		addSerieToChart(pieChart, getString(R.string.lva_failed),
				AppUtils.getECTS(mFailedLvas), Color.rgb(220, 0, 0));

		if (pieChart.getSeriesSet().size() > 0) {
			pieChart.setVisibility(View.VISIBLE);
		} else {
			pieChart.setVisibility(View.GONE);
		}

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.lva, menu);
	}

	private void addSerieToChart(PieChart chart, String category, double value,
			int color) {
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
