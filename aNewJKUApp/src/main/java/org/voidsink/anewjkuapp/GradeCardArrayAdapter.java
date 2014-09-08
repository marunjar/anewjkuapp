package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.view.GradeCardListView;

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
        //In this case I will use a Card, but you can use any view

        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View view = mInflater.inflate(R.layout.grade_card_header, null);

        CardView cardView= (CardView)view.findViewById(R.id.grade_card_header_id);
        Card card = getItem(position);
        if (card instanceof GradeCard) {
            Card headerCard = new Card(getContext());
            String header = getContext().getString(((GradeCard) card).getGrade().getGradeType().getStringResID());

            List<ExamGrade> grades = new ArrayList<>();
            for (int i = 0; i < getCount(); i++) {
                Card c = getItem(i);
                if (c instanceof GradeCard) {
                    grades.add(((GradeCard) c).getGrade());
                }
            }
            header += String.format(" (ø %.2f)", AppUtils.getAvgGrade(grades, false, ((GradeCard) card).getGrade().getGradeType()));

            headerCard.setTitle(header);
            cardView.setCard(headerCard);
        }
        return view;
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
