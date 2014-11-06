package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.utils.Consts;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class OehRightsFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_oeh_rights, container,
				false);

		((TextView) view.findViewById(R.id.oeh_rights_1_summary))
				.setMovementMethod(LinkMovementMethod.getInstance());
		((TextView) view.findViewById(R.id.oeh_rights_2_summary))
				.setMovementMethod(LinkMovementMethod.getInstance());
		((TextView) view.findViewById(R.id.oeh_rights_3_summary))
				.setMovementMethod(LinkMovementMethod.getInstance());
		((TextView) view.findViewById(R.id.oeh_rights_4_summary))
				.setMovementMethod(LinkMovementMethod.getInstance());
		((TextView) view.findViewById(R.id.oeh_rights_5_summary))
				.setMovementMethod(LinkMovementMethod.getInstance());
		((TextView) view.findViewById(R.id.oeh_rights_6_summary))
				.setMovementMethod(LinkMovementMethod.getInstance());

		return view;
	}

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_OEH_RIGHTS;
    }
}
