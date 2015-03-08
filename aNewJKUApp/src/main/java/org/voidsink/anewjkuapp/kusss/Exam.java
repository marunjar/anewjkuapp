package org.voidsink.anewjkuapp.kusss;

import android.content.Context;
import android.util.Log;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.utils.Analytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Exam {

    private static final String TAG = Exam.class.getSimpleName();

    private static final Pattern courseIdTermPattern = Pattern
            .compile(KusssHandler.PATTERN_LVA_NR_COMMA_TERM);
    private static final Pattern courseIdPattern = Pattern
            .compile(KusssHandler.PATTERN_LVA_NR);
    private static final Pattern termPattern = Pattern
            .compile(KusssHandler.PATTERN_TERM);
    private static final Pattern timePattern = Pattern
            .compile("\\d{2}\\:\\d{2}");

    private String courseId = "";
    private String term = "";
    private Date dtStart = null;
    private Date dtEnd = null;
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
                    Matcher courseIdTermMatcher = courseIdTermPattern.matcher(columns
                            .get(1).text()); // (courseId,term)
                    if (courseIdTermMatcher.find()) {
                        String courseIdTerm = courseIdTermMatcher.group();
                        setTitle(columns.get(1).text()
                                .substring(0, courseIdTermMatcher.start()));

                        Matcher courseIdMatcher = courseIdPattern.matcher(courseIdTerm); // courseId
                        if (courseIdMatcher.find()) {
                            setCourseId(courseIdMatcher.group());
                        }

                        Matcher termMatcher = termPattern.matcher(courseIdTerm); // term
                        if (termMatcher.find(courseIdMatcher.end())) {
                            setTerm(termMatcher.group());
                        }

                        initDates(dateFormat.parse(columns.get(2).text()));

                        initTimeLocation(c, columns.get(3).text());

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
                    Matcher courseIdTermMatcher = courseIdTermPattern.matcher(columns
                            .get(0).text()); // (courseId,term)
                    if (courseIdTermMatcher.find()) {
                        String courseIdTerm = courseIdTermMatcher.group();
                        setTitle(columns.get(0).text()
                                .substring(0, courseIdTermMatcher.start()));

                        Matcher courseIdMatcher = courseIdPattern.matcher(courseIdTerm); // courseId
                        if (courseIdMatcher.find()) {
                            setCourseId(courseIdMatcher.group());
                        }

                        Matcher termMatcher = termPattern.matcher(courseIdTerm); // term
                        if (termMatcher.find(courseIdMatcher.end())) {
                            setTerm(termMatcher.group());
                        }

                        initDates(dateFormat.parse(columns.get(1).text())); // date

                        initTimeLocation(c, columns.get(2).text());

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

    private void initDates(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        this.dtStart = cal.getTime();

        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.add(Calendar.SECOND, -1);

        this.dtEnd = cal.getTime();
    }

    public Exam(String courseId, String term, Date dtStart, Date dtEnd, String location, String description, String info, String title, boolean isRegistered) {
        this.courseId = courseId;
        this.term = term;
        this.dtStart = dtStart;
        this.dtEnd = dtEnd;
        this.location = location;
        this.description = description;
        this.info = info;
        this.title = title;
        this.isRegistered = isRegistered;
    }

    private void setRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    private void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    private void setTerm(String term) {
        this.term = term;
    }

    private void initDateTimes(String timeStr) {
        List<String> times = new ArrayList<>();
        // extract times
        Matcher timeMatcher = timePattern.matcher(timeStr);
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

        final SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm", Locale.GERMAN);
        // initialize dtStart, dtEnd
        if (times.size() == 1) {
            try {
                Date time = dfTime.parse(times.get(0));
                applyTime(dtStart, time);
                applyTime(dtEnd, time);
            } catch (ParseException e) {
                Analytics.sendException(null, e, false);
            }
        } else if (times.size() == 2) {
            try {
                Date timeStart = dfTime.parse(times.get(0));
                Date timeEnd = dfTime.parse(times.get(1));

                if (timeEnd.before(timeStart)) {
                    timeEnd = timeStart;
                }

                applyTime(dtStart, timeStart);
                applyTime(dtEnd, timeEnd);
            } catch (ParseException e) {
                Analytics.sendException(null, e, false);
            }
        }
    }

    private void applyTime(Date date, Date time) {
        Calendar calDate = Calendar.getInstance();
        calDate.setTime(date);

        Calendar calTime = Calendar.getInstance();
        calTime.setTime(time);
        calTime.set(Calendar.YEAR, calDate.get(Calendar.YEAR));
        calTime.set(Calendar.MONTH, calDate.get(Calendar.MONTH));
        calTime.set(Calendar.DAY_OF_YEAR, calDate.get(Calendar.DAY_OF_YEAR));

        date.setTime(calTime.getTimeInMillis());
    }

    private void initTimeLocation(Context c, String timeLocation) {
        String[] splitted = timeLocation.split("\\/");

        String location = "";

        try {
            if (splitted.length > 1) {
                initDateTimes(splitted[0]);
                location = splitted[1];
            } else {
                initDateTimes(splitted[0]);
            }
        } catch (Exception e) {
            Log.e(TAG, "cant parse string", e);
            Analytics.sendException(c, e, false, timeLocation);
        }
        this.location = location;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    private void setInfo(String info) {
        this.info = info;
    }

    public boolean isInitialized() {
        return !this.courseId.isEmpty() && !this.term.isEmpty()
                && this.dtStart != null && this.dtEnd != null;
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

    public Date getDtEnd() {
        return this.dtEnd;
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

    public String getCourseId() {
        return this.courseId;
    }

    public String getTerm() {
        return this.term;
    }

    public Date getDtStart() {
        return this.dtStart;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public String getTitle() {
        return title;
    }

    public String getKey() {
        return getKey(this.courseId, this.term, this.dtStart.getTime());
    }

    public static String getKey(String courseId, String term, long date) {
        return String.format("%s-%s-%d", courseId, term, date);
    }
}
