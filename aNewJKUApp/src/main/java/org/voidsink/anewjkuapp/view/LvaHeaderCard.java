package org.voidsink.anewjkuapp.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.kusss.LvaState;

import java.text.DateFormat;
import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by paul on 13.09.2014.
 */
public class LvaHeaderCard extends Card {

    LvaState mState;

    public LvaHeaderCard(Context context, LvaState state) {
        super(context, R.layout.inner_base_header);

        this.mState = state;

        setTitle(context.getString(state.getStringResID()));
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
}
