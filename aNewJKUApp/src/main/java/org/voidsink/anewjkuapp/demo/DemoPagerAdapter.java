package org.voidsink.anewjkuapp.demo;

import org.voidsink.anewjkuapp.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DemoPagerAdapter extends FragmentStatePagerAdapter {

	public DemoPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int i) {
		Fragment fragment = new DemoObjectFragment();
		Bundle args = new Bundle();
		// Our object is just an integer :-P
		args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public int getCount() {
		return 9;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return "OBJECT " + (position + 1);
	}

	// Instances of this class are fragments representing a single
	// object in our collection.
	public static class DemoObjectFragment extends Fragment {
		public static final String ARG_OBJECT = "object";

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// The last two arguments ensure LayoutParams are inflated
			// properly.
			View view = inflater.inflate(R.layout.fragment_demo_object,
					container, false);

			Bundle args = getArguments();
			((TextView) view.findViewById(R.id.text1)).setText(Integer
					.toString(args.getInt(ARG_OBJECT)));
			return view;
		}
	}
}
