package org.voidsink.anewjkuapp.kusss;

import android.content.Context;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.utils.Analytics;

import java.util.regex.Pattern;

public class Course {

    private static final Pattern courseIdPattern = Pattern
            .compile(KusssHandler.PATTERN_LVA_NR_WITH_DOT);

    private String term;
    private String courseId;
    private String title;
    private int cid;
    private String teacher;
    private double sws;
    private double ects;
    private String lvaType;
    private String code;

    public Course(String term, String courseId) {
        this.term = term;
        this.courseId = courseId;
    }

    public Course(Context c, String term, Element row) {
        this(term, "");

        Elements columns = row.getElementsByTag("td");

        if (columns.size() >= 11) {
            try {
                boolean active = columns.get(9)
                        .getElementsByClass("assignment-active").size() == 1;
                String courseIdText = columns.get(6).text();
                if (active && courseIdPattern.matcher(courseIdText).matches()) {
                    this.courseId = courseIdText.toUpperCase().replace(".", "");
                    setTitle(columns.get(5).text());
                    setLvaType(columns.get(4).text()); // type (UE, ...)
                    setTeacher(columns.get(7).text()); // Leiter
                    setCid(Integer.parseInt(columns.get(2).text())); // curricula id
                    setECTS(Double.parseDouble(columns.get(8).text()
                            .replace(",", "."))); // ECTS
                    // setSWS(Double.parseDouble(columns.get.child(6).text()
                    // .replace(",", "."))); // SWS
                    setCode(columns.get(3).text());
                }
            } catch (Exception e) {
                Analytics.sendException(c, e, true);
            }
        }
    }

    public Course(String term, String courseId, String title, int cid, String teacher, double sws, double ects, String type, String code) {
        this.term = term;
        this.courseId = courseId;
        this.title = title;
        this.cid = cid;
        this.teacher = teacher;
        this.ects = ects;
        this.sws = sws;
        this.lvaType = type;
        this.code = code;
    }

    private void setCode(String code) {
        this.code = code;
    }

    private void setCid(int cid) {
        this.cid = cid;
    }

    public int getCid() {
        return this.cid;
    }

    private void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getTeacher() {
        return this.teacher;
    }

    private void setSws(double sws) {
        this.sws = sws;
    }

    public double getSws() {
        return this.ects * 2 / 3;//this.sws;
    }

    private void setECTS(double ects) {
        this.ects = ects;
    }

    public double getEcts() {
        return this.ects;
    }

    private void setLvaType(String type) {
        this.lvaType = type;
    }

    public String getLvaType() {
        return this.lvaType;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public String getTerm() {
        return this.term;
    }

    public String getCourseId() {
        return this.courseId;
    }

    public boolean isInitialized() {
        return !term.isEmpty() && !courseId.isEmpty();
    }

    public String getCode() {
        return this.code;
    }

}