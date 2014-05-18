package org.voidsink.anewjkuapp.fragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;

import edu.emory.mathcs.backport.java.util.Collections;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class StatisticPageAdapter extends FragmentStatePagerAdapter {

	private List<Lva> mLvas;
	private List<ExamGrade> mGrades;
	private ArrayList<String> mTerms;

	public StatisticPageAdapter(FragmentManager fm, Context context) {
		super(fm);

		this.mLvas = KusssContentProvider.getLvas(context);
		this.mGrades = KusssContentProvider.getGrades(context);
		this.mTerms = new ArrayList<String>();
		for (Lva lva : this.mLvas) {
			if (this.mTerms.indexOf(lva.getTerm()) < 0) {
				this.mTerms.add(lva.getTerm());
			}
		}
		Collections.sort(this.mTerms, new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				return lhs.compareTo(rhs) * -1;
			}
		});
		System.out.println(this.mTerms);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			return new StatisticDetailFragment(this.mTerms, this.mLvas,
					this.mGrades);
		default:
			if (position - 1 < this.mTerms.size()) {
				return new StatisticDetailFragment(
						this.mTerms.get(position - 1), this.mLvas, this.mGrades);
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
			return "gesamt";
		default:
			if (position - 1 < this.mTerms.size()) {
				return this.mTerms.get(position - 1);
			}
			return null;
		}
	}
}
