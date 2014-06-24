package org.voidsink.anewjkuapp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.ContentValues;
import android.database.Cursor;

public class Poi {
	private String mName;
	private double mLat;
	private double mLon;
	private String mDescr;
	private int mId;

	public Poi(String name, double lat, double lon) {
		this.mName = name;
		this.mLat = lat;
		this.mLon = lon;
		this.mDescr = "";
		this.mId = -1;
	}
	
	public Poi(Cursor c) {
		this("", 0, 0);
		
		this.mName = c.getString(c.getColumnIndex(PoiContentContract.Poi.COL_NAME));
		this.mLat = c.getDouble(c.getColumnIndex(PoiContentContract.Poi.COL_LAT));
		this.mLon = c.getDouble(c.getColumnIndex(PoiContentContract.Poi.COL_LON));
		this.mDescr = c.getString(c.getColumnIndex(PoiContentContract.Poi.COL_DESCR));
		this.mId = c.getInt(c.getColumnIndex(PoiContentContract.Poi.COL_ROWID));
	}
	
	public String getName() {
		return this.mName;
	}

	public int getId() {
		return this.mId;
	}
	
	public String getDescr() {
		return this.mDescr;
	}

	public void parse(Element wpt) {
		NodeList descriptions = wpt.getElementsByTagName("desc");
		if (descriptions.getLength() == 1) {
			Document doc = Jsoup.parse(((Element) descriptions.item(0)).getTextContent());
			mDescr = doc.text();
		}
	}

	public ContentValues getContentValues(boolean isDefault) {
		ContentValues cv = new ContentValues();
		cv.put(PoiContentContract.Poi.COL_NAME, this.mName);
		cv.put(PoiContentContract.Poi.COL_LAT, this.mLat);
		cv.put(PoiContentContract.Poi.COL_LON, this.mLon);
		cv.put(PoiContentContract.Poi.COL_DESCR, this.mDescr);
		cv.put(PoiContentContract.Poi.COL_IS_DEFAULT,
				KusssDatabaseHelper.toInt(isDefault));
		return cv;
	}

	public ContentValues getContentValues(boolean oldIsDefault,
			boolean newIsDefault) {
		if (oldIsDefault) {
			// no way back
			newIsDefault = oldIsDefault;
		}
		ContentValues cv = getContentValues(newIsDefault);

		return cv;
	}
}
