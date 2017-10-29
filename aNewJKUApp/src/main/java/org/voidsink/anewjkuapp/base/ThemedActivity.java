/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2017 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.anewjkuapp.utils.UIUtils;

public class ThemedActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UIUtils.applyTheme(this);

        super.onCreate(savedInstanceState);

        initActionBar();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        initActionBar();
    }

    protected final void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(Consts.ARG_FRAGMENT_TAG);
            if (f instanceof StackedFragment) {
                actionBar.setDisplayHomeAsUpEnabled(((StackedFragment) f).getDisplayHomeAsUpEnabled());
                CharSequence title = ((StackedFragment) f).getTitle(this);
                if (title != null) {
                    actionBar.setTitle(title);
                } else {
                    actionBar.setTitle(R.string.app_name);
                }
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }

            onInitActionBar(actionBar);
        }
    }

    protected void onInitActionBar(ActionBar actionBar) {

    }

    @Override
    protected void onStart() {
        super.onStart();

        final String screenName = getScreenName();
        if (screenName != null && !screenName.isEmpty()) {
            Analytics.sendScreen(this, screenName);
        }
    }

    /*
     * returns screen name for logging activity
     */
    protected String getScreenName() {
        return null;
    }
}
