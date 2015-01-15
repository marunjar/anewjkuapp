package org.voidsink.anewjkuapp.utils;

import com.github.mikephil.charting.data.Entry;

public class EctsEntry extends Entry {

    private final float mEcts;

    public EctsEntry(float val, int xIndex) {
        this(val, val, xIndex);
    }

    public EctsEntry(float val, float ects, int xIndex) {
        super(val, xIndex);
        this.mEcts = ects;
    }

    public float getEcts() {
        return mEcts;
    }
}