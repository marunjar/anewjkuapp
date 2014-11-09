package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.base.StickyCardArrayAdapter;
import org.voidsink.anewjkuapp.mensa.Mensa;
import org.voidsink.anewjkuapp.mensa.MensaDay;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by paul on 06.09.2014.
 */
public class MenuCardArrayAdapter extends StickyCardArrayAdapter {

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

        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View view = mInflater.inflate(R.layout.menu_card_header, viewGroup, false);

        Card card = getItem(position);
        if (card instanceof MenuBaseCard) {
            final TextView tvHeaderTitle = (TextView) view;
            if (mUseDateHeader) {
                MensaDay day = ((MenuBaseCard) card).getDay();
                if (day != null) {
                    tvHeaderTitle.setText(DateFormat.getDateInstance().format(day.getDate()));
                }
            } else {
                Mensa mensa = ((MenuBaseCard) card).getMensa();
                if (mensa != null) {
                    tvHeaderTitle.setText(mensa.getName());
                }
            }
        }
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
}
