package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.fragment.StatFragmentDetail;

import java.util.List;

public class StatTabItem extends TermTabItem {

    public StatTabItem(String title, List<String> terms) {
        super(title, terms, StatFragmentDetail.class);
    }

}
