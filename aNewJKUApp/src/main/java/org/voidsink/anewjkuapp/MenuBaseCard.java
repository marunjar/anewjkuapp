package org.voidsink.anewjkuapp;

import android.content.Context;

import org.voidsink.anewjkuapp.base.ThemedCard;
import org.voidsink.anewjkuapp.mensa.Mensa;
import org.voidsink.anewjkuapp.mensa.MensaDay;
import org.voidsink.anewjkuapp.mensa.MensaMenu;

/**
 * Created by paul on 18.09.2014.
 */
public abstract class MenuBaseCard extends ThemedCard {
    protected Mensa mMensa;
    protected MensaDay mDay = null;
    protected MensaMenu mMenu = null;

    public MenuBaseCard(final Context c, Mensa mensa, MensaDay day, MensaMenu menu) {
        super(c);

        int layoutResId = getInnerLayoutResId();
        if (layoutResId > 0) {
            setInnerLayout(layoutResId);
        }

        this.mMensa = mensa;
        this.mDay = day;
        this.mMenu = menu;
    }

    protected int getInnerLayoutResId() {
        return 0;
    }

    public Mensa getMensa() {
        return mMensa;
    }

    public MensaDay getDay() {
        return mDay;
    }

    public MensaMenu getMenu() {
        return mMenu;
    }
}
