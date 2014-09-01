package org.voidsink.anewjkuapp;

public interface MensaItem {

	public static final int TYPE_MENSA = 0;
	public static final int TYPE_DAY = 1;
	public static final int TYPE_MENU = 2;
	public static final int TYPE_INFO = 3;

	public int getType();

}
