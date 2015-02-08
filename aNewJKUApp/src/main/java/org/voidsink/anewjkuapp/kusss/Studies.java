package org.voidsink.anewjkuapp.kusss;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.update.ImportStudiesTask;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;
import org.voidsink.anewjkuapp.utils.Analytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by paul on 24.11.2014.
 */
public class Studies {

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static final String TAG = Studies.class.getSimpleName();

    private boolean mIsStandard;
    private String mSkz;
    private String mTitle;
    private boolean mSteopDone;
    private boolean mActive;
    private String mUni;
    private Date mDtStart;
    private Date mDtEnd;

    public Studies(Context c, Element row) {
        Elements columns = row.getElementsByTag("td");
        if (columns.size() >= 8) {
            setIsStandard(columns.get(0).getElementsByAttributeValue("checked", "checked").size() > 0);

            setSkz(columns.get(1).text());

            setTitle(columns.get(2).text());

            setSteopDone(parseSteopDone(columns.get(3).text()));

            setActive(parseActive(columns.get(4).text()));

            setUni(columns.get(5).text());

            setDtStart(parseDate(c, columns.get(6).text()));

            setDtEnd(parseDate(c, columns.get(7).text()));
        }
    }

    public Studies(Date dtStart, Date dtEnd) {
        this.mDtStart = dtStart;
        this.mDtEnd = dtEnd;
    }

    public Studies(Cursor c) {
        this.mIsStandard = KusssDatabaseHelper.toBool(c.getInt(ImportStudiesTask.COLUMN_STUDIES_IS_STD));
        this.mSkz = c.getString(ImportStudiesTask.COLUMN_STUDIES_SKZ);
        this.mTitle = c.getString(ImportStudiesTask.COLUMN_STUDIES_TITLE);
        this.mSteopDone = KusssDatabaseHelper.toBool(c.getInt(ImportStudiesTask.COLUMN_STUDIES_STEOP_DONE));
        this.mActive = KusssDatabaseHelper.toBool(c.getInt(ImportStudiesTask.COLUMN_STUDIES_ACTIVE_STATE));
        this.mUni = c.getString(ImportStudiesTask.COLUMN_STUDIES_UNI);
        this.mDtStart = new Date(c.getLong(ImportStudiesTask.COLUMN_STUDIES_DT_START));
        if (!c.isNull(ImportStudiesTask.COLUMN_STUDIES_DT_END)) {
            this.mDtEnd = new Date(c.getLong(ImportStudiesTask.COLUMN_STUDIES_DT_END));
        }
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(KusssContentContract.Studies.COL_IS_STD, KusssDatabaseHelper.toInt(isStandard()));
        cv.put(KusssContentContract.Studies.COL_SKZ, getSkz());
        cv.put(KusssContentContract.Studies.COL_TITLE, getTitle());
        cv.put(KusssContentContract.Studies.COL_STEOP_DONE, KusssDatabaseHelper.toInt(isSteopDone()));
        cv.put(KusssContentContract.Studies.COL_ACTIVE_STATE, KusssDatabaseHelper.toInt(isActive()));
        cv.put(KusssContentContract.Studies.COL_UNI, getUni());
        cv.put(KusssContentContract.Studies.COL_DT_START, getDtStart().getTime());

        Date date = getDtEnd();
        if (date != null) {
            cv.put(KusssContentContract.Studies.COL_DT_END, date.getTime());
        } else {
            cv.putNull(KusssContentContract.Studies.COL_DT_END);
        }

        return cv;
    }

    private Date parseDate(Context c, String text) {
        try {
            if (!TextUtils.isEmpty(text)) {
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
        return !TextUtils.isEmpty(mSkz) && !TextUtils.isEmpty(mUni) && (mDtStart != null);
    }

    private void setSkz(String skz) {
        this.mSkz = skz;
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

    public void setDtStart(Date dtStart) {
        this.mDtStart = dtStart;
    }

    public void setDtEnd(Date dtEnd) {
        this.mDtEnd = dtEnd;
    }

    public boolean isStandard() {
        return mIsStandard;
    }

    public String getSkz() {
        return mSkz;
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

    public String getKey() {
        return getKey(getSkz(), getDtStart());
    }

    public static String getKey(String skz, Date dtStart) {
        return skz + "-" + dateFormat.format(dtStart);
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
