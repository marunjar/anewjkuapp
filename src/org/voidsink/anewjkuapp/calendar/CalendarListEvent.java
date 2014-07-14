package org.voidsink.anewjkuapp.calendar;

import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;


public class CalendarListEvent implements CalendarListItem {

	private int mColor;
	private String mTitle;
	private String mDescr;
	private String mTime;
	private String mLocation;
	private long mDtStart;
	private long mDtEnd;
	
	public CalendarListEvent(int color, String title, String descr,
			String location, long dtStart, long dtEnd) {
		this.mColor = color;
		this.mTitle = title;
		this.mDescr = descr;
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
		return TYPE_EVENT;
	}

	public String getLocation() {
		return mLocation;
	}

	public String getTime() {
		return mTime;
	}

	public String getTitle() {
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

	public String getDescr() {
		return mDescr;
	}
	
	
}
