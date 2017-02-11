/*
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.voidsink.anewjkuapp.calendar;

import android.content.Context;

import org.voidsink.anewjkuapp.utils.AppUtils;

import java.util.Date;


public class CalendarListEvent implements CalendarListItem {

    private final long mEventId;
    private final int mColor;
    private final String mTitle;
    private final String mDescr;
    private final String mTime;
    private final String mLocation;
    private final long mDtStart;
    private final long mDtEnd;
    private final boolean mAllDay;

    public CalendarListEvent(Context c, long eventId, int color, String title, String descr,
                             String location, long dtStart, long dtEnd, boolean allDay) {
        this.mEventId = eventId;
        this.mColor = color;
        this.mTitle = title;
        this.mDescr = descr;
        this.mLocation = location;
        this.mDtStart = dtStart;
        this.mDtEnd = dtEnd;
        this.mAllDay = allDay;

        Date mDtStart = new Date(dtStart);
        Date mDtEnd = new Date(dtEnd);

        this.mTime = AppUtils.getTimeString(c, mDtStart, mDtEnd, mAllDay);
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

    public boolean isAllDay() {
        return mAllDay;
    }

    public String getDescr() {
        return mDescr;
    }

    public long getEventId() {
        return mEventId;
    }

    public void showOnMap(Context context) {
        AppUtils.showEventLocation(context, getLocation());
    }

    public void showInCalendar(Context context) {
        AppUtils.showEventInCalendar(context, getEventId(), getDtStart());
    }
}
