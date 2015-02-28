package org.voidsink.anewjkuapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.view.MenuItem;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.map.android.layer.MyLocationOverlay;
import org.mapsforge.map.model.MapViewPosition;

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
                TypedArray themeArray = mContext.getTheme().obtainStyledAttributes(new int[] {android.R.attr.colorAccent});
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
