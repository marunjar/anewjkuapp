package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.fragment.LvaDetailFragment;

import java.util.List;

public class LvaTabItem extends TermTabItem {

    public LvaTabItem(String title, List<String> terms) {
        super(title, terms, LvaDetailFragment.class);
    }

}
