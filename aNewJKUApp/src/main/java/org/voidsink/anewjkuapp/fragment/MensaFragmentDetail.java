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

package org.voidsink.anewjkuapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.MensaInfoItem;
import org.voidsink.anewjkuapp.MensaItem;
import org.voidsink.anewjkuapp.MensaMenuAdapter;
import org.voidsink.anewjkuapp.MensaMenuItem;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseAsyncTaskLoader;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.mensa.IDay;
import org.voidsink.anewjkuapp.mensa.IMensa;
import org.voidsink.anewjkuapp.mensa.IMenu;
import org.voidsink.anewjkuapp.mensa.MenuLoader;
import org.voidsink.sectionedrecycleradapter.SectionedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Calendar;

public abstract class MensaFragmentDetail extends BaseFragment implements LoaderManager.LoaderCallbacks<ArrayList<MensaItem>> {

    public static final String TAG = MensaFragmentDetail.class.getSimpleName();
    private MensaMenuAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        RecyclerView mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mAdapter = new MensaMenuAdapter(getContext(), true);
        mRecyclerView.setAdapter(new SectionedRecyclerViewAdapter(mRecyclerView, mAdapter));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    private void updateData() {
        if (this.isVisible() && !getLoaderManager().hasRunningLoaders()) {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    protected abstract MenuLoader createLoader();

    @Override
    public Loader<ArrayList<MensaItem>> onCreateLoader(int i, Bundle bundle) {
        showProgressIndeterminate();

        return new MenuDetailLoader(getContext(), createLoader());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<MensaItem>> loader, ArrayList<MensaItem> mensaItems) {
        mAdapter.clear();
        if (mensaItems != null) {
            mAdapter.addAll(mensaItems);
        }
        mAdapter.notifyDataSetChanged();

        finishProgress();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<MensaItem>> loader) {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();

        finishProgress();
    }

    private static class MenuDetailLoader extends BaseAsyncTaskLoader<ArrayList<MensaItem>> {

        private final MenuLoader mMenuLoader;

        public MenuDetailLoader(Context context, MenuLoader menuLoader) {
            super(context);

            this.mMenuLoader = menuLoader;
        }

        @Override
        public ArrayList<MensaItem> loadInBackground() {
            ArrayList<MensaItem> mMenus = new ArrayList<>();

            final IMensa mensa = mMenuLoader.getMensa(MenuDetailLoader.this.getContext());

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            // set date to start of week
            cal.add(Calendar.DAY_OF_YEAR, -cal.get(Calendar.DAY_OF_WEEK) + cal.getFirstDayOfWeek());

            if (mensa != null) {
                for (IDay day : mensa.getDays()) {
                    // allow only menus >= start of this week
                    if ((day.getDate() != null) && (day.getDate().getTime() >= cal.getTimeInMillis())) {
                        for (IMenu menu : day.getMenus()) {
                            mMenus.add(new MensaMenuItem(mensa, day, menu));
                            // remember position of menu for today for scrolling to item after update
                        }
                    }
                }
            }
            if (mMenus.size() == 0) {
                mMenus.add(new MensaInfoItem(mensa, null, MenuDetailLoader.this.getContext().getString(R.string.mensa_menu_not_available), null));
            }
            return mMenus;
        }
    }
}
