package org.voidsink.anewjkuapp;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;

import org.voidsink.anewjkuapp.base.BaseAsyncTask;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.notification.CalendarChangedNotification;
import org.voidsink.anewjkuapp.notification.SyncNotification;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;

public class ImportCalendarTask extends BaseAsyncTask<Void, Void, Void> {

	private static final String TAG = ImportCalendarTask.class.getSimpleName();

	private static final Object sync_lock = new Object();

	private final CalendarBuilder mCalendarBuilder;

	private ContentProviderClient mProvider;
	private Account mAccount;
	private SyncResult mSyncResult;
	private Context mContext;
	private String mCalendarName;
	private ContentResolver mResolver;

	private final long mSyncFromNow;

	private boolean isSync;
	private SyncNotification mUpdateNotification;
	private CalendarChangedNotification mNotification;

	public static final String[] EVENT_PROJECTION = new String[] {
			CalendarContractWrapper.Events._ID(), //
			CalendarContractWrapper.Events.EVENT_LOCATION(), // VEvent.getLocation()
			CalendarContractWrapper.Events.TITLE(), // VEvent.getSummary()
			CalendarContractWrapper.Events.DESCRIPTION(), // VEvent.getDescription()
			CalendarContractWrapper.Events.DTSTART(), // VEvent.getStartDate()
			CalendarContractWrapper.Events.DTEND(), // VEvent.getEndDate()
			CalendarContractWrapper.Events._SYNC_ID(), // VEvent.getUID()
			CalendarContractWrapper.Events.DIRTY(),
			CalendarContractWrapper.Events.DELETED(),
			CalendarContractWrapper.Events.CALENDAR_ID() };

	// Constants representing column positions from PROJECTION.
	public static final int COLUMN_EVENT_ID = 0;
	public static final int COLUMN_EVENT_LOCATION = 1;
	public static final int COLUMN_EVENT_TITLE = 2;
	public static final int COLUMN_EVENT_DESCRIPTION = 3;
	public static final int COLUMN_EVENT_DTSTART = 4;
	public static final int COLUMN_EVENT_DTEND = 5;
	public static final int COLUMN_EVENT_KUSSS_ID = 6;
	public static final int COLUMN_EVENT_DIRTY = 7;
	public static final int COLUMN_EVENT_DELETED = 8;
	public static final int COLUMN_EVENT_CAL_ID = 9;

	public ImportCalendarTask(Account account, Context context,
			String getTypeID, CalendarBuilder calendarBuilder) {
		this(account, null, null, context.getContentResolver()
				.acquireContentProviderClient(
						CalendarContractWrapper.Events.CONTENT_URI()),
				new SyncResult(), context, getTypeID, calendarBuilder);
		this.isSync = false;
	}

