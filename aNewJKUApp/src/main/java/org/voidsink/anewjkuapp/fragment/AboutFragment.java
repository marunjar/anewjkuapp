package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.service.KusssService;
import org.voidsink.library.contributors.Contributors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container, false);

		(view.findViewById(R.id.about_credits))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Contributors contributors = new Contributors(
								getContext());
						contributors.getDialog(R.xml.credits).show();
					}
				});

		(view.findViewById(R.id.about_libraries))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Contributors contributors = new Contributors(
								getContext());
						contributors.getDialog(R.xml.libraries).show();
					}
				});

//        view.findViewById(R.id.force_logout).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                KusssHandler.getInstance().logout(getContext());
//            }
//        });

		return view;
	}
}
