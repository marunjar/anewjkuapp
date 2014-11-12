package org.voidsink.anewjkuapp;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.base.ThemedCard;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;
import org.voidsink.anewjkuapp.utils.UIUtils;

import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by paul on 06.09.2014.
 */
public class LvaCard extends ThemedCard {

    private LvaWithGrade lva;

    public LvaCard(final Context c, LvaWithGrade lva) {
        this(c);

        this.lva = lva;

        this.setTitle(lva.getLva().getTitle() + " " + lva.getLva().getLvaType());

        CardHeader header = getCardHeader();
        if (header != null) {
            header.setTitle(getTitle());
        }
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        TextView lvaNr = (TextView) view.findViewById(R.id.lva_list2_item_lvanr);
        TextView skz = (TextView) view.findViewById(R.id.lva_list2_item_skz);
        TextView code = (TextView) view.findViewById(R.id.lva_list2_item_code);
        TextView teacher = (TextView) view.findViewById(R.id.lva_list2_item_teacher);

        ViewGroup chipBack = (ViewGroup) view.findViewById(R.id.grade_chip);
        TextView chipEcts = (TextView) chipBack.findViewById(R.id.grade_chip_info);
        TextView chipGrade = (TextView) chipBack.findViewById(R.id.grade_chip_grade);

        UIUtils.setTextAndVisibility(teacher, lva.getLva().getTeacher());
        lvaNr.setText(lva.getLva().getLvaNr());
        skz.setText(String.format("[%s]", lva.getLva().getSKZ()));
        code.setText(lva.getLva().getCode());

        ExamGrade grade = this.lva.getGrade();
        if (grade == null) {
            chipBack.setBackgroundColor(Color.GRAY);
            chipGrade.setText("?");
        } else {
            chipBack.setBackgroundColor(grade.getGrade().getColor());
            chipGrade.setText(String.format("%d", grade.getGrade().getValue()));
        }
        chipEcts.setText(String.format("%.2f ECTS", lva.getLva()
                .getEcts()));
    }

    public LvaCard(Context context) {
        this(context, R.layout.lva_list_item);
    }

    public LvaCard(Context context, int innerLayout) {
        super(context, innerLayout);

        // init header
        CardHeader header = new CardHeader(context);

//        header.setPopupMenu(R.menu.lva_card_popup_menu, new CardHeader.OnClickCardHeaderPopupMenuListener(){
//            @Override
//            public void onMenuItemClick(BaseCard card, MenuItem item) {
//                switch (item.getItemId()) {
//                    default: Toast.makeText(mContext, "Click on " + item.getTitle(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        addCardHeader(header);
    }

    public LvaWithGrade getLva() {
        return this.lva;
    }
}
