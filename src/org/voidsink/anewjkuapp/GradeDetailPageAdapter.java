package org.voidsink.anewjkuapp;

import java.util.ArrayList;
import java.util.List;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.fragment.GradeDetailFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class GradeDetailPageAdapter extends FragmentStatePagerAdapter {

	public static final String TAG = GradeDetailPageAdapter.class.getSimpleName();
	private List<ExamGrade> mGrades;
	private ArrayList<String> mTerms;
	private Context mContext;

	public GradeDetailPageAdapter(FragmentManager fm, Context context) {
		super(fm);

		this.mContext = context;
		this.mGrades = new ArrayList<ExamGrade>();
		this.mTerms = new ArrayList<String>();
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			return new GradeDetailFragment(this.mGrades);
		default:
			if (position - 1 < this.mTerms.size()) {
				return new GradeDetailFragment(this.mTerms.get(position - 1), this.mGrades);
			}
			return null;
		}
	}

	@Override
	public int getCount() {
		return this.mTerms.size() + 1;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return mContext.getString(R.string.lva_all_terms);
		default:
			if (position - 1 < this.mTerms.size()) {
				return this.mTerms.get(position - 1);
			}
			return null;
		}
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	public void setData(List<ExamGrade> grades,
			ArrayList<String> terms) {
		this.mGrades = grades;
		this.mTerms = terms;

		notifyDataSetChanged();
	}
}
