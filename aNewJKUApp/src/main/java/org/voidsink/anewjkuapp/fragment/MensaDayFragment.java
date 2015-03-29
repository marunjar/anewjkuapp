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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.voidsink.anewjkuapp.MensaInfoItem;
import org.voidsink.anewjkuapp.MensaItem;
import org.voidsink.anewjkuapp.MensaMenuAdapter;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.mensa.ChoiceMenuLoader;
import org.voidsink.anewjkuapp.mensa.ClassicMenuLoader;
import org.voidsink.anewjkuapp.mensa.KHGMenuLoader;
import org.voidsink.anewjkuapp.mensa.Mensa;
import org.voidsink.anewjkuapp.mensa.MensaDay;
import org.voidsink.anewjkuapp.mensa.MensaMenu;
import org.voidsink.anewjkuapp.mensa.MenuLoader;
import org.voidsink.anewjkuapp.mensa.RaabMenuLoader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MensaDayFragment extends BaseFragment {

    public static final String TAG = MensaDayFragment.class.getSimpleName();
    private static final List<Mensa> mMensen = new ArrayList<>();
    private Date mDate;
    private MensaMenuAdapter mAdapter;

    public MensaDayFragment() {
        super();

        this.mDate = new Date();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container,
                false);

        final RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new MensaMenuAdapter(getContext(), false);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(mAdapter));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new MenuLoadTask(new ClassicMenuLoader(), 0).execute();
        new MenuLoadTask(new ChoiceMenuLoader(), 1).execute();
        new MenuLoadTask(new KHGMenuLoader(), 2).execute();
        new MenuLoadTask(new RaabMenuLoader(), 3).execute();
    }

    public void setDate(Date mDate) {
        this.mDate = mDate;
    }

    private synchronized void setMensa(Mensa mensa, int index) {
        while (index >= mMensen.size()) {
            mMensen.add(null);
        }

        mMensen.set(index, mensa);
    }

    private class MenuLoadTask extends AsyncTask<String, Void, Void> {
        private Context mContext;
        private MenuLoader mLoader;
        private int mIndex;
        private Mensa mMensa;

        public MenuLoadTask(MenuLoader loader, int index) {
            super();

            this.mLoader = loader;
            this.mIndex = index;
        }

        @Override
        protected Void doInBackground(String... urls) {
            mMensa = mLoader.getMensa(mContext);

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mMensa = null;

            mContext = MensaDayFragment.this.getContext();
            if (mContext == null) {
                Log.e(TAG, "context is null");
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            setMensa(mMensa, mIndex);

            List<MensaItem> menus = new ArrayList<>();
            int noMenuCount = 0;

            for (Mensa mensa : mMensen) {
                if (mensa != null) {
                    MensaDay day = mensa.getDay(mDate);
                    if (day != null && !day.isEmpty()) {
                        for (MensaMenu menu : day.getMenus()) {
                            menus.add(menu);
                        }
                    } else {
                        // add no menu card
                        menus.add(new MensaInfoItem(mensa, day, getString(R.string.mensa_menu_not_available), null));
                        noMenuCount++;
                    }
                }
            }

            // add default no menu card
            if (menus.size() == 0 || menus.size() == noMenuCount) {
                menus.clear();
                menus.add(new MensaInfoItem(null, null, getString(R.string.mensa_menu_not_available), null));
            }

            if (mAdapter != null) {
                mAdapter.clear();
                mAdapter.addAll(menus);
                mAdapter.notifyDataSetChanged();
            }

            super.onPostExecute(result);
        }
    }
}
