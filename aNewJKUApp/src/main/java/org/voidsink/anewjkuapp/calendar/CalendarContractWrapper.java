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
 *
 */

package org.voidsink.anewjkuapp.calendar;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;

public final class CalendarContractWrapper {

    private CalendarContractWrapper() {

    }

    public static String AUTHORITY() {
        return CalendarContract.AUTHORITY;
    }

    public static String CALLER_IS_SYNCADAPTER() {
        return CalendarContract.CALLER_IS_SYNCADAPTER;
    }

    public static Uri CONTENT_URI() {
        return CalendarContract.CONTENT_URI;
    }

    public static class Calendars {

        private Calendars() {

        }

        public static String OWNER_ACCOUNT() {
            return CalendarContract.Calendars.OWNER_ACCOUNT;
        }

        public static String ACCOUNT_NAME() {
            return CalendarContract.Calendars.ACCOUNT_NAME;
        }

        public static String ACCOUNT_TYPE() {
            return CalendarContract.Calendars.ACCOUNT_TYPE;
        }

        public static String NAME() {
            return CalendarContract.Calendars.NAME;
        }

        public static String CALENDAR_DISPLAY_NAME() {
            return CalendarContract.Calendars.CALENDAR_DISPLAY_NAME;
        }

        public static String CALENDAR_COLOR() {
            return CalendarContract.Calendars.CALENDAR_COLOR;
        }

        public static String CALENDAR_ACCESS_LEVEL() {
            return CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL;
        }

        public static int CAL_ACCESS_CONTRIBUTOR() {
            return CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR;
        }

        public static int CAL_ACCESS_EDITOR() {
            return CalendarContract.Calendars.CAL_ACCESS_EDITOR;
        }

        public static int CAL_ACCESS_FREEBUSY() {
            return CalendarContract.Calendars.CAL_ACCESS_FREEBUSY;
        }

        public static int CAL_ACCESS_NONE() {
            return CalendarContract.Calendars.CAL_ACCESS_NONE;
        }

        public static int CAL_ACCESS_OWNER() {
            return CalendarContract.Calendars.CAL_ACCESS_OWNER;
        }

        public static int CAL_ACCESS_READ() {
            return CalendarContract.Calendars.CAL_ACCESS_READ;
        }

        public static int CAL_ACCESS_RESPOND() {
            return CalendarContract.Calendars.CAL_ACCESS_RESPOND;
        }

        public static int CAL_ACCESS_ROOT() {
            return CalendarContract.Calendars.CAL_ACCESS_ROOT;
        }

        public static String VISIBLE() {
            return CalendarContract.Calendars.VISIBLE;
        }

        public static String SYNC_EVENTS() {
            return CalendarContract.Calendars.SYNC_EVENTS;
        }

        public static String CAN_PARTIALLY_UPDATE() {
            return CalendarContract.Calendars.CAN_PARTIALLY_UPDATE;
        }

        public static Uri CONTENT_URI() {
            return CalendarContract.Calendars.CONTENT_URI;
        }

        public static String _ID() {
            return CalendarContract.Calendars._ID;
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        public static String ALLOWED_ATTENDEE_TYPES() {
            return CalendarContract.Calendars.ALLOWED_ATTENDEE_TYPES;
        }

    }

    public static class Attendees {

        public static int TYPE_NONE() {
            return CalendarContract.Attendees.TYPE_NONE;
        }

    }

    public static class Events {

        public static String ACCOUNT_NAME() {
            return CalendarContract.Events.ACCOUNT_NAME;

        }

        public static String ACCOUNT_TYPE() {
            return CalendarContract.Events.ACCOUNT_TYPE;
        }

        public static String UID_2445() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return CalendarContract.Events.UID_2445;
            } else {
                return null;
            }
        }

        public static String DTSTART() {
            return CalendarContract.Events.DTSTART;
        }

        public static String DTEND() {
            return CalendarContract.Events.DTEND;
        }

        public static String TITLE() {
            return CalendarContract.Events.TITLE;
        }

        public static String DESCRIPTION() {
            return CalendarContract.Events.DESCRIPTION;
        }

        public static String EVENT_LOCATION() {
            return CalendarContract.Events.EVENT_LOCATION;
        }

        public static String _SYNC_ID() {
            return CalendarContract.Events._SYNC_ID;
        }

        public static String SYNC_LOCAL_ID() {
            return CalendarContract.Events.SYNC_DATA2; // sync_data2 : _sync_local_id
        }

        public static String DIRTY() {
            return CalendarContract.Events.DIRTY;
        }

        public static String _ID() {
            return CalendarContract.Events._ID;
        }

        public static Uri CONTENT_URI() {
            return CalendarContract.Events.CONTENT_URI;
        }

        public static String CALENDAR_ID() {
            return CalendarContract.Events.CALENDAR_ID;
        }

        public static String EVENT_TIMEZONE() {
            return CalendarContract.Events.EVENT_TIMEZONE;
        }

        public static String AVAILABILITY() {
            return CalendarContract.Events.AVAILABILITY;
        }

        public static Object AVAILABILITY_BUSY() {
            return CalendarContract.Events.AVAILABILITY_BUSY;
        }

        public static Object AVAILABILITY_FREE() {
            return CalendarContract.Events.AVAILABILITY_FREE;
        }

        public static String DELETED() {
            return CalendarContract.Events.DELETED;
        }

        public static String SYNC_ID_CUSTOM() {
            return CalendarContract.Events.SYNC_DATA10;
        }

        public static String ALL_DAY() {
            return CalendarContract.Events.ALL_DAY;
        }
    }
}
