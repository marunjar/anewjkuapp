package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.voidsink.anewjkuapp.calendar.CalendarCard;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.view.DateHeaderCard;
import org.voidsink.anewjkuapp.view.ExamCardListView;
import org.voidsink.anewjkuapp.view.GradeCardListView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by paul on 06.09.2014.
 */
public class ExamCardArrayAdapter extends CardArrayAdapter implements StickyListHeadersAdapter {

    protected ExamCardListView mExamListView;

    /**
     * Constructor
     *
     * @param context The current context.
     * @param cards   The cards to represent in the ListView.
     */
    public ExamCardArrayAdapter(Context context, List<Card> cards) {
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
        if (card instanceof ExamCard) {
            Card headerCard = new DateHeaderCard(getContext(), ((ExamCard) card).getExam().getDate());
            cardView.setCard(headerCard);
        }
        return view;
    }

    @Override
    public long getHeaderId(int position) {
        Card card = getItem(position);
        if (card instanceof ExamCard) {

            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTimeInMillis(((ExamCard) card).getExam().getDate().getTime());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        }
        return 0;
    }


    public void setExamListView(ExamCardListView examCardListView) {
        this.mExamListView = examCardListView;
    }}
