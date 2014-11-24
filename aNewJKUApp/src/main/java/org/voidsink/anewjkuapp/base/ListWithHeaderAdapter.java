package org.voidsink.anewjkuapp.base;

import android.content.Context;

import org.voidsink.anewjkuapp.view.ListViewWithHeader;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public abstract class ListWithHeaderAdapter<T> extends BaseArrayAdapter<T> implements StickyListHeadersAdapter {

    protected ListViewWithHeader mListView;

    public ListWithHeaderAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public void setListView(ListViewWithHeader cardListView) {
        this.mListView = cardListView;
    }
}