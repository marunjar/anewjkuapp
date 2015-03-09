package org.voidsink.anewjkuapp.base;

import android.os.Bundle;

import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
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
