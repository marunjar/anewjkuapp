package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class OehInfoFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_oeh_info, container,
				false);

		((TextView) view.findViewById(R.id.oeh_info_main_summary))
				.setMovementMethod(LinkMovementMethod.getInstance());
		((TextView) view.findViewById(R.id.oeh_info_jku_summary))
				.setMovementMethod(LinkMovementMethod.getInstance());
		((TextView) view.findViewById(R.id.oeh_info_contact_summary))
				.setMovementMethod(LinkMovementMethod.getInstance());

		return view;
	}

}
