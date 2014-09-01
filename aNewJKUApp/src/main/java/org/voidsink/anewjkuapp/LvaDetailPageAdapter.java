package org.voidsink.anewjkuapp;

import java.util.ArrayList;
import java.util.List;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.fragment.LvaDetailFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class LvaDetailPageAdapter extends FragmentStatePagerAdapter {

	public static final String TAG = LvaDetailPageAdapter.class.getSimpleName();
	private List<Lva> mLvas;
	private List<ExamGrade> mGrades;
	private ArrayList<String> mTerms;
	private Context mContext;

	public LvaDetailPageAdapter(FragmentManager fm, Context context) {
		super(fm);

		this.mContext = context;
		this.mLvas = new ArrayList<Lva>();
		this.mGrades = new ArrayList<ExamGrade>();
		this.mTerms = new ArrayList<String>();
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			return new LvaDetailFragment(this.mTerms, this.mLvas, this.mGrades);
		default:
			if (position - 1 < this.mTerms.size()) {
				return new LvaDetailFragment(this.mTerms.get(position - 1),
						this.mLvas, this.mGrades);
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

	public void setData(List<Lva> lvas, List<ExamGrade> grades,
			ArrayList<String> terms) {
		this.mLvas = lvas;
		this.mGrades = grades;
		this.mTerms = terms;

		notifyDataSetChanged();
	}
}
