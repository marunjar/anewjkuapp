package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;

/**
 * Created by paul on 06.09.2014.
 */
public class GradeCard extends Card {

    private static final DateFormat df = SimpleDateFormat.getDateInstance();

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
        TextView title = (TextView) view
                .findViewById(R.id.grade_list_grade_title);
        TextView lvaNr = (TextView) view
                .findViewById(R.id.grade_list_grade_lvanr);
        TextView term = (TextView) view
                .findViewById(R.id.grade_list_grade_term);
        TextView date = (TextView) view
                .findViewById(R.id.grade_list_grade_date);
        TextView grade = (TextView) view
                .findViewById(R.id.grade_list_grade_grade);

        TextView chipGrade = (TextView) view.findViewById(R.id.grade_chip_grade);
        TextView chipInfo = (TextView) view.findViewById(R.id.grade_chip_info);
        View chipBack = view.findViewById(R.id.grade_chip_background);

        title.setVisibility(View.GONE);
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

        chipGrade.setText(String.format("%d", mGrade.getGrade().getValue()));
        chipInfo.setText(String.format("%.2f ECTS", mGrade.getEcts()));

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