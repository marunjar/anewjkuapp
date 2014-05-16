package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MensaPagerAdapter extends FragmentStatePagerAdapter {

	public MensaPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int i) {
		switch (i) {
		case 0:
			return new MensaClassicFragment();
		case 1:
			return new MensaChoiceFragment();
		case 2:
			return new MensaKHGFragment();
		case 3:
			return new MensaRaabFragment();
		default:
			return null;
		}
	}

	@Override
	public int getCount() {
		return 4;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return "Classic";
		case 1:
			return "Choice";
		case 2:
			return "KHG";
		case 3:
			return "Raab";
		default:
			return null;
		}
	}
}
