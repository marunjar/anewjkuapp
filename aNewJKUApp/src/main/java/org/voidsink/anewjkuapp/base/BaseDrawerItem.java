package org.voidsink.anewjkuapp.base;

import android.content.Context;

import org.voidsink.anewjkuapp.DrawerItem;

/**
 * Created by paul on 06.11.2014.
 */
public abstract class BaseDrawerItem implements DrawerItem{

    private final String mLabel;
    private final int mLabelResId;

    public BaseDrawerItem(String label){
        this.mLabel = label;
        this.mLabelResId = 0;
    }

    public BaseDrawerItem(int labelResId) {
        this.mLabel = "";
        this.mLabelResId = labelResId;
    }

    @Override
    public String getLabel(Context c) {
        String label = "";
        if (mLabelResId > 0) {
            label = c.getString(mLabelResId);
        }
        if (label == null || label.isEmpty()) {
            label = mLabel;
        }

        return label;
    }
}
