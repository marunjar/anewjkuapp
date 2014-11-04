package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.view.GradeCardListView;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by paul on 06.09.2014.
 */
public class GradeCardArrayAdapter extends CardArrayAdapter implements StickyListHeadersAdapter {

    protected GradeCardListView mGradeListView;

    /**
     * Constructor
     *
     * @param context The current context.
     * @param cards   The cards to represent in the ListView.
     */
    public GradeCardArrayAdapter(Context context, List<Card> cards) {
        super(context, cards);
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
        // Build your custom HeaderView
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        final TextView tvHeaderTitle = (TextView) mInflater.inflate(R.layout.grade_card_header, null);

        Card card = getItem(position);
        if (card instanceof GradeCard) {
            tvHeaderTitle.setText(getContext().getString(((GradeCard) card).getGrade().getGradeType().getStringResID()));

        }
        return tvHeaderTitle;
    }

    @Override
    public long getHeaderId(int position) {
        Card card = getItem(position);
        if (card instanceof GradeCard) {
            return ((GradeCard) card).getGrade().getGradeType().getStringResID();
        }
        return 0;
    }


    public void setGradeListView(GradeCardListView lvaListView) {
        this.mGradeListView = lvaListView;
    }}
