package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.kusss.KusssHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.internal.base.BaseCard;

/**
 * Created by paul on 06.09.2014.
 */
public class ExamCard extends Card {

    private static final DateFormat df = SimpleDateFormat.getDateInstance();

    private ExamListExam mExam;

    public ExamCard(Context c, ExamListExam exam) {
        this(c);

        this.mExam = exam;

        this.setTitle(mExam.getTitle());

        CardHeader header = getCardHeader();
        if (header != null) {
            header.setTitle(getTitle());
//            if (!mExam.getDescription().isEmpty()) {
//                header.setButtonExpandVisible(true);
//            }
        }

        if (!mExam.getDescription().isEmpty()) {
            CardExpand expand = new ExamCardExpand(c, mExam.getDescription());
            addCardExpand(expand);
        }

        //Add a viewToClickExpand to enable click on whole card
        if (!mExam.getDescription().isEmpty()) {
            ViewToClickToExpand viewToClickToExpand =
                    ViewToClickToExpand.builder()
                            .highlightView(false)
                            .setupCardElement(ViewToClickToExpand.CardElementUI.CARD);
            setViewToClickToExpand(viewToClickToExpand);
        }
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        TextView info = (TextView) view
                .findViewById(R.id.exam_list_item_info);
        TextView lvaNr = (TextView) view
                .findViewById(R.id.exam_list_item_lvanr);
        TextView term = (TextView) view
                .findViewById(R.id.exam_list_item_term);
        TextView skz = (TextView) view
                .findViewById(R.id.exam_list_item_skz);
        TextView time = (TextView) view
                .findViewById(R.id.exam_list_item_time);
        TextView location = (TextView) view
                .findViewById(R.id.exam_list_item_location);
        View chip = view
                .findViewById(R.id.empty_chip_background);

        if (mExam.mark()) {
            chip.setBackgroundColor(CalendarUtils.COLOR_DEFAULT_EXAM);
        } else {
            chip.setBackgroundColor(CalendarUtils.COLOR_DEFAULT_LVA);
        }

        if (!mExam.getInfo().isEmpty()) {
            info.setText(mExam.getInfo());
            info.setVisibility(View.VISIBLE);
        } else {
            info.setVisibility(View.GONE);
        }
        lvaNr.setText(mExam.getLvaNr());
        term.setText(mExam.getTerm());
        skz.setText(String.format("[%s]", mExam.getSkz()));
        time.setText(mExam.getTime());
        location.setText(mExam.getLocation());
    }

    public ExamCard(Context context) {
        this(context, R.layout.exam_list_item);
    }

    public ExamCard(Context context, int innerLayout) {
        super(context, innerLayout);

        // init header
        CardHeader header = new CardHeader(context);

        header.setPopupMenu(R.menu.exam_card_popup_menu, new CardHeader.OnClickCardHeaderPopupMenuListener() {
            @Override
            public void onMenuItemClick(BaseCard card, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_exam_register: {
                        KusssHandler.getInstance().showExamInBrowser(getContext(), ((ExamCard) card).getExam().getLvaNr());
                        break;
                    }
                    default:
                        Toast.makeText(mContext, "Click on " + item.getTitle(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        addCardHeader(header);
    }

    class ExamCardExpand extends CardExpand {

        String mDescription;

        public ExamCardExpand(Context context, String description) {
            super(context, R.layout.exam_card_expand_section);
            mDescription = description;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {

            TextView descr = (TextView) view.findViewById(R.id.exam_list_item_description);

            if (!mDescription.isEmpty()) {
                descr.setText(mDescription);
            } else {
                descr.setVisibility(View.GONE);
            }
        }
    }

    public ExamListExam getExam() {
        return mExam;
    }
}