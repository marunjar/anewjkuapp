package org.voidsink.anewjkuapp.calendar;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;

@SuppressLint("NewApi")
public final class CalendarContractWrapper {

	private CalendarContractWrapper() {

	}

	public static boolean useSDK() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	public static String AUTHORITY() {
		if (useSDK()) {
			return CalendarContract.AUTHORITY;
		} else {
			return "com.android.calendar";
		}
	}

	public static String CALLER_IS_SYNCADAPTER() {
		if (useSDK()) {
			return CalendarContract.CALLER_IS_SYNCADAPTER;
		} else {
			return "caller_is_syncadapter";
		}
	}

    public static Uri CONTENT_URI() {
        if (useSDK()) {
            return CalendarContract.CONTENT_URI;
        } else {
            return Uri.parse(String.format("content://%1$s",
                    AUTHORITY()));
        }
    }

	public static class Calendars {

		private Calendars() {

		}

		public static String OWNER_ACCOUNT() {
			if (useSDK()) {
				return CalendarContract.Calendars.OWNER_ACCOUNT;
			} else {
				return "ownerAccount";
			}
		}

		public static String ACCOUNT_NAME() {
			if (useSDK()) {
				return CalendarContract.Calendars.ACCOUNT_NAME;
			} else {
				return "_sync_account";
			}
		}

		public static String ACCOUNT_TYPE() {
			if (useSDK()) {
				return CalendarContract.Calendars.ACCOUNT_TYPE;
			} else {
				return "_sync_account_type";
			}
		}

		public static String NAME() {
			if (useSDK()) {
				return CalendarContract.Calendars.NAME;
			} else {
				return "name";
			}
		}

		public static String CALENDAR_DISPLAY_NAME() {
			if (useSDK()) {
				return CalendarContract.Calendars.CALENDAR_DISPLAY_NAME;
			} else {
				return "displayName";
			}
		}

		public static String CALENDAR_COLOR() {
			if (useSDK()) {
				return CalendarContract.Calendars.CALENDAR_COLOR;
			} else {
				return "color";
			}
		}

		public static String CALENDAR_ACCESS_LEVEL() {
			if (useSDK()) {
				return CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL;
			} else {
				return "access_level";
			}
		}

        public static int CAL_ACCESS_CONTRIBUTOR() {
            if (useSDK()) {
                return CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR;
            }
            return 500;
        }

        public static int CAL_ACCESS_EDITOR() {
            if (useSDK()) {
                return CalendarContract.Calendars.CAL_ACCESS_EDITOR;
            }
            return 600;
        }

        public static int CAL_ACCESS_FREEBUSY() {
            if (useSDK()) {
                return CalendarContract.Calendars.CAL_ACCESS_FREEBUSY;
            }
            return 100;
        }

        public static int CAL_ACCESS_NONE() {
            if (useSDK()) {
                return CalendarContract.Calendars.CAL_ACCESS_NONE;
            }
            return 0;
        }

        public static int CAL_ACCESS_OWNER() {
            if (useSDK()) {
                return CalendarContract.Calendars.CAL_ACCESS_OWNER;
            } else {
                return 700;
            }
        }

        public static int CAL_ACCESS_READ() {
			if (useSDK()) {
				return CalendarContract.Calendars.CAL_ACCESS_READ;
			} else {
				return 200;
			}
		}

        public static int CAL_ACCESS_RESPOND() {
            if (useSDK()) {
                return CalendarContract.Calendars.CAL_ACCESS_RESPOND;
            } else {
                return 300;
            }
        }

        public static int CAL_ACCESS_ROOT() {
            if (useSDK()) {
                return CalendarContract.Calendars.CAL_ACCESS_ROOT;
            } else {
                return 800;
            }
        }

		public static String VISIBLE() {
			if (useSDK()) {
				return CalendarContract.Calendars.VISIBLE;
			} else {
				return null;
			}
		}

		public static String SYNC_EVENTS() {
			if (useSDK()) {
				return CalendarContract.Calendars.SYNC_EVENTS;
			} else {
				return "sync_events";
			}
		}

		public static String CAN_PARTIALLY_UPDATE() {
			if (useSDK()) {
				return CalendarContract.Calendars.CAN_PARTIALLY_UPDATE;
			} else {
				return null;
			}
		}

