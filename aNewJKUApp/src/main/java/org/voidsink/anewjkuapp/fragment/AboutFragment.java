/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2018 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp.fragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.util.Colors;

import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.utils.Consts;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import de.cketti.library.changelog.ChangeLog;

public class AboutFragment extends BaseFragment {

    private Libs.ActivityStyle getActivityStyle(Context context) {
        if (PreferenceWrapper.getUseLightDesign(context)) {
            return Libs.ActivityStyle.LIGHT_DARK_TOOLBAR;
        } else {
            return Libs.ActivityStyle.DARK;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        view.findViewById(R.id.about_libraries)
                .setOnClickListener(v -> new LibsBuilder()
                        .withFields(R.string.class.getFields()) // pass the fields of your application to the lib so it can find all external lib information
                        .withAutoDetect(true)
                        .withVersionShown(false)
                        .withLicenseShown(true)
                        .withActivityStyle(getActivityStyle(getActivity()))
                        .withActivityColor(getActivityColor(getActivity()))
                        .withActivityTitle(getActivity().getString(R.string.title_about))
                        .withAboutAppName(getString(R.string.app_name))
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
                        .start(getActivity()));

        view.findViewById(R.id.about_changelog)
                .setOnClickListener(v -> new ChangeLog(getActivity()).getFullLogDialog().show());

        return view;
    }

    private Colors getActivityColor(Context context) {

        TypedArray themeArray = context.getTheme().obtainStyledAttributes(new int[]{R.attr.colorPrimary, R.attr.colorPrimaryDark});
        int colorPrimary = themeArray.getColor(0, ContextCompat.getColor(context, R.color.default_primary));
        int colorPrimaryDark = themeArray.getColor(1, ContextCompat.getColor(context, R.color.default_primaryDark));
        themeArray.recycle();

        return new Colors(colorPrimary, colorPrimaryDark);
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_ABOUT;
    }

    @Override
    public CharSequence getTitle(Context context) {
        return context.getString(R.string.title_about);
    }
}
