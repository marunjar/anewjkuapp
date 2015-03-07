package org.voidsink.anewjkuapp.base;

import android.os.Bundle;

import org.voidsink.anewjkuapp.utils.Consts;

import java.util.Arrays;
import java.util.List;

public class TermFragment extends BaseFragment {

    private List<String> mTerms;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        if (args != null) {
            String[] termArray = args.getStringArray(Consts.ARG_TERMS);

            if (termArray != null && termArray.length > 0) {
                mTerms = Arrays.asList(termArray);

            }
        }
    }

    protected List<String> getTerms() {
        return mTerms;
    }

}
