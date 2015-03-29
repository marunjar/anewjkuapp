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

package org.voidsink.anewjkuapp.base;

import android.os.Bundle;

import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class TermFragment extends BaseFragment {

    private List<Term> mTerms;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        if (args != null) {
            String[] termArray = args.getStringArray(Consts.ARG_TERMS);

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
    }

    protected List<Term> getTerms() {
        return mTerms;
    }

}
