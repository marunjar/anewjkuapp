/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2020 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.List;

public class TermTabItem extends SlidingTabItem {

    private final List<Term> mTerms;

    public TermTabItem(CharSequence title, List<Term> terms, Class<? extends Fragment> fragment) {
        super(title, fragment);
        this.mTerms = terms;
    }

    @Override
    protected void getArguments(@NonNull Bundle arguments) {
        if (mTerms != null) {

            String[] mTermStrings = new String[mTerms.size()];
            for (int i = 0; i < mTerms.size(); i++) {
                mTermStrings[i] = mTerms.get(i).toString();
            }

            arguments.putStringArray(Consts.ARG_TERMS, mTermStrings);
        }
    }
}
