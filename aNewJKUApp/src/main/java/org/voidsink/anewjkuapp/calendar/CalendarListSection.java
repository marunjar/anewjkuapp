package org.voidsink.anewjkuapp.calendar;

import java.text.DateFormat;
import java.util.Date;

public class CalendarListSection implements CalendarListItem {

	private CharSequence mText;

	public CalendarListSection(long date) {
		this.mText = DateFormat.getDateInstance().format(new Date(date));
	}

	@Override
	public boolean isEvent() {
		return false;
	}

	@Override
	public int getType() {
		return TYPE_SECTION;
	}

	public CharSequence getText() {
		return mText;
	}

}
