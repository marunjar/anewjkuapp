package org.voidsink.anewjkuapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.base.SlidingTabItem;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.List;

public class TermTabItem extends SlidingTabItem {

    private final List<String> mTerms;

    public TermTabItem(CharSequence title, List<String> terms, Class<? extends Fragment> fragment) {
        super(title, fragment);
        this.mTerms = terms;
    }

    protected List<String> getTerms() {
        return mTerms;
    }

    protected Bundle getArguments() {
        Bundle b = new Bundle();

        if (mTerms != null) {
            b.putStringArray(Consts.ARG_TERMS, mTerms.toArray(new String[]{}));
        }

        return b;
    }
}
