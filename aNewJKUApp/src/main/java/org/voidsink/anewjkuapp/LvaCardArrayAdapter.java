package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.view.LvaCardListView;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by paul on 06.09.2014.
 */
public class LvaCardArrayAdapter extends CardArrayAdapter implements StickyListHeadersAdapter {

    protected LvaCardListView mLvaListView;

    /**
     * Constructor
     *
     * @param context The current context.
     * @param cards   The cards to represent in the ListView.
     */
    public LvaCardArrayAdapter(Context context, List<Card> cards) {
        super(context, cards);
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {

        // Build your custom HeaderView
        //In this case I will use a Card, but you can use any view

        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View view = mInflater.inflate(R.layout.lva_card_header, null);

        CardView cardView= (CardView)view.findViewById(R.id.lva_card_header_id);
        Card card = getItem(position);
        if (card instanceof LvaCard) {
            Card headerCard = new Card(getContext());
            String header = getContext().getString(((LvaCard) card).getLva().getState().getStringResID());

            List<LvaWithGrade> lvas = new ArrayList<>();
            for (int i = 0; i < getCount(); i++) {
                Card c = getItem(i);
                if (c instanceof LvaCard) {
                    lvas.add(((LvaCard) c).getLva());
                }
            }
            header += String.format(" (%.2f ECTS)", AppUtils.getECTS(((LvaCard) card).getLva().getState(), lvas));

            headerCard.setTitle(header);
            cardView.setCard(headerCard);
        }
        return view;
    }

    @Override
    public long getHeaderId(int position) {
        Card card = getItem(position);
        if (card instanceof LvaCard) {
            return((LvaCard) card).getLva().getState().getStringResID();
        }
        return 0;
    }


    public void setLvaListView(LvaCardListView lvaListView) {
        this.mLvaListView = lvaListView;
    }}
