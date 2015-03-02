package org.voidsink.anewjkuapp.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
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
import org.voidsink.anewjkuapp.mensa.Mensa;
import org.voidsink.anewjkuapp.mensa.MensaDay;
import org.voidsink.anewjkuapp.mensa.MensaMenu;
import org.voidsink.anewjkuapp.mensa.MenuLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public abstract class MensaFragmentDetail extends BaseFragment {

    public static final String TAG = MensaFragmentDetail.class.getSimpleName();
    private MensaMenuAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new MensaMenuAdapter(getContext(), true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(mAdapter));

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

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            // set date to start of week
            cal.add(Calendar.DAY_OF_YEAR, -cal.get(Calendar.DAY_OF_WEEK) + cal.getFirstDayOfWeek());

            if (mensa != null) {
                for (MensaDay day : mensa.getDays()) {
                    // allow only menus >= start of this week
                    if ((day.getDate() != null) && (day.getDate().getTime() >= cal.getTimeInMillis())) {
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
            }
            if (mMenus.size() == 0) {
                mMenus.add(new MensaInfoItem(mensa, null, getString(R.string.mensa_menu_not_available), null));
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
                        mRecyclerView != null) {
                    mRecyclerView.smoothScrollToPosition(mSelectPosition);
                }
            }
            super.onPostExecute(result);
        }
    }
}
