package org.voidsink.anewjkuapp.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import org.voidsink.anewjkuapp.view.MenuCardListView;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;

public abstract class MensaFragmentDetail extends BaseFragment {

	public static final String TAG = MensaFragmentDetail.class.getSimpleName();
    private MenuCardListView mListView;
    private MenuCardArrayAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_card_menu, container,
				false);

		mListView = (MenuCardListView) view.findViewById(R.id.menu_card_list);
		mAdapter = new MenuCardArrayAdapter(getContext(), new ArrayList<Card>(), true);
		mListView.setAdapter(mAdapter);

		return view;
	}

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new MenuLoadTask().execute();
    }

    private class MenuLoadTask extends AsyncTask<String, Void, Void> {
		private Mensa mensa;
        private List<Card> mMenus;
		private Context mContext;

		@Override
		protected Void doInBackground(String... urls) {
			mensa = createLoader().getMensa(mContext);

            mMenus = new ArrayList<Card>();

            for (MensaDay day : mensa.getDays()) {
                for (MensaMenu menu : day.getMenus()) {
                    mMenus.add(new MenuCard(mContext, mensa, day, menu));
                }
            }

			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mContext = MensaFragmentDetail.this.getContext();
			if (mContext == null) {
				Log.e(TAG, "context is null");
			}
			mAdapter.clear();
			mAdapter.notifyDataSetChanged();
        }

		@Override
		protected void onPostExecute(Void result) {
			mAdapter.clear();
			mAdapter.addAll(mMenus);
			mAdapter.notifyDataSetChanged();

			super.onPostExecute(result);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	protected abstract MenuLoader createLoader();

}
