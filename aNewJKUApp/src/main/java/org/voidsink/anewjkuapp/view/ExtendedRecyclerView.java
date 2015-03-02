package org.voidsink.anewjkuapp.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

public class ExtendedRecyclerView extends RecyclerView {

    protected static String TAG = ExtendedRecyclerView.class.getSimpleName();
    private int mFixedColumnWidth = -1;
    private int mFixedNumColumns = -1;


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
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        if (mFixedNumColumns > 0) {
            if (getLayoutManager() instanceof GridLayoutManager) {
                ((GridLayoutManager) getLayoutManager()).setSpanCount(mFixedNumColumns);
            } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
                ((StaggeredGridLayoutManager) getLayoutManager()).setSpanCount(mFixedNumColumns);
            }
        } else if (mFixedColumnWidth > 0) {
            int spanCount = Math.max(1, mFixedNumColumns);

            if (getLayoutManager() instanceof GridLayoutManager) {
                GridLayoutManager mGridLM = (GridLayoutManager) getLayoutManager();

                switch (mGridLM.getOrientation()) {
                    case GridLayoutManager.VERTICAL: {
                        spanCount = Math.max(1, getMeasuredWidth() / mFixedColumnWidth);
                        break;
                    }
                    case GridLayoutManager.HORIZONTAL: {
                        spanCount = Math.max(1, getMeasuredHeight() / mFixedColumnWidth);
                        break;
                    }
                }
                mGridLM.setSpanCount(spanCount);
            } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager mStaggeredGridLM = (StaggeredGridLayoutManager) getLayoutManager();

                switch (mStaggeredGridLM.getOrientation()) {
                    case StaggeredGridLayoutManager.VERTICAL: {
                        spanCount = Math.max(1, getMeasuredWidth() / mFixedColumnWidth);
                        break;
                    }
                    case StaggeredGridLayoutManager.HORIZONTAL: {
                        spanCount = Math.max(1, getMeasuredHeight() / mFixedColumnWidth);
                        break;
                    }
                }
                mStaggeredGridLM.setSpanCount(spanCount);
            }
        }
    }
}
