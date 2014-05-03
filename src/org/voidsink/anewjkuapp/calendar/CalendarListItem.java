package org.voidsink.anewjkuapp.calendar;

public interface CalendarListItem {

	public static final int SECTION_TYPE = 0;
	public static final int EVENT_TYPE = 1;
	
	public boolean isEvent();
	public int getType();
}
