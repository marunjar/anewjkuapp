package org.voidsink.anewjkuapp.calendar;

public interface CalendarListItem {

	public static final int TYPE_SECTION = 0;
	public static final int TYPE_EVENT = 1;
	
	public boolean isEvent();
	public int getType();
}