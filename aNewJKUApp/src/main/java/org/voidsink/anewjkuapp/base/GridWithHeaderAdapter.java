package org.voidsink.anewjkuapp.base;

import android.content.Context;

import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;

import org.voidsink.anewjkuapp.view.GridViewWithHeader;

public abstract class GridWithHeaderAdapter<T> extends BaseArrayAdapter<T> implements StickyGridHeadersSimpleAdapter {

    protected GridViewWithHeader mGridView;

    public GridWithHeaderAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public void setGridView(GridViewWithHeader gridView) {
        this.mGridView = gridView;
    }
}