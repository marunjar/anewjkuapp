package org.voidsink.anewjkuapp.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.MensaItem;
import org.voidsink.anewjkuapp.MensaMenuAdapter;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.mensa.Mensa;
import org.voidsink.anewjkuapp.mensa.MensaDay;
import org.voidsink.anewjkuapp.mensa.MensaMenu;
import org.voidsink.anewjkuapp.mensa.MenuLoader;
import org.voidsink.anewjkuapp.view.GridViewWithHeader;
import org.voidsink.anewjkuapp.view.ListViewWithHeader;

import java.util.ArrayList;
import java.util.List;

public abstract class MensaFragmentDetail extends BaseFragment {

    public static final String TAG = MensaFragmentDetail.class.getSimpleName();
    private MensaMenuAdapter mAdapter;
    private GridViewWithHeader mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grid_with_header, container,
                false);

        mListView = (GridViewWithHeader) view.findViewById(R.id.gridview);
        mAdapter = new MensaMenuAdapter(getContext(), android.R.layout.simple_list_item_1, true);
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new MenuLoadTask().execute();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    protected abstract MenuLoader createLoader();

    private class MenuLoadTask extends AsyncTask<String, Void, Void> {
        private List<MensaItem> mMenus;
        private Context mContext;
        private int mSelectPosition;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mContext = MensaFragmentDetail.this.getContext();
            if (mContext == null) {
                Log.e(TAG, "context is null");
            }
            mMenus = new ArrayList<>();
            mSelectPosition = -1;
        }

        @Override
        protected Void doInBackground(String... urls) {
            final Mensa mensa = createLoader().getMensa(mContext);

            if (mensa != null) {
                for (MensaDay day : mensa.getDays()) {
                    for (MensaMenu menu : day.getMenus()) {
                        mMenus.add(menu);
                        // remember position of menu for today for scrolling to item after update
                        if (mSelectPosition == -1 &&
                                DateUtils.isToday(day.getDate().getTime())) {
                            mSelectPosition = mMenus.size() - 1;
                        }
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mAdapter != null) {
                mAdapter.clear();
                mAdapter.addAll(mMenus);
                mAdapter.notifyDataSetChanged();

                // scroll to today's menu
                if (mSelectPosition >= 0 &&
                        mListView != null) {
                    mListView.smoothScrollToPosition(mSelectPosition);
                }
            }
            super.onPostExecute(result);
        }
    }
}
