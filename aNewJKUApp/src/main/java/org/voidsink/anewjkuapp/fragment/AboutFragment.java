package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.library.contributors.Contributors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.cketti.library.changelog.ChangeLog;

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

        (view.findViewById(R.id.about_changelog))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ChangeLog(getActivity()).getFullLogDialog().show();
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

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_ABOUT;
    }
}
