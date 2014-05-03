package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container, false);

		((TextView) view.findViewById(R.id.about_me))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// send me an eMail
						Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
								Uri.fromParts("mailto", "paul.pretsch@aon.at",
										null));
						emailIntent.putExtra(
								android.content.Intent.EXTRA_SUBJECT,
								getString(R.string.app_name));
						emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
								"Hallo,\n\n...");

						startActivity(Intent.createChooser(emailIntent,
								"Send email..."));
					}
				});

		return view;
	}
}
