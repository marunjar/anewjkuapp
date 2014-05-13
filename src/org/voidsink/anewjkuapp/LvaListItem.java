package org.voidsink.anewjkuapp;

public interface LvaListItem {

	public static final int TYPE_LVA = 0;
	public static final int TYPE_TERM = 1;

	public boolean isLva();
	public int getType();

}
