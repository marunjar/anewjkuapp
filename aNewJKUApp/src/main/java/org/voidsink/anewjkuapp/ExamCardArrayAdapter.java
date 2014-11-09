package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.base.StickyCardArrayAdapter;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by paul on 06.09.2014.
 */
public class ExamCardArrayAdapter extends StickyCardArrayAdapter {

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
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        final TextView tvHeaderTitle = (TextView) mInflater.inflate(R.layout.exam_card_header, null);

        Card card = getItem(position);
        if (card instanceof ExamCard) {
            tvHeaderTitle.setText(DateFormat.getDateInstance().format(((ExamCard) card).getExam().getDate()));
        }
        return tvHeaderTitle;
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
}
