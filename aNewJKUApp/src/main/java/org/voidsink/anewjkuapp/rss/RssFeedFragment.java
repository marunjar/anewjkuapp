package org.voidsink.anewjkuapp.rss;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.pkmmte.pkrss.Article;
import com.pkmmte.pkrss.Callback;
import com.pkmmte.pkrss.PkRSS;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.utils.Consts;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Created by paul on 16.11.2014.
 */
public class RssFeedFragment extends BaseFragment {

    private URL mUrl = null;
    private CardArrayAdapter mCardArrayAdapter;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        try {
            mUrl = new URL(args.getString(Consts.ARG_FEED_URL));
        } catch (MalformedURLException e) {
            mUrl = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_rss_feed, container, false);

        final CardListView mCardListView = (CardListView) v.findViewById(R.id.rssfeed_list);
        final List<Card> cards = new ArrayList<>();

        mCardArrayAdapter = new CardArrayAdapter(getContext(), cards);
        mCardListView.setAdapter(mCardArrayAdapter);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateData();
    }

    private void updateData() {
        new LoadFeedTask().execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.rss_feed, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_feed:
                updateData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class LoadFeedTask implements Callback {

        private Boolean mDone;
        private List<Article> mFeed = null;

        private void execute() {
            mDone = false;

            if (mUrl != null) {
                List<Article> mArticles = PkRSS.with(getContext()).get(mUrl.toString());
                if (mArticles != null) mArticles.clear();

                PkRSS.with(getContext()).load(mUrl.toString()).callback(this).async();
            } else {
                mDone = true;
            }

            new AsyncTask<Void, Void, Void>() {

                private ProgressDialog mProgressDialog;

                @Override
                protected void onPreExecute() {
                    mProgressDialog = ProgressDialog.show(getContext(),
                            getContext().getString(R.string.progress_title),
                            getContext().getString(R.string.progress_load_feed), true);

                    super.onPreExecute();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    while (!mDone) {
                        Log.d(getClass().getSimpleName(), "wait...");
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (mCardArrayAdapter != null) {
                        DisplayImageOptions options = new DisplayImageOptions.Builder()
                                .cacheInMemory(true)
                                .displayer(new SimpleBitmapDisplayer())
                                .showImageForEmptyUri(getResources().getDrawable(R.drawable.ic_launcher))
                                .showImageOnFail(getResources().getDrawable(R.drawable.ic_launcher))
                                .build();

                        List<Card> cards = new ArrayList<>();

                        if (mFeed != null) {
                            for (Article article : mFeed) {
                                Card card = new RssCard(getContext(), article, options);
                                cards.add(card);
                            }
                        }

                        mCardArrayAdapter.clear();
                        mCardArrayAdapter.addAll(cards);
                        mCardArrayAdapter.notifyDataSetChanged();
                    }

                    mProgressDialog.dismiss();

                    super.onPostExecute(aVoid);
                }
            }.execute();
        }


        @Override
        public void OnPreLoad() {
            Log.d(LoadFeedTask.class.getSimpleName(), "OnPreLoad...");
        }

        @Override
        public void OnLoaded(List<Article> articles) {
            Log.d(LoadFeedTask.class.getSimpleName(), "OnLoaded...");

            if (mUrl != null) {
                mFeed = PkRSS.with(getContext()).get(mUrl.toString());
            }

            mDone = true;
        }

        @Override
        public void OnLoadFailed() {
            mDone = true;
            Log.d(LoadFeedTask.class.getSimpleName(), "OnLoadFailed...");

            Toast.makeText(getContext(), "TODO: Error loading feed.", Toast.LENGTH_SHORT).show();
        }
    }


}
