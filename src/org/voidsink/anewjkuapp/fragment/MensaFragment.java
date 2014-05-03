package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public class MensaFragment extends BaseFragment {

	private static final String ARG_TAB_NO = "TAB_NO";
	private FragmentTabHost mTabHost;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mTabHost = new FragmentTabHost(getActivity());
		mTabHost.setup(getActivity(), getChildFragmentManager(),
				R.id.mensa_tabs);

		mTabHost.addTab(mTabHost.newTabSpec("classic").setIndicator("Classic"),
				MensaClassicFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("choice").setIndicator("Choice"),
				MensaChoiceFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("khg").setIndicator("KHG"),
				MensaKHGFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec("raab").setIndicator("Raab"),
				MensaRaabFragment.class, null);

		if (savedInstanceState != null) {
			mTabHost.setCurrentTab(Math.min(savedInstanceState.getInt(
					ARG_TAB_NO, 0), mTabHost.getTabWidget().getTabCount()));
		}

		return mTabHost;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(ARG_TAB_NO, mTabHost.getCurrentTab());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mTabHost = null;
	}

}
