package org.voidsink.anewjkuapp.rss;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.pkmmte.pkrss.Article;
import com.pkmmte.pkrss.Callback;
import com.pkmmte.pkrss.PkRSS;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.utils.Consts;
import android.widget.Toast;

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
public class RssFeedFragment extends BaseFragment implements Callback {

    private URL mUrl = null;
    private CardArrayAdapter mCardArrayAdapter;
    private List<Article> mFeed = new ArrayList<>();

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
        loadFeed();
    }

    private void loadFeed() {
        if (mUrl != null) {
            PkRSS.with(getContext()).load(mUrl.toString()).callback(RssFeedFragment.this).async();
        }
    }

    @Override
    public void OnPreLoad() {

    }

    @Override
    public void OnLoaded(List<Article> articles) {
        if (mUrl != null) {
            mFeed = PkRSS.with(getContext()).get(mUrl.toString());
        }
    }

    private void updateData() {
        if (mCardArrayAdapter != null) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .displayer(new SimpleBitmapDisplayer())
                    .showImageForEmptyUri(getResources().getDrawable(R.drawable.ic_launcher_grey))
                    .showImageOnFail(getResources().getDrawable(R.drawable.ic_launcher_grey))
                    .build();

            List<Card> cards = new ArrayList<>();

            for (Article article : mFeed) {
                Card card = new RssCard(getContext(), article, options);
                cards.add(card);
            }

            mCardArrayAdapter.clear();
            mCardArrayAdapter.addAll(cards);
            mCardArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void OnLoadFailed() {
        Toast.makeText(getActivity(), "TODO: Error loading feed.", Toast.LENGTH_SHORT).show();
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
}
