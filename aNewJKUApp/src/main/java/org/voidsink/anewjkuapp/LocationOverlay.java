/*
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
 *
 */

package org.voidsink.anewjkuapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.view.MenuItem;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.map.model.MapViewPosition;
import org.voidsink.anewjkuapp.base.MyLocationOverlay;

public class LocationOverlay extends MyLocationOverlay {

    private final Context mContext;
    private MenuItem snapToLocationItem;

    public LocationOverlay(Context context, MapViewPosition mapViewPosition,
                           Bitmap bitmap) {
        super(context, mapViewPosition, bitmap);
        this.mContext = context;
    }

    public LocationOverlay(Context context, MapViewPosition mapViewPosition,
                           Bitmap bitmap, Paint circleFill, Paint circleStroke) {
        super(context, mapViewPosition, bitmap, circleFill, circleStroke);
        this.mContext = context;
    }

    public void setSnapToLocationItem(MenuItem item) {
        if (item != null && item.isCheckable()) {
            this.snapToLocationItem = item;
        } else {
            this.snapToLocationItem = null;
        }
    }

    @Override
    public synchronized void setSnapToLocationEnabled(
            boolean snapToLocationEnabled) {
        super.setSnapToLocationEnabled(snapToLocationEnabled);

        onSnapToLocationChanged(snapToLocationEnabled);
    }

    private void onSnapToLocationChanged(boolean snapToLocationEnabled) {
        if (this.snapToLocationItem != null) {
            this.snapToLocationItem.setChecked(isSnapToLocationEnabled());
            if (isSnapToLocationEnabled()) {

                // get accent color from theme
                TypedArray themeArray = mContext.getTheme().obtainStyledAttributes(new int[]{R.attr.colorAccent});
                int mColorAccent = themeArray.getColor(0, mContext.getResources().getColor(R.color.default_accent));
                themeArray.recycle();

                this.snapToLocationItem.getIcon()
                        .setColorFilter(mColorAccent, Mode.MULTIPLY);
            } else {
                this.snapToLocationItem.getIcon()
                        .setColorFilter(null);
            }
        }
    }
}
