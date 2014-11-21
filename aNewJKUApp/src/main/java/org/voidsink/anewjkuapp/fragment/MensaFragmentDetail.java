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

import org.voidsink.anewjkuapp.MenuCard;
import org.voidsink.anewjkuapp.MenuCardArrayAdapter;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.mensa.Mensa;
import org.voidsink.anewjkuapp.mensa.MensaDay;
import org.voidsink.anewjkuapp.mensa.MensaMenu;
import org.voidsink.anewjkuapp.mensa.MenuLoader;
import org.voidsink.anewjkuapp.view.StickyCardListView;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;

public abstract class MensaFragmentDetail extends BaseFragment {

    public static final String TAG = MensaFragmentDetail.class.getSimpleName();
    private MenuCardArrayAdapter mAdapter;
    private StickyCardListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_menu, container,
                false);

        mListView = (StickyCardListView) view.findViewById(R.id.menu_card_list);
        mAdapter = new MenuCardArrayAdapter(getContext(), new ArrayList<Card>(), true);
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
        private List<Card> mMenus;
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
                        mMenus.add(new MenuCard(mContext, mensa, day, menu));
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
            mAdapter.clear();
            mAdapter.addAll(mMenus);
            mAdapter.notifyDataSetChanged();

            // scroll to today's menu
            if (mSelectPosition >= 0 &&
                    mListView != null) {
                mListView.smoothScrollToPosition(mSelectPosition);
            }

            super.onPostExecute(result);
        }
    }
}
