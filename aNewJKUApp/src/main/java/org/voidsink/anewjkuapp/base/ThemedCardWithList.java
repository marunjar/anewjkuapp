package org.voidsink.anewjkuapp.base;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;

import it.gmariotti.cardslib.library.prototypes.CardWithList;

public abstract class ThemedCardWithList extends CardWithList {

    public ThemedCardWithList(Context context) {
        super(context);

        applyTheme(context);
    }

    public ThemedCardWithList(Context context, int innerLayout) {
        super(context, innerLayout);

        applyTheme(context);
    }

    protected void applyTheme(Context context) {
        // default theme for cards is holo light, so change only if holo is used
        if (!PreferenceWrapper.getUseLightDesign(context)) {
            setBackgroundResourceId(R.drawable.card_background_holo);
        }
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        super.setupInnerViewElements(parent, view);

        if (mListView != null) {
            // set background transparent, so the color of the card would be "used" as background
            mListView.setBackgroundColor(Color.argb(0, 0, 0, 0));
            // try to resolve background color and set it as divider color
            TypedValue styleID = new TypedValue();
            if (getContext().getTheme().resolveAttribute(android.R.attr.colorBackground, styleID, true)) {
                final int dividerHeight = mListView.getDividerHeight();
                mListView.setDividerDrawable(new ColorDrawable(styleID.data));
                // restore height after setting divider color
                mListView.setDividerHeight(dividerHeight);
            }
        }
    }
}
