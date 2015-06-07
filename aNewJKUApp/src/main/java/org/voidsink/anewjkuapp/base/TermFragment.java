/*
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
 *
 */

package org.voidsink.anewjkuapp.base;

import android.os.Bundle;

import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class TermFragment extends BaseFragment {

    private List<Term> mTerms;

    private void fillTermArray(String[] termArray) {
        if (termArray != null && termArray.length > 0) {

            try {
                mTerms = new ArrayList<>();
                for (String termStr : termArray) {
                    mTerms.add(Term.parseTerm(termStr));
                }
            } catch (ParseException e) {
                mTerms = null;
                Analytics.sendException(getContext(), e, true);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            fillTermArray(savedInstanceState.getStringArray(Consts.ARG_TERMS));
        }
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        if (args != null) {
            fillTermArray(args.getStringArray(Consts.ARG_TERMS));
        }
    }

    protected List<Term> getTerms() {
        return mTerms;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mTerms != null) {
            String[] termArray = new String[mTerms.size()];
            for (int i = 0; i < mTerms.size(); i++) {
                termArray[i] = mTerms.get(i).toString();
            }
            outState.putStringArray(Consts.ARG_TERMS, termArray);
        }
    }
}
