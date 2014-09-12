package org.voidsink.anewjkuapp.fragment;

import android.graphics.Color;
import android.provider.CalendarContract;

import org.voidsink.anewjkuapp.AppUtils;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;

import java.util.List;

public class MensaFragment extends SlidingTabsFragment {

    @Override
    protected void fillTabs(List<SlidingTabItem> mTabs) {

        mTabs.add(new SlidingTabItem("Classic", MensaClassicFragment.class, CalendarUtils.COLOR_DEFAULT_LVA, Color.GRAY));
        mTabs.add(new SlidingTabItem("Choice", MensaChoiceFragment.class, CalendarUtils.COLOR_DEFAULT_EXAM, Color.GRAY));
        mTabs.add(new SlidingTabItem("KHG", MensaKHGFragment.class, CalendarUtils.COLOR_DEFAULT_LVA, Color.GRAY));
        mTabs.add(new SlidingTabItem("Raab", MensaRaabFragment.class, CalendarUtils.COLOR_DEFAULT_EXAM, Color.GRAY));
    }
}
