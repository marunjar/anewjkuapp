/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2020 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.view.MenuItem;

import androidx.core.content.ContextCompat;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.map.model.IMapViewPosition;
import org.voidsink.anewjkuapp.base.MyLocationOverlay;

public class LocationOverlay extends MyLocationOverlay {

    private final Context mContext;
    private MenuItem snapToLocationItem;

    public LocationOverlay(Activity activity, IMapViewPosition mapViewPosition,
                           Bitmap bitmap) {
        super(activity, mapViewPosition, bitmap);
        this.mContext = activity;
    }

    public LocationOverlay(Activity activity, IMapViewPosition mapViewPosition,
                           Bitmap bitmap, Paint circleFill, Paint circleStroke) {
        super(activity, mapViewPosition, bitmap, circleFill, circleStroke);
        this.mContext = activity;
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

        onSnapToLocationChanged();
    }

    private void onSnapToLocationChanged() {
        if (this.snapToLocationItem != null) {
            final boolean snapToLocationEnabled = isSnapToLocationEnabled();

            this.snapToLocationItem.setChecked(snapToLocationEnabled);
            if (snapToLocationEnabled) {
                // get accent color from theme
                TypedArray themeArray = mContext.getTheme().obtainStyledAttributes(new int[]{androidx.appcompat.R.attr.colorAccent});
                int mColorAccent = themeArray.getColor(0, ContextCompat.getColor(mContext, R.color.default_secondary));
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
