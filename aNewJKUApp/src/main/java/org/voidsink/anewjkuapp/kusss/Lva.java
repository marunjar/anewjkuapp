package org.voidsink.anewjkuapp.kusss;

import android.content.Context;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.utils.Analytics;

import java.util.regex.Pattern;

public class Lva {

    private static final Pattern lvaNrPattern = Pattern
            .compile(KusssHandler.PATTERN_LVA_NR_WITH_DOT);

    private String term;
    private String lvaNr;
    private String title;
    private int skz;
    private String teacher;
    private double sws;
    private double ects;
    private String lvaType;
    private String code;

    public Lva(String term, String lvaNr) {
        this.term = term;
        this.lvaNr = lvaNr;
    }

    public Lva(Context c, String term, Element row) {
        this(term, "");

        Elements columns = row.getElementsByTag("td");

        if (columns.size() >= 11) {
            try {
                boolean active = columns.get(9)
                        .getElementsByClass("assignment-active").size() == 1;
                String lvaNrText = columns.get(6).text();
                if (active && lvaNrPattern.matcher(lvaNrText).matches()) {
                    this.lvaNr = lvaNrText.toUpperCase().replace(".", "");
                    setTitle(columns.get(5).text());
                    setLvaType(columns.get(4).text()); // type (UE, ...)
                    setTeacher(columns.get(7).text()); // Leiter
                    setSKZ(Integer.parseInt(columns.get(2).text())); // SKZ
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

    public Lva(String term, String lvaNr, String title, int skz, String teacher, double sws, double ects, String type, String code) {
        this.term = term;
        this.lvaNr = lvaNr;
        this.title = title;
        this.skz = skz;
        this.teacher = teacher;
        this.ects = ects;
        this.sws = sws;
        this.lvaType = type;
        this.code = code;
    }

    private void setCode(String code) {
        this.code = code;
    }

    private void setSKZ(int skz) {
        this.skz = skz;
    }

    public int getSKZ() {
        return this.skz;
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

    public String getLvaNr() {
        return this.lvaNr;
    }

    public boolean isInitialized() {
        return !term.isEmpty() && !lvaNr.isEmpty();
    }

    public String getCode() {
        return this.code;
    }

}