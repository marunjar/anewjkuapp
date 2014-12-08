package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.mensa.Mensa;
import org.voidsink.anewjkuapp.mensa.MensaDay;

public interface MensaItem {

	public static final int TYPE_MENU = 0;
	public static final int TYPE_INFO = 1;

	public int getType();
    public MensaDay getDay();
    public Mensa getMensa();

}