		public static Uri CONTENT_URI() {
			if (useSDK()) {
				return CalendarContract.Calendars.CONTENT_URI;
			} else {
				return Uri.parse(String.format("content://%1$s/%2$s",
						AUTHORITY(), "calendars"));
			}
		}

		public static String _ID() {
			if (useSDK()) {
				return CalendarContract.Calendars._ID;
			} else {
				return "_id";
			}
		}

		public static String ALLOWED_ATTENDEE_TYPES() {
			if (useSDK()) {
				return CalendarContract.Calendars.ALLOWED_ATTENDEE_TYPES;
			} else {
				return null;
			}
		}

	}

	public static class Attendees {

		public static int TYPE_NONE() {
			if (useSDK()) {
				return CalendarContract.Attendees.TYPE_NONE;
			} else {
				return 0;
			}
		}

	}

	public static class Events {

		public static String ACCOUNT_NAME() {
			if (useSDK()) {
				return CalendarContract.Events.ACCOUNT_NAME;
			} else {
				return "_sync_account";
			}

		}

		public static String ACCOUNT_TYPE() {
			if (useSDK()) {
				return CalendarContract.Events.ACCOUNT_TYPE;
			} else {
				return "_sync_account_type";
			}
		}

		public static String UID_2445() {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				return CalendarContract.Events.UID_2445;
			} else {
				return null;
			}
		}

		public static String DTSTART() {
			if (useSDK()) {
				return CalendarContract.Events.DTSTART;
			} else {
				return "dtstart";
			}
		}

		public static String DTEND() {
			if (useSDK()) {
				return CalendarContract.Events.DTEND;
			} else {
				return "dtend";
			}
		}

		public static String TITLE() {
			if (useSDK()) {
				return CalendarContract.Events.TITLE;
			} else {
				return "title";
			}
		}

		public static String DESCRIPTION() {
			if (useSDK()) {
				return CalendarContract.Events.DESCRIPTION;
			} else {
				return "description";
			}
		}

		public static String EVENT_LOCATION() {
			if (useSDK()) {
				return CalendarContract.Events.EVENT_LOCATION;
			} else {
				return "eventLocation";
			}
		}

		public static String _SYNC_ID() {
			if (useSDK()) {
				return CalendarContract.Events._SYNC_ID;
			} else {
				return "_sync_id";
			}
		}

        public static String SYNC_LOCAL_ID() {
            if (useSDK()) {
                return CalendarContract.Events.SYNC_DATA2; // sync_data2 : _sync_local_id
            } else {
                return "_sync_local_id";
            }
        }

		public static String DIRTY() {
			if (useSDK()) {
				return CalendarContract.Events.DIRTY;
			} else {
				return "_sync_dirty";
			}
		}

		public static String _ID() {
			if (useSDK()) {
				return CalendarContract.Events._ID;
			} else {
				return "_id";
			}
		}

		public static Uri CONTENT_URI() {
			if (useSDK()) {
				return CalendarContract.Events.CONTENT_URI;
			} else {
				return Uri.parse(String.format("content://%1$s/%2$s",
						AUTHORITY(), "events"));
			}
		}

		public static String CALENDAR_ID() {
			if (useSDK()) {
				return CalendarContract.Events.CALENDAR_ID;
			} else {
				return "calendar_id";
			}
		}

		public static String EVENT_TIMEZONE() {
			if (useSDK()) {
				return CalendarContract.Events.EVENT_TIMEZONE;
			} else {
				return "eventTimezone";
			}
		}

		public static String AVAILABILITY() {
			if (useSDK()) {
				return CalendarContract.Events.AVAILABILITY;
			} else {
				return null;
			}
		}

		public static Object AVAILABILITY_BUSY() {
			if (useSDK()) {
				return CalendarContract.Events.AVAILABILITY_BUSY;
			} else {
				return null;
			}
		}

		public static Object AVAILABILITY_FREE() {
			if (useSDK()) {
				return CalendarContract.Events.AVAILABILITY_FREE;
			} else {
				return null;
			}
		}

		public static String DELETED() {
			if (useSDK()) {
				return CalendarContract.Events.DELETED;
			} else {
				return "deleted";
			}
		}
	}
}
