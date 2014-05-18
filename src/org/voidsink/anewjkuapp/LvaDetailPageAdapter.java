package org.voidsink.anewjkuapp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.fragment.LvaDetailFragment;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;

import edu.emory.mathcs.backport.java.util.Collections;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

public class LvaDetailPageAdapter extends FragmentStatePagerAdapter {

	public static final String TAG = LvaDetailPageAdapter.class.getSimpleName();
	private List<Lva> mLvas;
	private List<ExamGrade> mGrades;
	private ArrayList<String> mTerms;
	private Context mContext;

	public LvaDetailPageAdapter(FragmentManager fm, Context context) {
		super(fm);

		this.mContext = context;

		LvaContentObserver mLvaObserver = new LvaContentObserver(new Handler());
		context.getContentResolver().registerContentObserver(
				KusssContentContract.Lva.CONTENT_URI, false, mLvaObserver);

		this.mLvas = new ArrayList<Lva>();
		this.mGrades = new ArrayList<ExamGrade>();
		this.mTerms = new ArrayList<String>();

		loadContent(mContext);
	}

	private void loadContent(Context context) {
		new AsyncTask<Void, Void, Void>() {

			private ProgressDialog progressDialog;
			private List<Lva> lvas;
			private List<ExamGrade> grades;
			private ArrayList<String> terms;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressDialog = ProgressDialog.show(mContext,
						mContext.getString(R.string.progress_title),
						mContext.getString(R.string.progress_load_lva), true);
			}

			@Override
			protected Void doInBackground(Void... params) {
				this.lvas = KusssContentProvider.getLvas(mContext);
				this.grades = KusssContentProvider.getGrades(mContext);
				this.terms = new ArrayList<String>();
				for (Lva lva : this.lvas) {
					if (this.terms.indexOf(lva.getTerm()) < 0) {
						this.terms.add(lva.getTerm());
					}
				}
				Collections.sort(this.terms, new Comparator<String>() {

					@Override
					public int compare(String lhs, String rhs) {
						return lhs.compareTo(rhs) * -1;
					}
				});
				AppUtils.sortLVAs(this.lvas);

				System.out.println(this.terms);
				
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				progressDialog.dismiss();

				mLvas = this.lvas;
				mGrades = this.grades;
				mTerms = this.terms;

				notifyDataSetChanged();
				super.onPostExecute(result);
			}
		}.execute();
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

	private class LvaContentObserver extends ContentObserver {

		public LvaContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Log.i(TAG, "lvas changed");
			loadContent(mContext);
		}
	}
}
