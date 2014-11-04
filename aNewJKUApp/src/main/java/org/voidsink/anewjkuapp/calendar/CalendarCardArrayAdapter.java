package org.voidsink.anewjkuapp.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.view.CalendarCardListView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by paul on 06.09.2014.
 */
public class CalendarCardArrayAdapter extends CardArrayAdapter implements StickyListHeadersAdapter {

    protected CalendarCardListView mCardListView;

    /**
     * Constructor
     *
     * @param context The current context.
     * @param cards   The cards to represent in the ListView.
     */
    public CalendarCardArrayAdapter(Context context, List<Card> cards) {
        super(context, cards);
    }


    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
        // Build your custom HeaderView
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        final TextView tvHeaderTitle = (TextView) mInflater.inflate(R.layout.calendar_card_header, null);

        Card card = getItem(position);
        if (card instanceof CalendarCard) {
            tvHeaderTitle.setText(DateFormat.getDateInstance().format(new Date(((CalendarCard) card).getDtStart())));
        }
        return tvHeaderTitle;
    }

    @Override
    public long getHeaderId(int position) {
        Card card = getItem(position);
        if (card instanceof CalendarCard) {

            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTimeInMillis(((CalendarCard) card).getDtStart());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        }
        return 0;
    }


    /**
     * @param cardListView
     *            cardListView
     */
    public void setCardListView(CalendarCardListView cardListView) {
        this.mCardListView = cardListView;
    }}
