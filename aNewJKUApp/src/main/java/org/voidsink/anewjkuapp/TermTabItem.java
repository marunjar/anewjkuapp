package org.voidsink.anewjkuapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;

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

    protected List<Term> getTerms() {
        return mTerms;
    }

    protected Bundle getArguments() {
        Bundle b = new Bundle();

        if (mTerms != null) {

            String[] mTermStrings = new String[mTerms.size()];
            for (int i = 0; i < mTerms.size(); i++) {
                mTermStrings[i] = mTerms.get(i).toString();
            }

            b.putStringArray(Consts.ARG_TERMS, mTermStrings);
        }

        return b;
    }
}
