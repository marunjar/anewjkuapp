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

import android.net.Uri;
import android.provider.CalendarContract;

public interface PoiContentContract {

    String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.poi";

    String CONTENT_TYPE_DIR = "vnd.android.cursor.dir";
    String CONTENT_TYPE_ITEM = "vnd.android.cursor.item";

    Uri CONTENT_URI = Uri.parse(String.format("content://%1$s",
            AUTHORITY));

    static String getFTS() {
        return "fts4";
    }

    interface Poi {
        String PATH = "poi";
        String MIMETYPE = "vnd.anewjkuapp.poi";

        Uri CONTENT_URI = PoiContentContract.CONTENT_URI
                .buildUpon().appendPath(PATH).build();

        // DB Table consts
        String TABLE_NAME = "poi";
        String COL_ROWID = "rowid";
        String COL_LAT = "latitude";
        String COL_LON = "longtitude";
        String COL_NAME = "name";
        String COL_DESCR = "description";
        String COL_ADR_STREET = "adr_street";
        String COL_ADR_CITY = "adr_city";
        String COL_ADR_STATE = "adr_state";
        String COL_ADR_COUNTRY = "adr_country";
        String COL_ADR_POSTAL_CODE = "adr_postal_code";
        String COL_IS_DEFAULT = "from_user";

        interface DB {
            String[] PROJECTION = new String[]{
                    Poi.COL_ROWID,
                    Poi.COL_NAME,
                    Poi.COL_LON,
                    Poi.COL_LAT,
                    Poi.COL_DESCR,
                    Poi.COL_IS_DEFAULT};

            int COL_ID = 0;
            int COL_NAME = 1;
            int COL_LON = 2;
            int COL_LAT = 3;
            int COL_DESCR = 4;
            int COL_IS_DEFAULT = 5;

        }
    }

    static Uri asEventSyncAdapter(Uri uri, String account,
                                  String accountType) {
        return uri
                .buildUpon()
                .appendQueryParameter(
                        CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(
                        CalendarContract.Events.ACCOUNT_NAME, account)
                .appendQueryParameter(
                        CalendarContract.Events.ACCOUNT_TYPE,
                        accountType).build();
    }

}
