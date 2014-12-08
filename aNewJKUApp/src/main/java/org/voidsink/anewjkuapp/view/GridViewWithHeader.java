package org.voidsink.anewjkuapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;

import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import org.voidsink.anewjkuapp.base.BaseArrayAdapter;
import org.voidsink.anewjkuapp.base.GridWithHeaderAdapter;
import org.voidsink.anewjkuapp.base.ListWithHeaderAdapter;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class GridViewWithHeader extends StickyGridHeadersGridView {

    protected static String TAG = GridViewWithHeader.class.getSimpleName();

    /**
     * Stycky Card Array Adapter
     */
    protected BaseArrayAdapter mAdapter;

    //--------------------------------------------------------------------------
    // Fields for expand/collapse animation
    //--------------------------------------------------------------------------
    private boolean mShouldRemoveObserver = false;
    private List<View> mViewsToDraw = new ArrayList<View>();

    //--------------------------------------------------------------------------
    // Custom Attrs
    //--------------------------------------------------------------------------
    private int[] mTranslate;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    public GridViewWithHeader(Context context) {
        super(context);
        init(null, 0);
    }

    public GridViewWithHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GridViewWithHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    //--------------------------------------------------------------------------
    // Init
    //--------------------------------------------------------------------------

    /**
     * Initialize
     *
     * @param attrs
     * @param defStyle
     */
    protected void init(AttributeSet attrs, int defStyle) {
        //Init attrs
        initAttrs(attrs, defStyle);
        //Set divider to 0dp
        //setDividerHeight(0);
        // disable drawing list under list header
        //setDrawingListUnderStickyHeader(false);
    }

    /**
     * Init custom attrs.
     *
     * @param attrs
     * @param defStyle
     */
    protected void initAttrs(AttributeSet attrs, int defStyle) {

    }

    //--------------------------------------------------------------------------
    // Adapter
    //--------------------------------------------------------------------------


    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter instanceof GridWithHeaderAdapter) {
            setAdapter((GridWithHeaderAdapter) adapter);
        } else {
            Log.e(TAG, "You are using a generic adapter. Pay attention: your adapter has to call cardArrayAdapter#getView method");
            super.setAdapter(adapter);
        }
    }

    public void setAdapter(GridWithHeaderAdapter adapter) {
        super.setAdapter(adapter);

        //Set Layout used by items
        adapter.setGridView(this);
        mAdapter = adapter;
    }
}
