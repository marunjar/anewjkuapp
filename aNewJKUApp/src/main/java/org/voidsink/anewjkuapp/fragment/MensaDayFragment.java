package org.voidsink.anewjkuapp.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import org.voidsink.anewjkuapp.view.GridViewWithHeader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by paul on 18.09.2014.
 */
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
        View view = inflater.inflate(R.layout.fragment_grid_with_header, container,
                false);

        final GridViewWithHeader mGridView = (GridViewWithHeader) view.findViewById(R.id.gridview);
        mAdapter = new MensaMenuAdapter(getContext(), android.R.layout.simple_list_item_1, false);
        mGridView.setAdapter(mAdapter);

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
