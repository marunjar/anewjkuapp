/*******************************************************************************
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 ******************************************************************************/

package org.voidsink.anewjkuapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.aboutlibraries.LibsBuilder;

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

                        new LibsBuilder()
                                .withFields(R.string.class.getFields()) // pass the fields of your application to the lib so it can find all external lib information
                                .withLibraries("jsoup")
                                .withAutoDetect(true)
                                .withVersionShown(false)
                                .withLicenseShown(true)
                                .withActivityTheme(UIUtils.getAppThemeResId(getActivity())) // must be AppCompat theme
                                .withAboutAppName(getString(R.string.app_name))
                                .withAboutIconShown(true)
                                .withAboutVersionShown(true)
                                .start(getActivity());
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
