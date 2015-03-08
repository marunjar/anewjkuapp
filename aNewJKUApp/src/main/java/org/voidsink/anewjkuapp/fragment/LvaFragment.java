package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.TermTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;
import org.voidsink.anewjkuapp.kusss.Term;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.Arrays;
import java.util.List;

public class LvaFragment extends SlidingTabsFragment {

    @Override
    protected void fillTabs(List<SlidingTabItem> mTabs) {
        List<Term> mTerms = KusssContentProvider.getTerms(getContext());

        mTabs.add(new TermTabItem(getString(R.string.all_terms), null, LvaDetailFragment.class));

        for (Term term : mTerms) {
            mTabs.add(new TermTabItem(term.getTerm(), Arrays.asList(term.getTerm()), LvaDetailFragment.class));
        }
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_COURSES;
    }
}
