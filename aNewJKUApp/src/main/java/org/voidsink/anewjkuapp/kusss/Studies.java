package org.voidsink.anewjkuapp.kusss;

import android.content.Context;
import android.text.TextUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.utils.Analytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Studies {

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
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

    public Studies(boolean isStandard, String skz, String title, boolean steopDone, boolean isActive, String uni, Date dtStart, Date dtEnd) {
        this(dtStart, dtEnd);

        this.mIsStandard = isStandard;
        this.mSkz = skz;
        this.mTitle = title;
        this.mSteopDone = steopDone;
        this.mActive = isActive;
        this.mUni = uni;
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
