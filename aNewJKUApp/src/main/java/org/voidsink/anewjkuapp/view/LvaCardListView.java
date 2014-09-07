package org.voidsink.anewjkuapp.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.voidsink.anewjkuapp.LvaCardArrayAdapter;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.calendar.CalendarCardArrayAdapter;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by paul on 06.09.2014.
 */
public class LvaCardListView extends StickyListHeadersListView {

    protected static String TAG = "CardListView";

    /**
     * Stycky Card Array Adapter
     */
    protected CardArrayAdapter mAdapter;

    //--------------------------------------------------------------------------
    // Fields for expand/collapse animation
    //--------------------------------------------------------------------------

    private boolean mShouldRemoveObserver = false;

    private List<View> mViewsToDraw = new ArrayList<View>();

    private int[] mTranslate;
    private List<LvaWithGrade> lvas;

    //--------------------------------------------------------------------------
    // Custom Attrs
    //--------------------------------------------------------------------------

    /**
     * Default layout to apply to card
     */
//    protected int list_card_layout_resourceID = R.layout.calendar_card_layout;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public LvaCardListView(Context context) {
        super(context);
        init(null, 0);
    }

    public LvaCardListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LvaCardListView(Context context, AttributeSet attrs, int defStyle) {
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
        setDividerHeight(0);

    }


    /**
     * Init custom attrs.
     *
     * @param attrs
     * @param defStyle
     */
    protected void initAttrs(AttributeSet attrs, int defStyle) {

//        list_card_layout_resourceID = R.layout.calendar_card_layout;

        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.card_options, defStyle, defStyle);

        try {
//            list_card_layout_resourceID = a.getResourceId(R.styleable.card_options_list_card_layout_resourceID, this.list_card_layout_resourceID);
        } finally {
            a.recycle();
        }
    }

    //--------------------------------------------------------------------------
    // Adapter
    //--------------------------------------------------------------------------

    /**
     * Set the adapter. You can provide a {@link it.gmariotti.cardslib.library.internal.CardArrayAdapter}, or a {@link it.gmariotti.cardslib.library.internal.CardCursorAdapter}
     * or a generic adapter.
     * Pay attention: your generic adapter has to call {@link it.gmariotti.cardslib.library.internal.CardArrayAdapter#getView} method
     *
     * @param adapter
     */
    @Override
    public void setAdapter(StickyListHeadersAdapter adapter) {
        if (adapter instanceof LvaCardArrayAdapter) {
            setAdapter((LvaCardArrayAdapter) adapter);
        } else {
            Log.e(TAG, "You are using a generic adapter. Pay attention: your adapter has to call cardArrayAdapter#getView method");
            super.setAdapter(adapter);
        }
    }

    /**
     * Set {@link it.gmariotti.cardslib.library.internal.CardArrayAdapter} and layout used by items in ListView
     *
     * @param adapter {@link it.gmariotti.cardslib.library.internal.CardArrayAdapter}
     */
    public void setAdapter(LvaCardArrayAdapter adapter) {
        super.setAdapter(adapter);

        //Set Layout used by items
//        adapter.setRowLayoutId(R.layout.card_layout);

        adapter.setLvaListView(this);
        mAdapter = adapter;
    }
}
