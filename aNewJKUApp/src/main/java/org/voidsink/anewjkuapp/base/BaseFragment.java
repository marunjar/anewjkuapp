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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.utils.Consts;

public class BaseFragment extends Fragment implements StackedFragment {

    private static final String TAG = BaseFragment.class.getSimpleName();
    private Intent mPendingIntent = null;
    private CharSequence mTitle = null;
    private int mId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Log.i(getClass().getSimpleName(), "onCreate");
        setHasOptionsMenu(true);
    }

    protected Context getContext() {
        return this.getActivity();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            mId = savedInstanceState.getInt(Consts.ARG_FRAGMENT_ID, mId);
            mTitle = savedInstanceState.getCharSequence(Consts.ARG_FRAGMENT_TITLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected");

        switch (item.getItemId()) {
            case R.id.action_refresh_calendar:
                return onRefreshSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected boolean onRefreshSelected(MenuItem item) {
        Log.d(TAG, "onRefreshSelected");
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(Consts.ARG_FRAGMENT_ID, mId);
        outState.putCharSequence(Consts.ARG_FRAGMENT_TITLE, mTitle);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroyOptionsMenu() {
        // Log.i(getClass().getSimpleName(), "onDestroyOptionsMenu");
        super.onDestroyOptionsMenu();
    }

    public final void handleIntent(Intent intent) {
        if (getContext() == null) {
            this.mPendingIntent = intent;
        } else {
            handlePendingIntent(intent);
            this.mPendingIntent = null;
        }
    }

    public void handlePendingIntent(Intent intent) {
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        if (args != null) {
            if (args.containsKey(Consts.ARG_FRAGMENT_TITLE)) {
                mTitle = args.getCharSequence(Consts.ARG_FRAGMENT_TITLE);
            }
            if (args.containsKey(Consts.ARG_FRAGMENT_ID)) {
                mId = args.getInt(Consts.ARG_FRAGMENT_ID);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        final String screenName = getScreenName();
        if (screenName != null && !screenName.isEmpty()) {
            Analytics.sendScreen(getContext(), screenName);
        }

        if (mPendingIntent != null) {
            if (getContext() != null) {
                handlePendingIntent(mPendingIntent);
            } else {
                Log.w(TAG, "context not set, can't call handlePendingIntent");
            }
            this.mPendingIntent = null;
        }
    }

    /*
     * returns screen name for logging activity
     */
    protected String getScreenName() {
        return null;
    }

    @Override
    public boolean getDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    public CharSequence getTitle(Context context) {
        return mTitle;
    }

    @Override
    public int getId(Context context) {
        return mId;
    }
}
