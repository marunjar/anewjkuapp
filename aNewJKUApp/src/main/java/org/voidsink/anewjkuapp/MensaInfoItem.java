package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.mensa.Mensa;
import org.voidsink.anewjkuapp.mensa.MensaDay;

/**
 * Created by paul on 07.12.2014.
 */
public class MensaInfoItem implements MensaItem {
    protected final MensaDay mDay;
    protected String title;
    protected String descr;
    protected Mensa mMensa;

    public MensaInfoItem(Mensa mensa, MensaDay day, String title, String descr) {
        this.mDay = day;
        this.descr = descr;
        this.mMensa = mensa;
        this.title = title;
    }

    @Override
    public int getType() {
        return MensaItem.TYPE_INFO;
    }

    @Override
    public MensaDay getDay() {
        return mDay;
    }

    @Override
    public Mensa getMensa() {
        return mMensa;
    }

    public String getTitle() {
        return title;
    }

    public String getDescr() {
        return descr;
    }
}
