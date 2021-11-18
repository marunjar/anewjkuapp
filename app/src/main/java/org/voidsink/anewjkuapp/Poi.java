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

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Poi {
    private String mName;
    private double mLat;
    private double mLon;
    private String mDescr;
    private int mId;

    public Poi(@NonNull String name, double lat, double lon) {
        this.mName = name;
        this.mLat = lat;
        this.mLon = lon;
        this.mDescr = "";
        this.mId = -1;
    }

    public Poi(Cursor c) {
        this("", 0, 0);

        this.mName = c.getString(c.getColumnIndexOrThrow(PoiContentContract.Poi.COL_NAME));
        this.mLat = c.getDouble(c.getColumnIndexOrThrow(PoiContentContract.Poi.COL_LAT));
        this.mLon = c.getDouble(c.getColumnIndexOrThrow(PoiContentContract.Poi.COL_LON));
        this.mDescr = c.getString(c.getColumnIndexOrThrow(PoiContentContract.Poi.COL_DESCR));
        this.mId = c.getInt(c.getColumnIndexOrThrow(PoiContentContract.Poi.COL_ROWID));
    }

    @NonNull
    public String getName() {
        return this.mName;
    }

    public int getId() {
        return this.mId;
    }

    public String getDescr() {
        return this.mDescr;
    }

    public double getLat() {
        return mLat;
    }

    public double getLon() {
        return mLon;
    }

    public void parse(Element wpt) {
        NodeList descriptions = wpt.getElementsByTagName("desc");
        if (descriptions.getLength() == 1) {
            Document doc = Jsoup.parse(descriptions.item(0).getTextContent());
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
        return getContentValues(newIsDefault || oldIsDefault); // no way back
    }
}
