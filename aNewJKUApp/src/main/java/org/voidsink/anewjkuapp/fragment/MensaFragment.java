package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;

import java.util.List;

public class MensaFragment extends SlidingTabsFragment {

    @Override
    protected void fillTabs(List<SlidingTabItem> mTabs) {
        final int indicatorColor = getResources().getColor(android.R.color.white);
        final int dividerColor = getResources().getColor(R.color.primary);

        mTabs.add(new SlidingTabItem("Classic", MensaClassicFragment.class, indicatorColor, dividerColor));
        mTabs.add(new SlidingTabItem("Choice", MensaChoiceFragment.class, indicatorColor, dividerColor));
        mTabs.add(new SlidingTabItem("KHG", MensaKHGFragment.class, indicatorColor, dividerColor));
        mTabs.add(new SlidingTabItem("Raab", MensaRaabFragment.class, indicatorColor, dividerColor));
    }
}
