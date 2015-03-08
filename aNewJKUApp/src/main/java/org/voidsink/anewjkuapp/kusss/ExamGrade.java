package org.voidsink.anewjkuapp.kusss;

import android.content.Context;
import android.text.TextUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.utils.Analytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExamGrade {

    private static final Pattern lvaNrTermPattern = Pattern
            .compile(KusssHandler.PATTERN_LVA_NR_COMMA_TERM);
    private static final Pattern lvaNrPattern = Pattern
            .compile(KusssHandler.PATTERN_LVA_NR);
    private static final Pattern termPattern = Pattern
            .compile(KusssHandler.PATTERN_TERM);

    private int skz;
    private Grade grade;
    private String term;
    private String lvaNr;
    private Date date;
    private final GradeType gradeType;
    private String title;
    private String code;
    private double ects;
    private double sws;

    public ExamGrade(GradeType type, Date date, String lvaNr, String term,
                     Grade grade, int skz, String title, String code, double ects,
                     double sws) {
        this.gradeType = type;
        this.date = date;
        this.lvaNr = lvaNr;
        this.term = term;
        this.grade = grade;
        this.skz = skz;
        this.title = title;
        this.code = code;
        this.ects = ects;
        this.sws = sws;
    }

    public ExamGrade(Context c, GradeType type, Element row) {
        this(type, null, "", "", null, 0, "", "", 0, 0);

        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        final Elements columns = row.getElementsByTag("td");
        if (columns.size() >= 7) {
            String title = columns.get(1).text();
            Matcher lvaNrTermMatcher = lvaNrTermPattern.matcher(title); // (lvaNr,term)
            if (lvaNrTermMatcher.find()) {
                String lvaNrTerm = lvaNrTermMatcher.group();

                Matcher lvaNrMatcher = lvaNrPattern.matcher(lvaNrTerm); // lvaNr
                if (lvaNrMatcher.find()) {
                    setLvaNr(lvaNrMatcher.group());
                }

                Matcher termMatcher = termPattern.matcher(lvaNrTerm); // term
                if (termMatcher.find(lvaNrMatcher.end())) {
                    setTerm(termMatcher.group());
                }

                String tmp = title.substring(0, lvaNrTermMatcher.start());
                if (lvaNrTermMatcher.end() <= title.length()) {
                    String addition = title
                            .substring(lvaNrTermMatcher.end(),
                                    title.length())
                            .replaceAll("(\\(.*?\\))", "").trim();
                    if (addition.length() > 0) {
                        tmp = tmp + " (" + addition + ")";
                    }
                }
                title = tmp;
            }

            title = title.trim() + " " + columns.get(4).text().trim(); // title + lvaType

            setTitle(title); // title

            try {
                setDate(dateFormat.parse(columns.get(0).text())); // date
            } catch (ParseException e) {
                Analytics.sendException(c, e, false, columns.get(0).text());
            }

            setGrade(Grade.parseGrade(columns.get(2).text())); // grade

            try {
                String[] ectsSws = columns.get(5).text().replace(",", ".")
                        .split("/");
                if (ectsSws.length == 2) {
                    setEcts(Double.parseDouble(ectsSws[0]));
                    setSws(Double.parseDouble(ectsSws[1]));
                }
            } catch (Exception e) {
                Analytics.sendException(c, e, false, columns.get(5).text());
            }

            try {
                String skzText = columns.get(6).text();
                if (!TextUtils.isEmpty(skzText)) {
                    setSKZ(Integer.parseInt(skzText)); // grade
                }
            } catch (NumberFormatException e) {
                Analytics.sendException(c, e, false, columns.get(6).text());
            }
            setCode(columns.get(3).text());
        }
    }

    private void setSws(double sws) {
        this.sws = sws;
    }

    private void setEcts(double ects) {
        this.ects = ects;
    }

    private void setCode(String code) {
        this.code = code;
    }

    private void setTitle(String title) {
        this.title = title.trim();
    }

    private void setSKZ(int skz) {
        this.skz = skz;
    }

    private void setGrade(Grade grade) {
        this.grade = grade;
    }

    private void setTerm(String term) {
        this.term = term;
    }

    private void setLvaNr(String lvaNr) {
        this.lvaNr = lvaNr;
    }

    private void setDate(Date date) {
        this.date = date;
    }

    public String getCode() {
        return this.code;
    }

    public Date getDate() {
        return this.date;
    }

    public String getLvaNr() {
        return this.lvaNr;
    }

    public String getTerm() {
        return this.term;
    }

    public Grade getGrade() {
        return this.grade;
    }

    public int getSkz() {
        return this.skz;
    }

    public GradeType getGradeType() {
        return this.gradeType;
    }

    public boolean isInitialized() {
        return this.gradeType != null && this.date != null
                && this.grade != null;
    }

    public String getTitle() {
        return this.title;
    }

    public double getEcts() {
        return this.ects;
    }

    public double getSws() {
        return this.sws;
    }

}
