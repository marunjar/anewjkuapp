package org.voidsink.anewjkuapp.calendar;

import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;


public class CalendarListEvent implements CalendarListItem {

	private int mColor;
	private CharSequence mTitle;
	private CharSequence mTime;
	private CharSequence mLocation;
	private long mDtStart;
	private long mDtEnd;

	public CalendarListEvent(int color, CharSequence title,
			CharSequence location, long dtStart, long dtEnd) {
		this.mColor = color;
		this.mTitle = title;
		this.mLocation = location;
		this.mDtStart = dtStart;
		this.mDtEnd = dtEnd;

		Date mDtStart = new Date(dtStart);
		Date mDtEnd = new Date(dtEnd);

		DateFormat dfStart = DateFormat.getTimeInstance();
		DateFormat dfEnd = DateFormat.getTimeInstance();
		if (!DateUtils.isSameDay(mDtStart, mDtEnd)) {
			dfEnd = DateFormat.getDateTimeInstance();
		}

		this.mTime = String.format("%s - %s", dfStart.format(mDtStart),
				dfEnd.format(mDtEnd));
	}

	@Override
	public boolean isEvent() {
		return true;
	}

	@Override
	public int getType() {
		return EVENT_TYPE;
	}

	public CharSequence getLocation() {
		return mLocation;
	}

	public CharSequence getTime() {
		return mTime;
	}

	public CharSequence getTitle() {
		return mTitle;
	}

	public int getColor() {
		return mColor;
	}

	public long getDtStart() {
		return mDtStart;
	}

	public long getDtEnd() {
		return mDtEnd;
	}
	
	
}
