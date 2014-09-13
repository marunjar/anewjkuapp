package org.voidsink.anewjkuapp.calendar;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang.time.DateUtils;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.fragment.CalendarFragment;
import org.voidsink.anewjkuapp.fragment.MapFragment;

import java.text.DateFormat;
import java.util.BitSet;
import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;

/**
 * Created by paul on 06.09.2014.
 */
public class CalendarCard extends Card {

    private long mEventId = 0;
    private int mColor;
    private String mDescr;
    private String mTime;
    private String mLocation;
    private long mDtStart;
    private long mDtEnd;

    public CalendarCard(final Context c, long eventId, int color, String title, String descr,
                        String location, long dtStart, long dtEnd) {
        this(c);

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

        CardHeader header = getCardHeader();
        if (header != null) {
            header.setTitle(getTitle());
        }
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
        if (getCardHeader() != null) {
            title.setVisibility(View.GONE); //--> shown in cardHeader
        } else{
            title.setText(getTitle());
        }

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
        super(new ContextThemeWrapper(context, R.style.AppTheme), innerLayout);

        // init header
        CardHeader header = new CardHeader(new ContextThemeWrapper(context, R.style.AppTheme));

        header.setPopupMenu(R.menu.calendar_card_popup_menu, new CardHeader.OnClickCardHeaderPopupMenuListener(){
            @Override
            public void onMenuItemClick(BaseCard card, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.show_in_calendar: {
                        Uri uri = ContentUris.withAppendedId(CalendarContractWrapper.Events.CONTENT_URI(), mEventId);
                        Intent intent = new Intent(Intent.ACTION_VIEW)
                                .setData(uri);
                        mContext.startActivity(intent);
                        break;
                    }
                    case R.id.show_on_map: {
                        Intent intent = new Intent(mContext, MainActivity.class).putExtra(
                                MainActivity.ARG_SHOW_FRAGMENT,
                                MapFragment.class.getName()).setAction(
                                Intent.ACTION_SEARCH).addFlags(
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        if (card instanceof CalendarCard) {
                            intent.putExtra(SearchManager.QUERY, ((CalendarCard) card).getLocation());
                            intent.putExtra(MainActivity.ARG_EXACT_LOCATION, true);
                        } else {
                            intent.putExtra(SearchManager.QUERY, "Uniteich");
                        }
                        mContext.startActivity(intent);
                        break;
                    }
                }
            }
        });

        addCardHeader(header);
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
