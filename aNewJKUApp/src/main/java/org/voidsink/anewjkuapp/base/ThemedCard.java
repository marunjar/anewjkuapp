package org.voidsink.anewjkuapp.base;

import android.content.Context;

import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;

import it.gmariotti.cardslib.library.internal.Card;

public class ThemedCard extends Card {

    public ThemedCard(Context context) {
        super(context);

        applyTheme(context);
    }

    public ThemedCard(Context context, int innerLayout) {
        super(context, innerLayout);

        applyTheme(context);
    }

    protected void applyTheme(Context context) {
        // default theme for cards is holo light, so change only if holo is used
        if (!PreferenceWrapper.getUseLightDesign(context)) {
            setBackgroundResourceId(R.drawable.card_background_holo);
        }
    }

}
