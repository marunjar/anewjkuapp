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

package org.voidsink.anewjkuapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.voidsink.anewjkuapp.MensaInfoItem;
import org.voidsink.anewjkuapp.MensaItem;
import org.voidsink.anewjkuapp.MensaMenuAdapter;
import org.voidsink.anewjkuapp.MensaMenuItem;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseAsyncTaskLoader;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.mensa.ChoiceMenuLoader;
import org.voidsink.anewjkuapp.mensa.ClassicMenuLoader;
import org.voidsink.anewjkuapp.mensa.IDay;
import org.voidsink.anewjkuapp.mensa.IMensa;
import org.voidsink.anewjkuapp.mensa.IMenu;
import org.voidsink.anewjkuapp.mensa.KHGMenuLoader;
import org.voidsink.anewjkuapp.mensa.MenuLoader;
import org.voidsink.anewjkuapp.mensa.RaabMenuLoader;
import org.voidsink.anewjkuapp.mensa.TagestellerMenuLoader;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.sectionedrecycleradapter.SectionedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MensaDayFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<List<MensaItem>> {

    private Date mDate;
    private MensaMenuAdapter mAdapter;
    private RecyclerView mRecyclerView;

    public MensaDayFragment() {
        super();

        this.mDate = new Date();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mAdapter = new MensaMenuAdapter(getContext(), false);
        mRecyclerView.setAdapter(new SectionedRecyclerViewAdapter<>(mRecyclerView, mAdapter));
        mRecyclerView.setContentDescription(getTitle(getContext()));

        LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    private void updateData() {
        if (this.isVisible() && !LoaderManager.getInstance(this).hasRunningLoaders()) {
            LoaderManager.getInstance(this).restartLoader(0, null, this);
        }
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container,
                false);

        mRecyclerView = view.findViewById(R.id.recyclerView);

        return view;
    }

    public void setDate(Date mDate) {
        this.mDate = mDate;
    }

    protected List<MenuLoader> createLoaders() {
        List<MenuLoader> loaders = new ArrayList<>();

        loaders.add(new ClassicMenuLoader());
        loaders.add(new TagestellerMenuLoader());
        loaders.add(new ChoiceMenuLoader());
        loaders.add(new KHGMenuLoader());
        loaders.add(new RaabMenuLoader());

        return loaders;
    }

    @NonNull
    @Override
    public Loader<List<MensaItem>> onCreateLoader(int id, @Nullable Bundle args) {
        showProgressIndeterminate();

        return new MenuDayLoader(getContext(), mDate, createLoaders());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<MensaItem>> loader, List<MensaItem> data) {
        mAdapter.clear();
        if (data != null) {
            mAdapter.addAll(data);
        }
        mAdapter.notifyDataSetChanged();

        finishProgress();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<MensaItem>> loader) {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();

        finishProgress();
    }

    private static class MenuDayLoader extends BaseAsyncTaskLoader<List<MensaItem>> {
        private final Date mDate;
        private final List<MenuLoader> mMenuLoaders;

        MenuDayLoader(Context c, Date date, List<MenuLoader> mMenuLoaders) {
            super(c);
            this.mDate = date;
            this.mMenuLoaders = mMenuLoaders;
        }

        @Override
        public List<MensaItem> loadInBackground() {
            List<MensaItem> menus = new ArrayList<>();
            int noMenuCount = 0;

            for (MenuLoader menuLoader : mMenuLoaders) {
                IMensa mensa = menuLoader.getMensa(getContext());
                if (mensa != null) {
                    IDay day = mensa.getDay(mDate);
                    if (day != null && !day.isEmpty()) {
                        for (IMenu menu : day.getMenus()) {
                            menus.add(new MensaMenuItem(mensa, day, menu));
                        }
                    } else {
                        // add no menu card
                        menus.add(new MensaInfoItem(mensa, day, getContext().getString(R.string.mensa_menu_not_available), null));
                        noMenuCount++;
                    }
                }
            }

            // add default no menu card
            if (menus.size() == 0 || menus.size() == noMenuCount) {
                menus.clear();
                menus.add(new MensaInfoItem(null, null, getContext().getString(R.string.mensa_menu_not_available), null));
            }

            return menus;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.mensa_day, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_refresh_mensa_day) {
            updateData();
            return true;
        } else if (itemId == R.id.action_open_in_browser_classic) {
            if (getContext() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.MENSA_MENU_CLASSIC));
                getContext().startActivity(intent);
            }
            return true;
        } else if (itemId == R.id.action_open_in_browser_choice) {
            if (getContext() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.MENSA_MENU_CHOICE));
                getContext().startActivity(intent);
            }
            return true;
        } else if (itemId == R.id.action_open_in_browser_khg) {
            if (getContext() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.MENSA_MENU_KHG));
                getContext().startActivity(intent);
            }
            return true;
        } else if (itemId == R.id.action_open_in_browser_raab) {
            if (getContext() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.MENSA_MENU_RAAB));
                getContext().startActivity(intent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