	public ImportCalendarTask(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult,
			Context context, String calendarName, CalendarBuilder calendarBuilder) {
		this.mAccount = account;
		this.mProvider = provider;
		this.mResolver = context.getContentResolver();
		this.mSyncResult = syncResult;
		this.mContext = context;
		this.mCalendarName = calendarName;
		this.mCalendarBuilder = calendarBuilder;
		this.mSyncFromNow = System.currentTimeMillis();
		this.isSync = true;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.d(TAG, "prepare importing calendar");

		if (!isSync) {
			mUpdateNotification = new SyncNotification(mContext,
					R.string.notification_sync_calendar);
			mUpdateNotification.show(CalendarUtils.getCalendarName(mContext,
					this.mCalendarName));
		}
		mNotification = new CalendarChangedNotification(mContext,
				CalendarUtils.getCalendarName(mContext, this.mCalendarName));
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.d(TAG, "Start importing calendar");

		synchronized (sync_lock) {

			updateNotify("Kalender wird gelesen");

			try {

				Log.d(TAG, "setup connection");

				if (KusssHandler.getInstance().isAvailable(
						AppUtils.getAccountAuthToken(mContext, mAccount),
						AppUtils.getAccountName(mContext, mAccount),
						AppUtils.getAccountPassword(mContext, mAccount))) {

					Log.d(TAG, "loading calendar");

					Calendar iCal = null;
					// {{ Load calendar events from resource
					switch (this.mCalendarName) {
					case CalendarUtils.ARG_CALENDAR_EXAM:
						iCal = KusssHandler.getInstance().getExamIcal(
								mCalendarBuilder);
						break;
					case CalendarUtils.ARG_CALENDAR_LVA:
						iCal = KusssHandler.getInstance().getLVAIcal(
								mCalendarBuilder);
						break;
					}
					if (iCal == null) {
						mSyncResult.stats.numParseExceptions++;
					} else {
						List<?> events = iCal.getComponents(Component.VEVENT);

						Log.d(TAG,
								String.format("got %s events", events.size()));

						updateNotify("Termine werden aktualisiert");

						ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

						// Build hash table of incoming entries
						Map<String, VEvent> eventsMap = new HashMap<String, VEvent>();
						for (Object e : events) {
							if (VEvent.class.isInstance(e)) {
								String uid = ((VEvent) e).getUid().getValue();
								// Log.d(TAG, "uid hashed: " + uid);
								// compense DST
								eventsMap.put(uid, (VEvent) e);
							}
						}

						String calendarId = CalendarUtils.getCalIDByName(mContext, mAccount, mCalendarName);

						if (calendarId == null) {
							return null;
						}

						Log.d(TAG, "Fetching local entries for merge with:"
								+ calendarId);

						Uri calUri = CalendarContractWrapper.Events
								.CONTENT_URI();

						// The ID of the recurring event whose instances you are
						// searching
						// for in the Instances table
						String selection = CalendarContractWrapper.Events
								.CALENDAR_ID() + " = ?";
						String[] selectionArgs = new String[] { calendarId };

						Log.d(TAG, "calUri: " + calUri.toString());

						Cursor c = mProvider.query(calUri, EVENT_PROJECTION,
								selection, selectionArgs, null);

						if (c == null) {
							Log.w(TAG, "selection failed");
						} else {
							Log.d(TAG,
									"Found "
											+ c.getCount()
											+ " local entries. Computing merge solution...");

							// Find stale data
							String eventId;
							String eventKusssId;
							String eventLocation;
							String eventTitle;
							String eventDescription;
							long eventDTStart;
							long eventDTEnd;
							boolean eventDirty;
							boolean eventDeleted;

							while (c.moveToNext()) {
								mSyncResult.stats.numEntries++;
								eventId = c.getString(COLUMN_EVENT_ID);
								eventKusssId = c
										.getString(COLUMN_EVENT_KUSSS_ID);
								eventLocation = c
										.getString(COLUMN_EVENT_LOCATION);
								eventTitle = c.getString(COLUMN_EVENT_TITLE);
								eventDescription = c
										.getString(COLUMN_EVENT_DESCRIPTION);
								eventDTStart = c.getLong(COLUMN_EVENT_DTSTART);
								eventDTEnd = c.getLong(COLUMN_EVENT_DTEND);
								eventDirty = "1".equals(c
										.getString(COLUMN_EVENT_DIRTY));
								eventDeleted = "1".equals(c
										.getString(COLUMN_EVENT_DELETED));

								if (eventKusssId != null) {
									VEvent match = eventsMap.get(eventKusssId);
									if (match != null && !eventDeleted) {
										// Entry exists. Remove from entry
										// map
										// to prevent insert later
										eventsMap.remove(eventKusssId);
										// Check to see if the entry needs
										// to be
										// updated
										Uri existingUri = calUri.buildUpon()
												.appendPath(eventId).build();
										if ((!match.getSummary().getValue()
												.trim().equals(eventTitle))
												|| (!match.getLocation()
														.getValue().trim()
														.equals(eventLocation))
												|| (!match
														.getDescription()
														.getValue()
														.equals(eventDescription))
												|| (match.getStartDate()
														.getDate().getTime() != eventDTStart)
												|| (match.getEndDate()
														.getDate().getTime() != eventDTEnd)) {
											// Update existing record
											Log.d(TAG, "Scheduling update: "
													+ existingUri + " dirty="
													+ eventDirty);

											if (mNotification != null) {
												mNotification
														.addUpdate(getEventString(match));
											}

											batch.add(ContentProviderOperation
													.newUpdate(
															KusssContentContract
																	.asEventSyncAdapter(
																			existingUri,
																			mAccount.name,
																			mAccount.type))
													.withValue(
															CalendarContractWrapper.Events
																	.EVENT_LOCATION(),
															match.getLocation()
																	.getValue()
																	.trim())
													.withValue(
															CalendarContractWrapper.Events
																	.TITLE(),
															match.getSummary()
																	.getValue()
																	.trim())
													.withValue(
															CalendarContractWrapper.Events
																	.DESCRIPTION(),
															match.getDescription()
																	.getValue())
													.withValue(
															CalendarContractWrapper.Events
																	.DTSTART(),
															eventDTStart)
													.withValue(
															CalendarContractWrapper.Events
																	.DTEND(),
															eventDTEnd).build());
											mSyncResult.stats.numUpdates++;
										} else {
											mSyncResult.stats.numSkippedEntries++;
											// Log.d(TAG, "No action: " +
											// eventKusssId);
										}
									} else {
										Log.d(TAG,
												"may delete event: "
														+ eventKusssId
														+ " "
														+ eventDTStart
														+ " > "
														+ (mSyncFromNow - DateUtils.DAY_IN_MILLIS));
										if (eventDeleted
												|| ((eventKusssId != null)
														&& (!eventKusssId
																.isEmpty()) && (eventDTStart > (mSyncFromNow - DateUtils.DAY_IN_MILLIS)))) {
											// Entry doesn't exist. Remove
											// only
											// newer
											// events from the database.
											Uri deleteUri = calUri.buildUpon()
													.appendPath(eventId)
													.build();
											Log.d(TAG, "Scheduling delete: "
													+ deleteUri);

											if (!eventDeleted) {
												mNotification
														.addDelete(getEventString(
																eventDTStart,
																eventDTEnd,
																eventTitle));
											}

											batch.add(ContentProviderOperation
													.newDelete(
															KusssContentContract
																	.asEventSyncAdapter(
																			deleteUri,
																			mAccount.name,
																			mAccount.type))
													.build());
											mSyncResult.stats.numDeletes++;
										} else {
											mSyncResult.stats.numSkippedEntries++;
										}
									}
								} else {
									Log.i(TAG,
											"SyncID not set, ignore event: dirty="
													+ eventDirty + " title="
													+ eventTitle);
								}
							}
							c.close();

							Log.d(TAG, "Cursor closed, " + eventsMap.size()
									+ " events left");

							// Add new items
							for (VEvent v : eventsMap.values()) {
								updateNotify("Termine werden hinzugefügt");

								mNotification.addInsert(getEventString(v));

								Builder builder = ContentProviderOperation
										.newInsert(
												KusssContentContract
														.asEventSyncAdapter(
																CalendarContractWrapper.Events
																		.CONTENT_URI(),
																mAccount.name,
																mAccount.type))
										.withValue(
												CalendarContractWrapper.Events
														.CALENDAR_ID(),
												calendarId)
										.withValue(
												CalendarContractWrapper.Events
														.EVENT_LOCATION(),
												v.getLocation().getValue()
														.trim())
										.withValue(
												CalendarContractWrapper.Events
														.TITLE(),
												v.getSummary().getValue()
														.trim())
										.withValue(
												CalendarContractWrapper.Events
														.DESCRIPTION(),
												v.getDescription().getValue())
										.withValue(
												CalendarContractWrapper.Events
														._SYNC_ID(),
												v.getUid().getValue())
										.withValue(
												CalendarContractWrapper.Events
														.DTSTART(),
												v.getStartDate().getDate()
														.getTime())
										.withValue(
												CalendarContractWrapper.Events
														.DTEND(),
												v.getEndDate().getDate()
														.getTime())
										.withValue(
												CalendarContractWrapper.Events
														.EVENT_TIMEZONE(),
												TimeZone.getDefault().getID());

								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
									if (mCalendarName
											.equals(CalendarUtils.ARG_CALENDAR_EXAM)) {
										builder.withValue(
												CalendarContractWrapper.Events
														.AVAILABILITY(),
												CalendarContractWrapper.Events
														.AVAILABILITY_BUSY());
									} else {
										builder.withValue(
												CalendarContractWrapper.Events
														.AVAILABILITY(),
												CalendarContractWrapper.Events
														.AVAILABILITY_FREE());
									}
								}

								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
									builder.withValue(
											CalendarContractWrapper.Events
													.UID_2445(), v.getUid()
													.getValue());
								}

								ContentProviderOperation op = builder.build();
								Log.d(TAG, "Scheduling insert: "
										+ v.getUid().getValue());
								batch.add(op);

								mSyncResult.stats.numInserts++;
							}

							if (batch.size() > 0) {
								updateNotify("Termine werden gespeichert");

								Log.d(TAG, "Applying batch update");
								mProvider.applyBatch(batch);
								Log.d(TAG, "Notify resolver");
								mResolver.notifyChange(calUri.buildUpon()
										.appendPath(calendarId).build(), // URI
																			// where
																			// data
																			// was
																			// modified
										null, // No local observer
										false); // IMPORTANT: Do not sync to
												// network
							} else {
								Log.w(TAG,
										"No batch operations found! Do nothing");
							}
						}
					}
				} else {
					mSyncResult.stats.numAuthExceptions++;
				}
			} catch (Exception e) {
				Log.e(TAG, "import calendar failed", e);
			}
		}

		setImportDone();

		if (mUpdateNotification != null) {
			mUpdateNotification.cancel();
		}
		mNotification.show();

		return null;
	}

	private void updateNotify(String string) {
		if (mUpdateNotification != null) {
			mUpdateNotification.update(string);
		}
	}

	private String getEventString(VEvent v) {
		return getEventString(v.getStartDate().getDate().getTime(), v
				.getEndDate().getDate().getTime(), v.getSummary().getValue()
				.trim());
	}

	private String getEventString(long eventDTStart, long eventDTEnd,
			String eventTitle) {
		DateFormat df = DateFormat.getDateInstance();

		int index = eventTitle.indexOf(", ");
		if (index > 1) {
			eventTitle = eventTitle.substring(0, index);
		}

		return String.format("%s: %s", df.format(new Date(eventDTStart)),
				eventTitle);
	}

}
