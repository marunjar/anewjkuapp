package org.voidsink.anewjkuapp.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.pie.PieChart;

import org.voidsink.anewjkuapp.AppUtils;
import org.voidsink.anewjkuapp.GradeCard;
import org.voidsink.anewjkuapp.GradeCardArrayAdapter;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.view.GradeCardListView;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;

@SuppressLint("ValidFragment")
public class GradeDetailFragment extends BaseFragment {

	public static final String TAG = GradeDetailFragment.class.getSimpleName();

	private GradeCardListView mListView;
	private GradeCardArrayAdapter mAdapter;

	private List<ExamGrade> mGrades;

    private void addIfRecent(List<ExamGrade> grades, ExamGrade grade) {
		int i = 0;
		while (i < grades.size()) {
			ExamGrade g = grades.get(i);
			// check only grades for same lva and term
			if (g.getCode().equals(grade.getCode())
					&& g.getLvaNr().equals(grade.getLvaNr())) {
				// keep only recent (best and newest) grade
				if (g.getDate().before(grade.getDate())) {
					// remove last grade
					grades.remove(i);
				} else {
					// break without adding
					return;
				}
			} else {
				i++;
			}
		}
		// finally add grade
		grades.add(grade);
	}

	public GradeDetailFragment(String term, List<ExamGrade> grades) {
        this.mGrades = new ArrayList<ExamGrade>();
		if (grades != null) {
			for (ExamGrade grade : grades) {
				if (term == null || grade.getTerm().equals(term)) {
					addIfRecent(this.mGrades, grade);
				}
			}
		}
        AppUtils.sortGrades(this.mGrades);
	}

	public GradeDetailFragment(List<ExamGrade> grades) {
		this(null, grades);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_card_grade_detail, container,
				false);

		mListView = (GradeCardListView) view.findViewById(R.id.grade_card_list);

        List<Card> gradeCards = new ArrayList<>();
        for (ExamGrade g : this.mGrades) {
            gradeCards.add(new GradeCard(getContext(), g));
        }
        mAdapter = new GradeCardArrayAdapter(getContext(), gradeCards);
		mListView.setAdapter(mAdapter);

		PieChart pieChart = (PieChart) view.findViewById(R.id.grade_pie_chart);

		boolean ectsWeighting = false;

		// init pie chart
		AppUtils.addSerieToPieChart(pieChart,
				getString(Grade.G1.getStringResID()),
				AppUtils.getGradeCount(this.mGrades, Grade.G1, ectsWeighting),
				Grade.G1.getColor());
		AppUtils.addSerieToPieChart(pieChart,
				getString(Grade.G2.getStringResID()),
				AppUtils.getGradeCount(this.mGrades, Grade.G2, ectsWeighting),
				Grade.G2.getColor());
		AppUtils.addSerieToPieChart(pieChart,
				getString(Grade.G3.getStringResID()),
				AppUtils.getGradeCount(this.mGrades, Grade.G3, ectsWeighting),
				Grade.G3.getColor());
		AppUtils.addSerieToPieChart(pieChart,
				getString(Grade.G4.getStringResID()),
				AppUtils.getGradeCount(this.mGrades, Grade.G4, ectsWeighting),
				Grade.G4.getColor());
		AppUtils.addSerieToPieChart(pieChart,
				getString(Grade.G5.getStringResID()),
				AppUtils.getGradeCount(this.mGrades, Grade.G5, ectsWeighting),
				Grade.G5.getColor());

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
