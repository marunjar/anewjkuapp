package org.voidsink.anewjkuapp.calendar;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import org.apache.commons.lang.time.DateUtils;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.fragment.MapFragment;

import java.text.DateFormat;
import java.util.Date;


public class CalendarListEvent implements CalendarListItem {

    private final long mEventId;
    private int mColor;
    private String mTitle;
    private String mDescr;
    private String mTime;
    private String mLocation;
    private long mDtStart;
    private long mDtEnd;

    public CalendarListEvent(long eventId, int color, String title, String descr,
                             String location, long dtStart, long dtEnd) {
        this.mEventId = eventId;
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

    public long getEventId() {
        return mEventId;
    }

    public void showOnMap(Context context) {
        Intent intent = new Intent(context, MainActivity.class)
                .putExtra(MainActivity.ARG_SHOW_FRAGMENT, MapFragment.class.getName())
                .putExtra(MainActivity.ARG_SAVE_LAST_FRAGMENT, false)
                .setAction(Intent.ACTION_SEARCH)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (!TextUtils.isEmpty(getLocation())) {
            intent.putExtra(SearchManager.QUERY, getLocation());
            intent.putExtra(MainActivity.ARG_EXACT_LOCATION, true);
        } else {
            intent.putExtra(SearchManager.QUERY, "Uniteich");
        }
        context.startActivity(intent);
    }

    public void showInCalendar(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Uri.Builder builder = CalendarContractWrapper.CONTENT_URI().buildUpon();
            builder.appendPath("time");
            ContentUris.appendId(builder, getDtStart());
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setData(builder.build());
            context.startActivity(intent);
        } else {
            Uri uri = ContentUris.withAppendedId(CalendarContractWrapper.Events.CONTENT_URI(), getEventId());
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setData(uri);
            context.startActivity(intent);
        }
    }
}
