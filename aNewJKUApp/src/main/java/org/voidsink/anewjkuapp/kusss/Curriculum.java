/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2017 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp.kusss;

import android.content.Context;
import android.text.TextUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.analytics.Analytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Curriculum {

    private static final String TAG = Curriculum.class.getSimpleName();

    private boolean mIsStandard;
    private String mCid;
    private String mTitle;
    private boolean mSteopDone;
    private boolean mActive;
    private String mUni;
    private Date mDtStart;
    private Date mDtEnd;

    public Curriculum(Context c, Element row) {
        Elements columns = row.getElementsByTag("td");
        if (columns.size() >= 8) {
            setIsStandard(columns.get(0).getElementsByAttributeValue("checked", "checked").size() > 0);

            setCid(columns.get(1).text());

            setTitle(columns.get(2).text());

            setSteopDone(parseSteopDone(columns.get(3).text()));

            setActive(parseActive(columns.get(4).text()));

            setUni(columns.get(5).text());

            setDtStart(parseDate(c, columns.get(6).text()));

            setDtEnd(parseDate(c, columns.get(7).text()));
        }
    }

    public Curriculum(Date dtStart, Date dtEnd) {
        this.mDtStart = dtStart;
        this.mDtEnd = dtEnd;
    }

    public Curriculum(boolean isStandard, String cid, String title, boolean steopDone, boolean isActive, String uni, Date dtStart, Date dtEnd) {
        this(dtStart, dtEnd);

        this.mIsStandard = isStandard;
        this.mCid = cid;
        this.mTitle = title;
        this.mSteopDone = steopDone;
        this.mActive = isActive;
        this.mUni = uni;
    }


    private Date parseDate(Context c, String text) {
        try {
            if (!TextUtils.isEmpty(text)) {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
                return dateFormat.parse(text);
            }
            return null;
        } catch (ParseException e) {
            Analytics.sendException(c, e, false, text);
            return null;
        }
    }

    private boolean parseActive(String text) {
        return text.equalsIgnoreCase("aktiv");
    }

    private boolean parseSteopDone(String text) {
        return text.equalsIgnoreCase("abgeschlossen") || text.equalsIgnoreCase("completed");
    }

    private void setIsStandard(boolean isStandard) {
        mIsStandard = isStandard;
    }

    public boolean isInitialized() {
        return !TextUtils.isEmpty(mCid) && !TextUtils.isEmpty(mUni) && (mDtStart != null);
    }

    private void setCid(String cid) {
        this.mCid = cid;
    }

    private void setTitle(String title) {
        this.mTitle = title;
    }

    private void setSteopDone(boolean steopDone) {
        this.mSteopDone = steopDone;
    }

    private void setActive(boolean active) {
        this.mActive = active;
    }

    private void setUni(String uni) {
        this.mUni = uni;
    }

    private void setDtStart(Date dtStart) {
        this.mDtStart = dtStart;
    }

    private void setDtEnd(Date dtEnd) {
        this.mDtEnd = dtEnd;
    }

    public boolean isStandard() {
        return mIsStandard;
    }

    public String getCid() {
        return mCid;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isSteopDone() {
        return mSteopDone;
    }

    public boolean isActive() {
        return mActive;
    }

    public String getUni() {
        return mUni;
    }

    public Date getDtStart() {
        return mDtStart;
    }

    public Date getDtEnd() {
        return mDtEnd;
    }

    public boolean dateInRange(Date date) {
        if (date == null) {
            return false;
        }

        if (date.before(getDtStart())) {
            return false;
        }

        if (getDtEnd() != null && date.after(getDtEnd())) {
            return false;
        }

        return true;
    }
}
