package org.voidsink.anewjkuapp.kusss;

/**
 * Created by paul on 13.02.2015.
 */
public class Term implements Comparable<Term> {

    private final String mTerm;
    private boolean mLoaded;

    public Term(String term) {
        this.mTerm = term;
        this.mLoaded = false;
    }

    public String getTerm() {
        return mTerm;
    }

    public boolean isLoaded() {
        return mLoaded;
    }

    public void setLoaded(boolean mLoaded) {
        this.mLoaded = mLoaded;
    }

    @Override
    public int compareTo(Term another) {
        if (another == null) return 1;

        return this.getTerm().compareToIgnoreCase(another.getTerm());
    }
}
