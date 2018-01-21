/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2017 Paul "Marunjar" Pretsch
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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.utils.Consts;

public class OehRightsFragment extends BaseFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
