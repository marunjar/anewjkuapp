package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.mensa.Mensa;
import org.voidsink.anewjkuapp.mensa.MensaDay;
import org.voidsink.anewjkuapp.view.DateHeaderCard;
import org.voidsink.anewjkuapp.view.MenuCardListView;

import java.util.Calendar;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by paul on 06.09.2014.
 */
public class MenuCardArrayAdapter extends CardArrayAdapter implements StickyListHeadersAdapter {

    protected MenuCardListView mMenuListView;
    protected boolean mUseDateHeader;

    /**
     * Constructor
     *
     * @param context The current context.
     * @param cards   The cards to represent in the ListView.
     */
    public MenuCardArrayAdapter(Context context, List<Card> cards, boolean useDateHeader) {
        super(context, cards);

        this.mUseDateHeader = useDateHeader;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {

        // Build your custom HeaderView
        //In this case I will use a Card, but you can use any view

        Card headerCard = null;
        Card card = getItem(position);
        if (card instanceof MenuBaseCard) {
            if (mUseDateHeader) {
                MensaDay day = ((MenuBaseCard) card).getDay();
                if (day != null) {
                    headerCard = new DateHeaderCard(getContext(), day.getDate());
                }
            } else {
                Mensa mensa = ((MenuBaseCard) card).getMensa();
                if (mensa != null) {
                    headerCard = new Card(getContext());
                    headerCard.setTitle(mensa.getName());
                }
            }
        }

        // create empty card
        if (headerCard == null) {
            headerCard = new Card(getContext());
        }

        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View view = mInflater.inflate(R.layout.menu_card_header, null);

        CardView cardView = (CardView)view.findViewById(R.id.menu_card_header_id);
        cardView.setCard(headerCard);

        return view;
    }

    @Override
    public long getHeaderId(int position) {
        Card card = getItem(position);
        if (card instanceof MenuBaseCard) {
            if (mUseDateHeader) {
                MensaDay day = ((MenuBaseCard) card).getDay();
                if (day != null) {
                    Calendar cal = Calendar.getInstance(); // locale-specific
                    cal.setTimeInMillis(day.getDate().getTime());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    return cal.getTimeInMillis();
                }
            } else {
                Mensa mensa = ((MenuBaseCard) card).getMensa();
                if (mensa != null) {
                    return mensa.getName().hashCode();
                }
            }
        }
        return 0;
    }

    public void setMenuListView(MenuCardListView menuCardListView) {
        this.mMenuListView = menuCardListView;
    }}
