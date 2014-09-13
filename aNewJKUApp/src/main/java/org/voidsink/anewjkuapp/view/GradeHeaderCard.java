package org.voidsink.anewjkuapp.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.kusss.GradeType;

import java.text.DateFormat;
import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by paul on 13.09.2014.
 */
public class GradeHeaderCard extends Card {

    GradeType mGradeType;

    public GradeHeaderCard(Context context, GradeType gradeType) {
        super(context, R.layout.inner_base_header);

        this.mGradeType = gradeType;

        setTitle(context.getString(gradeType.getStringResID()));
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
