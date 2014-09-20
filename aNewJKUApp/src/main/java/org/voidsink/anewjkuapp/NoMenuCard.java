package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.ContextThemeWrapper;

import org.voidsink.anewjkuapp.mensa.Mensa;
import org.voidsink.anewjkuapp.mensa.MensaDay;
import org.voidsink.anewjkuapp.mensa.MensaMenu;

import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by paul on 06.09.2014.
 */
public class NoMenuCard extends MenuBaseCard {

    public NoMenuCard(final Context c, Mensa mensa, MensaDay day, MensaMenu menu) {
        super(c, mensa, day, menu);

        this.mMensa = mensa;
        this.mDay = day;
        this.mMenu = menu;

        // init header
        CardHeader header = new CardHeader(new ContextThemeWrapper(c, R.style.AppTheme));

        header.setTitle(c.getString(R.string.menu_not_available));
        addCardHeader(header);
    }
}
