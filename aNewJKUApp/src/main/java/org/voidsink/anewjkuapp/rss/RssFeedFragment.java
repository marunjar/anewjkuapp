package org.voidsink.anewjkuapp.rss;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
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
import java.util.List;

public class RssFeedFragment extends BaseFragment {

    private URL mUrl = null;
    private RssListAdapter mAdapter;
    private DisplayImageOptions mOptions;

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
        View v = inflater.inflate(R.layout.fragment_grid, container, false);

        final GridView mGridView = (GridView) v.findViewById(R.id.gridview);

        mAdapter = new RssListAdapter(getContext(), mOptions);
        mGridView.setAdapter(mAdapter);

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
            if (mAdapter != null) {
                mAdapter.clear();
                if (mFeed != null) {
                    mAdapter.addAll(mFeed);
                } else {
                    Toast.makeText(getContext(), "TODO: Error loading feed.", Toast.LENGTH_SHORT).show();
                }
                mAdapter.notifyDataSetChanged();
            }

            mProgressDialog.dismiss();

            super.onPostExecute(aVoid);
        }
    }
}
