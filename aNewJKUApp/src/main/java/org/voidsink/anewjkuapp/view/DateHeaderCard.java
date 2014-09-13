package org.voidsink.anewjkuapp.view;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by paul on 13.09.2014.
 */
public class DateHeaderCard extends Card {

    Date mDate;

    public DateHeaderCard(Context context, Date date) {
        super(context, R.layout.inner_base_header);

        this.mDate = date;

        setTitle(DateFormat.getDateInstance().format(date));
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Add simple title to header
        if (view != null){
            TextView mTitleView = (TextView) view.findViewById(R.id.card_header_inner_simple_title);
            if (mTitleView != null)
                mTitleView.setText(getTitle());
        }
    }

    public DateHeaderCard(Context context) {
        this(context, new Date());
    }



}
