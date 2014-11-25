package org.voidsink.anewjkuapp.kusss;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Exam {

    private static final String TAG = Exam.class.getSimpleName();

    private static final Pattern lvaNrTermPattern = Pattern
            .compile(KusssHandler.PATTERN_LVA_NR_COMMA_TERM);
    private static final Pattern lvaNrPattern = Pattern
            .compile(KusssHandler.PATTERN_LVA_NR);
    private static final Pattern termPattern = Pattern
            .compile(KusssHandler.PATTERN_TERM);
    private static final Pattern timePattern = Pattern
            .compile("\\d{2}\\:\\d{2}");

    private String lvaNr = "";
    private String term = "";
    private Date date = null;
    private String time = "";
    private String location = "";
    private String description = "";
    private String info = "";
    private String title = "";
    private boolean isRegistered = false;

    public Exam(Context c, Element row, boolean isNewExam) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        Elements columns = row.getElementsByTag("td");
        if (isNewExam) {
            // allow only exams that can be selected
            if (columns.size() >= 5
                    && columns.get(0).select("input").size() == 1) {
                try {
                    Matcher lvaNrTermMatcher = lvaNrTermPattern.matcher(columns
                            .get(1).text()); // (lvaNr,term)
                    if (lvaNrTermMatcher.find()) {
                        String lvaNrTerm = lvaNrTermMatcher.group();
                        setTitle(columns.get(1).text()
                                .substring(0, lvaNrTermMatcher.start()));

                        Matcher lvaNrMatcher = lvaNrPattern.matcher(lvaNrTerm); // lvaNr
                        if (lvaNrMatcher.find()) {
                            setLvaNr(lvaNrMatcher.group());
                        }

                        Matcher termMatcher = termPattern.matcher(lvaNrTerm); // term
                        if (termMatcher.find(lvaNrMatcher.end())) {
                            setTerm(termMatcher.group());
                        }

                        setDate(dateFormat.parse(columns.get(2).text())); // date

                        setTimeLocation(c, columns.get(3).text());

                        setRegistered(false);
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Exam ctor", e);
                    Analytics.sendException(null, e, false);
                }
            }
        } else {
            if (columns.size() >= 5
                    && columns.get(4).select("input").size() == 1) {
                try {
                    Matcher lvaNrTermMatcher = lvaNrTermPattern.matcher(columns
                            .get(0).text()); // (lvaNr,term)
                    if (lvaNrTermMatcher.find()) {
                        String lvaNrTerm = lvaNrTermMatcher.group();
                        setTitle(columns.get(0).text()
                                .substring(0, lvaNrTermMatcher.start()));

                        Matcher lvaNrMatcher = lvaNrPattern.matcher(lvaNrTerm); // lvaNr
                        if (lvaNrMatcher.find()) {
                            setLvaNr(lvaNrMatcher.group());
                        }

                        Matcher termMatcher = termPattern.matcher(lvaNrTerm); // term
                        if (termMatcher.find(lvaNrMatcher.end())) {
                            setTerm(termMatcher.group());
                        }

                        setDate(dateFormat.parse(columns.get(1).text())); // date

                        setTimeLocation(c, columns.get(2).text());

                        setRegistered(columns.get(0)
                                .getElementsByClass("assignment-inactive")
                                .size() == 0);
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Exam ctor", e);
                    Analytics.sendException(c, e, false);
                }
            }
        }
    }

    private void setRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    private void setLvaNr(String lvaNr) {
        this.lvaNr = lvaNr;
    }

    private void setTerm(String term) {
        this.term = term;
    }

    private void setDate(Date date) {
        this.date = date;
    }

    private String extractTimeString(String time) {
        List<String> times = new ArrayList<>();
        // extract times
        Matcher timeMatcher = timePattern.matcher(time);
        while (timeMatcher.find()) {
            times.add(timeMatcher.group());
        }
        // remove duplicates
        int i = 0;
        while (i < times.size() - 1) {
            if (times.get(i).equals(times.get(i + 1))) {
                times.remove(i + 1);
            } else {
                i++;
            }
        }

        // create new time string
        String result = "";
        for (String s : times) {
            if (!result.isEmpty()) {
                result += " - ";
            }
            result += s;
        }

        return result;
    }

    private void setTimeLocation(Context c, String timeLocation) {
        String[] splitted = timeLocation.split("\\/");

        String time = "";
        String location = "";

        try {
            if (splitted.length > 1) {
                time = extractTimeString(splitted[0]);
                location = splitted[1];
            } else {
                time = extractTimeString(splitted[0]);
            }
        } catch (Exception e) {
            Log.e(TAG, "cant parse string", e);
            Analytics.sendException(c, e, false, timeLocation);
        }
        this.time = time;
        this.location = location;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    private void setInfo(String info) {
        this.info = info;
    }

    public boolean isInitialized() {
        return !this.lvaNr.isEmpty() && !this.term.isEmpty()
                && this.date != null;
    }

    public void addAdditionalInfo(Element row) {
        Elements columns = row.getElementsByTag("td");
        if (columns.size() == 1) {
            String text = columns.get(0).text().trim();
            if (!text.isEmpty()) {
                Elements info = columns.get(0).getElementsByAttributeValue(
                        "class", "info_icon");
                if (info.size() > 0) {
                    setInfo(text);
                } else {
                    setDescription(text);
                }
            }
        }
    }

    public String getTime() {
        return this.time;
    }

    public String getLocation() {
        return this.location;
    }

    public String getInfo() {
        return this.info;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLvaNr() {
        return this.lvaNr;
    }

    public String getTerm() {
        return this.term;
    }

    public Date getDate() {
        return this.date;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(KusssContentContract.Exam.EXAM_COL_DATE, getDate().getTime());
        cv.put(KusssContentContract.Exam.EXAM_COL_DESCRIPTION, getDescription());
        cv.put(KusssContentContract.Exam.EXAM_COL_INFO, getInfo());
        cv.put(KusssContentContract.Exam.EXAM_COL_LOCATION, getLocation());
        cv.put(KusssContentContract.Exam.EXAM_COL_LVANR, getLvaNr());
        cv.put(KusssContentContract.Exam.EXAM_COL_TERM, getTerm());
        cv.put(KusssContentContract.Exam.EXAM_COL_TIME, getTime());
        cv.put(KusssContentContract.Exam.EXAM_COL_IS_REGISTERED,
                KusssDatabaseHelper.toInt(isRegistered()));
        cv.put(KusssContentContract.Exam.EXAM_COL_TITLE, getTitle());
        return cv;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public String getTitle() {
        return title;
    }

    public String getKey() {
        return getKey(this.lvaNr, this.term, this.date.getTime());
    }

    public static String getKey(String lvaNr, String term, long date) {
        return String.format("%s-%s-%d", lvaNr, term, date);
    }
}
