package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.library.contributors.Contributors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class AboutFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container, false);

		((Button) view.findViewById(R.id.about_credits))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Contributors contributors = new Contributors(
								getContext());
						contributors.getDialog(R.xml.credits).show();
					}
				});

		((Button) view.findViewById(R.id.about_libraries))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Contributors contributors = new Contributors(
								getContext());
						contributors.getDialog(R.xml.libraries).show();
					}
				});

		return view;
	}
}
