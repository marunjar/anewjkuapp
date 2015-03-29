/*******************************************************************************
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 ******************************************************************************/

package org.voidsink.anewjkuapp.base;

import android.content.Context;

import org.voidsink.anewjkuapp.DrawerItem;

public abstract class BaseDrawerItem implements DrawerItem {

    private final String mLabel;
    private final int mLabelResId;

    public BaseDrawerItem(String label) {
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
