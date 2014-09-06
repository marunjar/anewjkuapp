package org.voidsink.anewjkuapp.calendar;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang.time.DateUtils;
import org.voidsink.anewjkuapp.R;

import java.text.DateFormat;
import java.util.BitSet;
import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by paul on 06.09.2014.
 */
public class CalendarCard extends Card {

    private int mColor;
    private String mDescr;
    private String mTime;
    private String mLocation;
    private long mDtStart;
    private long mDtEnd;

    public CalendarCard(Context c, int color, String title, String descr,
                        String location, long dtStart, long dtEnd) {
        this(c);

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
    public void setupInnerViewElements(ViewGroup parent, View view) {

        View chip = parent
                .findViewById(R.id.calendar_list_item_chip);
        TextView title = (TextView) parent
                .findViewById(R.id.calendar_list_item_title);
        TextView descr = (TextView) parent
                .findViewById(R.id.calendar_list_item_descr);
        TextView time = (TextView) parent
                .findViewById(R.id.calendar_list_item_time);
        TextView location = (TextView) parent
                .findViewById(R.id.calendar_list_item_location);

        chip.setBackgroundColor(getColor());
        title.setText(getTitle());

        if (getDescr().isEmpty()) {
            descr.setVisibility(View.GONE);
        } else {
            descr.setVisibility(View.VISIBLE);
            descr.setText(getDescr());
        }

        if (getTime().isEmpty()) {
            time.setVisibility(View.GONE);
        } else {
            time.setVisibility(View.VISIBLE);
            time.setText(getTime());
        }

        if (getLocation().isEmpty()) {
            location.setVisibility(View.GONE);
        } else {
            location.setVisibility(View.VISIBLE);
            location.setText(getLocation());
        }
    }

    public CalendarCard(Context context) {
        this(context, R.layout.calendar_list_item);
    }

    public CalendarCard(Context context, int innerLayout) {
        super(context, innerLayout);
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
