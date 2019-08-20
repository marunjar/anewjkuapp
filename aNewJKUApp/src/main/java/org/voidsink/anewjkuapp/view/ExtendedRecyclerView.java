/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2019 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class ExtendedRecyclerView extends RecyclerView {

    //protected static String TAG = ExtendedRecyclerView.class.getSimpleName();
    private int mFixedColumnWidth = -1;
    private int mFixedNumColumns = -1;

    private int mOrigSpanCount = -1;

    public ExtendedRecyclerView(Context context) {
        super(context);
    }

    public ExtendedRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public ExtendedRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (context != null && attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.numColumns});
            mFixedNumColumns = array.getInteger(0, -1);
            array.recycle();

            array = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.columnWidth});
            mFixedColumnWidth = array.getDimensionPixelSize(0, -1);
            array.recycle();
        }
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);

        if (layout instanceof GridLayoutManager) {
            mOrigSpanCount = ((GridLayoutManager) layout).getSpanCount();
        } else if (layout instanceof StaggeredGridLayoutManager) {
            mOrigSpanCount = ((StaggeredGridLayoutManager) layout).getSpanCount();
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        if (mFixedNumColumns > 0) {
            if (getLayoutManager() instanceof GridLayoutManager) {
                ((GridLayoutManager) getLayoutManager()).setSpanCount(checkSpanCount(mFixedNumColumns));
            } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
                ((StaggeredGridLayoutManager) getLayoutManager()).setSpanCount(checkSpanCount(mFixedNumColumns));
            }
        } else if (mFixedColumnWidth > 0) {
            int spanCount = mOrigSpanCount;

            if (getLayoutManager() instanceof GridLayoutManager) {
                GridLayoutManager mGridLM = (GridLayoutManager) getLayoutManager();

                switch (mGridLM.getOrientation()) {
                    case RecyclerView.VERTICAL: {
                        spanCount = getMeasuredWidth() / mFixedColumnWidth;
                        break;
                    }
                    case RecyclerView.HORIZONTAL: {
                        spanCount = getMeasuredHeight() / mFixedColumnWidth;
                        break;
                    }
                }
                mGridLM.setSpanCount(checkSpanCount(spanCount));
            } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager mStaggeredGridLM = (StaggeredGridLayoutManager) getLayoutManager();

                switch (mStaggeredGridLM.getOrientation()) {
                    case StaggeredGridLayoutManager.VERTICAL: {
                        spanCount = getMeasuredWidth() / mFixedColumnWidth;
                        break;
                    }
                    case StaggeredGridLayoutManager.HORIZONTAL: {
                        spanCount = getMeasuredHeight() / mFixedColumnWidth;
                        break;
                    }
                }
                mStaggeredGridLM.setSpanCount(checkSpanCount(spanCount));
            }
        }
    }

    private int checkSpanCount(int spanCount) {
        if (spanCount < 1) return 1;
        //if (spanCount > mOrigSpanCount) return mOrigSpanCount;
        return spanCount;
    }
}
