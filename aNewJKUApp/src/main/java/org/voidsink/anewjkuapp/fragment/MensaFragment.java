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

package org.voidsink.anewjkuapp.fragment;

import android.graphics.Color;

import org.voidsink.anewjkuapp.MensaDayTabItem;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MensaFragment extends SlidingTabsFragment {

    @Override
    protected void fillTabs(List<SlidingTabItem> mTabs) {
        if (PreferenceWrapper.getGroupMenuByDay(getContext())) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            // jump to next day if later than 4pm
            if (cal.get(Calendar.HOUR_OF_DAY) >= 16) {
                cal.add(Calendar.DATE, 1);
            }
            // add days until next friday
            do {
                // do not add weekend (no menu)
                if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    mTabs.add(new MensaDayTabItem(getTabTitle(cal), cal.getTime(), CalendarUtils.COLOR_DEFAULT_EXAM, Color.GRAY));
                }
                // increment day
                cal.add(Calendar.DATE, 1);
            } while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY);
        } else {
            mTabs.add(new SlidingTabItem(getString(R.string.mensa_title_classic), MensaClassicFragment.class));
            mTabs.add(new SlidingTabItem(getString(R.string.mensa_title_choice), MensaChoiceFragment.class));
            mTabs.add(new SlidingTabItem(getString(R.string.mensa_title_khg), MensaKHGFragment.class));
            mTabs.add(new SlidingTabItem(getString(R.string.mensa_title_raab), MensaRaabFragment.class));
        }
    }

    private String getTabTitle(final Calendar cal) {
        final Calendar now = Calendar.getInstance();
        if (now.get(Calendar.DATE) == cal.get(Calendar.DATE)) {
            return getResources().getString(R.string.mensa_menu_today);
        } else if (cal.get(Calendar.DATE) - now.get(Calendar.DATE) == 1) {
            return getResources().getString(R.string.mensa_menu_tomorrow);
        }
        return new SimpleDateFormat("EEEE").format(cal.getTime());
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_MENSA;
    }
}
