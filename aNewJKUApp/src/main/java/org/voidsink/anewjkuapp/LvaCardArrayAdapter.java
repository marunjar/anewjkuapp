package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.view.LvaCardListView;
import org.voidsink.anewjkuapp.view.LvaHeaderCard;

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
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        final TextView tvHeaderTitle = (TextView) mInflater.inflate(R.layout.lva_card_header, null);

        Card card = getItem(position);
        if (card instanceof LvaCard) {
            tvHeaderTitle.setText(getContext().getString(((LvaCard) card).getLva().getState().getStringResID()));
        }
        return tvHeaderTitle;
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
