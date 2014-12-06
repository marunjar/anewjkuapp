package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.LvaTabItem;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;
import org.voidsink.anewjkuapp.provider.KusssContentProvider;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.Arrays;
import java.util.List;

public class LvaFragment extends SlidingTabsFragment {

    @Override
    protected void fillTabs(List<SlidingTabItem> mTabs) {
        List<String> mTerms = KusssContentProvider.getTerms(getContext());

        mTabs.add(new LvaTabItem(getString(R.string.all_terms), null));

        for (String term: mTerms) {
            mTabs.add(new LvaTabItem(term, Arrays.asList(term)));
        }
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_LVAS;
    }
}
