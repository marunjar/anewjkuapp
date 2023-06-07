/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2023 Paul "Marunjar" Pretsch
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;
import org.voidsink.anewjkuapp.utils.Consts;

public class BaseFragment extends Fragment implements StackedFragment, PendingIntentHandler {

    private static final Logger logger = LoggerFactory.getLogger(StackedFragment.class);

    private CharSequence mTitle = null;
    private int mId = 0;
    private ContentLoadingProgressBar mProgress;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            mId = savedInstanceState.getInt(Consts.ARG_FRAGMENT_ID, mId);
            mTitle = savedInstanceState.getCharSequence(Consts.ARG_FRAGMENT_TITLE);
        }

        mProgress = view.findViewById(R.id.load_progress_bar);
        if (mProgress != null) {
            finishProgress();
        }
    }

    protected void showProgressIndeterminate() {
        if (mProgress != null) {
            mProgress.show();
        }
    }

    protected void finishProgress() {
        if (mProgress != null) {
            mProgress.hide();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(Consts.ARG_FRAGMENT_ID, mId);
        outState.putCharSequence(Consts.ARG_FRAGMENT_TITLE, mTitle);
    }

    public final void handleIntent() {
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).handlePendingIntent(this);
        } else {
            logger.warn("no activity, can't handle intent");
        }
    }

    @Override
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
            AnalyticsHelper.sendScreen(screenName);
        }

        handleIntent();
    }

    /*
     * returns screen name for logging activity
     */
    @Nullable
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() instanceof ThemedActivity) {
            ((ThemedActivity) getActivity()).initActionBar(this);
        }
    }
}
