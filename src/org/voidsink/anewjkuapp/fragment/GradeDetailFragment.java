package org.voidsink.anewjkuapp.fragment;

import java.util.ArrayList;
import java.util.List;

import org.voidsink.anewjkuapp.AppUtils;
import org.voidsink.anewjkuapp.GradeListAdapter;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Grade;

import com.androidplot.pie.PieChart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

@SuppressLint("ValidFragment")
public class GradeDetailFragment extends BaseFragment {

	public static final String TAG = GradeDetailFragment.class.getSimpleName();

	private ExpandableListView mListView;
	private GradeListAdapter mAdapter;

	private ArrayList<ExamGrade> mGrades;

	public GradeDetailFragment() {
		this(null);
	}

	public GradeDetailFragment(String term, List<ExamGrade> grades) {
		this.mGrades = new ArrayList<ExamGrade>();
		if (term != null) {
			if (mGrades != null) {
				for (ExamGrade grade : grades) {
					if (grade.getTerm().equals(term)) {
						this.mGrades.add(grade);
					}
				}
			}
		} else {
			if (mGrades != null) {
				this.mGrades.addAll(grades);
			}
		}
	}

	public GradeDetailFragment(List<ExamGrade> grades) {
		this(null, grades);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_grade_detail, container,
				false);

		mListView = (ExpandableListView) view.findViewById(R.id.grade_list);
		mAdapter = new GradeListAdapter(getContext());
		mAdapter.addAll(this.mGrades);
		mListView.setAdapter((ExpandableListAdapter) mAdapter);
		
		if (mAdapter.getGroupCount() == 1) {
			mListView.expandGroup(0);
		}
		
		PieChart pieChart = (PieChart) view.findViewById(R.id.grade_pie_chart);

		boolean ectsWeighting = false;
		
		// init pie chart
		AppUtils.addSerieToPieChart(pieChart, getString(Grade.G1.getStringResID()),
				AppUtils.getGradeCount(this.mGrades, Grade.G1, ectsWeighting), Grade.G1.getColor());
		AppUtils.addSerieToPieChart(pieChart, getString(Grade.G2.getStringResID()),
				AppUtils.getGradeCount(this.mGrades, Grade.G2, ectsWeighting), Grade.G2.getColor());
		AppUtils.addSerieToPieChart(pieChart, getString(Grade.G3.getStringResID()),
				AppUtils.getGradeCount(this.mGrades, Grade.G3, ectsWeighting), Grade.G3.getColor());
		AppUtils.addSerieToPieChart(pieChart, getString(Grade.G4.getStringResID()),
				AppUtils.getGradeCount(this.mGrades, Grade.G4, ectsWeighting), Grade.G4.getColor());
		AppUtils.addSerieToPieChart(pieChart, getString(Grade.G5.getStringResID()),
				AppUtils.getGradeCount(this.mGrades, Grade.G5, ectsWeighting), Grade.G5.getColor());

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
		inflater.inflate(R.menu.grade, menu);
	}
}
