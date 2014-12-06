package org.voidsink.anewjkuapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.ui.LibsActivity;

import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.anewjkuapp.utils.UIUtils;
import org.voidsink.library.contributors.Contributors;

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

                        //Create an intent with context and the Activity class
                        Intent i = new Intent(getActivity().getApplicationContext(), LibsActivity.class);
                        //Pass the fields of your application to the lib so it can find all external lib information
                        i.putExtra(Libs.BUNDLE_FIELDS, Libs.toStringArray(R.string.class.getFields()));

                        //Display the library version (OPTIONAL)
                        i.putExtra(Libs.BUNDLE_VERSION, true);
                        //Display the library license (OPTIONAL
                        i.putExtra(Libs.BUNDLE_LICENSE, true);

                        //Pass your theme (must be AppCompat theme)
                        //i.putExtra(Libs.BUNDLE_THEME, UIUtils.getAppThemeResId(getActivity()));

                        //Pass a custom accent color (OPTIONAL)

                        //start the activity
                        startActivity(i);
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
