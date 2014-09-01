package org.voidsink.anewjkuapp;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.map.android.layer.MyLocationOverlay;
import org.mapsforge.map.model.MapViewPosition;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.view.MenuItem;

public class LocationOverlay extends MyLocationOverlay {

	MenuItem snapToLocationItem;

	public LocationOverlay(Context context, MapViewPosition mapViewPosition,
			Bitmap bitmap) {
		super(context, mapViewPosition, bitmap);
	}

	public LocationOverlay(Context context, MapViewPosition mapViewPosition,
			Bitmap bitmap, Paint circleFill, Paint circleStroke) {
		super(context, mapViewPosition, bitmap, circleFill, circleStroke);
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

			this.snapToLocationItem.setChecked(snapToLocationEnabled);
			if (snapToLocationEnabled) {
				this.snapToLocationItem.getIcon()
						.setColorFilter(new PorterDuffColorFilter(CalendarUtils.COLOR_DEFAULT_LVA,
								Mode.SRC_OUT));
			} else {
				this.snapToLocationItem.getIcon()
				.setColorFilter(null);
			}
		}
	}
}
