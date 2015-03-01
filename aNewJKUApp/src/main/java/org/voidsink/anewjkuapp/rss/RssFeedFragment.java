package org.voidsink.anewjkuapp.rss;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.rss.lib.FeedEntry;
import org.voidsink.anewjkuapp.rss.lib.FeedPullParser;
import org.voidsink.anewjkuapp.utils.Consts;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RssFeedFragment extends BaseFragment {

    private URL mUrl = null;
    private RssListAdapter mAdapter;
    private DisplayImageOptions mOptions;
    private RecyclerView mRecyclerView;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .displayer(new SimpleBitmapDisplayer())
                .showImageOnLoading(getResources().getDrawable(R.mipmap.ic_launcher))
                .showImageForEmptyUri(getResources().getDrawable(R.mipmap.ic_launcher))
                .showImageOnFail(getResources().getDrawable(R.mipmap.ic_launcher))
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true); // performance

        mAdapter = new RssListAdapter(getContext(), new ArrayList<FeedEntry>(), mOptions);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

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

    private class LoadFeedTask extends AsyncTask<Void, Void, Void> {

        private List<FeedEntry> mFeed = null;
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
            mFeed = new FeedPullParser().parse(mUrl);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mFeed != null) {
                mRecyclerView.setAdapter(new RssListAdapter(getContext(), mFeed, mOptions));
            } else {
                mRecyclerView.setAdapter(new RssListAdapter(getContext(), new ArrayList<FeedEntry>(), mOptions));
                Toast.makeText(getContext(), "TODO: Error loading feed.", Toast.LENGTH_SHORT).show();
            }

            mProgressDialog.dismiss();

            super.onPostExecute(aVoid);
        }
    }
}
