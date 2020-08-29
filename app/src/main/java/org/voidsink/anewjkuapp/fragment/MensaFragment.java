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

package org.voidsink.anewjkuapp.fragment;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.voidsink.anewjkuapp.MensaDayTabItem;
import org.voidsink.anewjkuapp.PreferenceHelper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.base.SlidingTabsFragment;
import org.voidsink.anewjkuapp.utils.Consts;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MensaFragment extends SlidingTabsFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    @Override
    public void onStart() {
        super.onStart();

        PreferenceManager.getDefaultSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void fillTabs(List<SlidingTabItem> mTabs) {
        if (PreferenceHelper.getGroupMenuByDay(getContext())) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            // jump to next day if later than 4pm
            if (cal.get(Calendar.HOUR_OF_DAY) >= 19) {
                cal.add(Calendar.DATE, 1);
            }
            // add days until next friday
            do {
                // do not add weekend (no menu)
                if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    mTabs.add(new MensaDayTabItem(getTabTitle(cal), cal.getTime()));
                }
                // increment day
                cal.add(Calendar.DATE, 1);
            } while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY);
        } else {
            mTabs.add(new SlidingTabItem(getString(R.string.mensa_title_classic), MensaClassicFragment.class));
            mTabs.add(new SlidingTabItem(getString(R.string.mensa_title_tagesteller), MensaTagestellerFragment.class));
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
        return new SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.getTime());
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_MENSA;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferenceHelper.PREF_MENSA_GROUP_MENU_BY_DAY)) {
            createTabs(null);
        }
    }
}
