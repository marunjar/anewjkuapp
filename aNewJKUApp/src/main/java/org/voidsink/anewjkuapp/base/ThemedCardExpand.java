package org.voidsink.anewjkuapp.base;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;

import it.gmariotti.cardslib.library.internal.CardExpand;

/**
 * Created by paul on 29.10.2014.
 */
public class ThemedCardExpand extends CardExpand {

    public ThemedCardExpand(Context context) {
        super(context);
    }

    public ThemedCardExpand(Context context, int innerLayout) {
        super(context, innerLayout);
    }


    @Override
    public View getInnerView(Context context, ViewGroup parent) {
        if (PreferenceWrapper.getUseLightDesign(context)) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                parent.setBackground(context.getResources().getDrawable(R.drawable.card_background));
            } else {
                parent.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.card_background));
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                parent.setBackground(context.getResources().getDrawable(R.drawable.card_background_holo));
            } else {
                parent.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.card_background_holo));
            }
        }

        return super.getInnerView(context, parent);
    }

}
