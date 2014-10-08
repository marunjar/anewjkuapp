package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.kusss.ExamGrade;

import java.text.DateFormat;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by paul on 06.09.2014.
 */
public class GradeCard extends Card {

    private ExamGrade mGrade;

    public GradeCard(final Context c, ExamGrade grade) {
        this(c);

        this.mGrade = grade;

        this.setTitle(mGrade.getTitle());

        CardHeader header = getCardHeader();
        if (header != null) {
            header.setTitle(getTitle());
        }
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        TextView lvaNr = (TextView) view
                .findViewById(R.id.grade_list_grade_lvanr);
        TextView term = (TextView) view
                .findViewById(R.id.grade_list_grade_term);
        TextView date = (TextView) view
                .findViewById(R.id.grade_list_grade_date);
        TextView grade = (TextView) view
                .findViewById(R.id.grade_list_grade_grade);
        TextView skz = (TextView) view
                .findViewById(R.id.grade_list_grade_skz);

        View chipBack = view.findViewById(R.id.grade_chip);
        TextView chipInfo = (TextView) view.findViewById(R.id.grade_chip_info);
        TextView chipGrade = (TextView) view.findViewById(R.id.grade_chip_grade);

        if (!mGrade.getLvaNr().isEmpty()) {
            lvaNr.setText(mGrade.getLvaNr());
            lvaNr.setVisibility(View.VISIBLE);
        } else {
            lvaNr.setVisibility(View.GONE);
        }

        if (!mGrade.getTerm().isEmpty()) {
            term.setText(mGrade.getTerm());
            term.setVisibility(View.VISIBLE);
        } else {
            term.setVisibility(View.GONE);
        }

        if (mGrade.getSkz() > 0) {
            skz.setText(String.format("[%d]", mGrade.getSkz()));
            skz.setVisibility(View.VISIBLE);
        } else {
            skz.setVisibility(View.GONE);
        }

        chipGrade.setText(String.format("%d", mGrade.getGrade().getValue()));
        chipInfo.setText(String.format("%.2f ECTS", mGrade.getEcts()));

        final DateFormat df = DateFormat.getDateInstance();

        date.setText(df.format(mGrade.getDate()));
        grade.setText(mContext.getString(mGrade.getGrade()
                .getStringResID()));

        chipBack.setBackgroundColor(mGrade.getGrade().getColor());
    }

    public GradeCard(Context context) {
        this(context, R.layout.grade_list_grade);
    }

    public GradeCard(Context context, int innerLayout) {
        super(context, innerLayout);

        // init header
        CardHeader header = new CardHeader(context);

//        header.setPopupMenu(R.menu.grade_card_popup_menu, new CardHeader.OnClickCardHeaderPopupMenuListener() {
//            @Override
//            public void onMenuItemClick(BaseCard card, MenuItem item) {
//                switch (item.getItemId()) {
//                    default:
//                        Toast.makeText(mContext, "Click on " + item.getTitle(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        addCardHeader(header);
    }

    public ExamGrade getGrade() {
        return mGrade;
    }
}