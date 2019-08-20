/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2019 Paul "Marunjar" Pretsch
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assessment {

    private static final Pattern courseIdTermPattern = Pattern
            .compile(KusssHandler.PATTERN_LVA_NR_COMMA_TERM);
    private static final Pattern courseIdPattern = Pattern
            .compile(KusssHandler.PATTERN_LVA_NR);
    private static final Pattern termPattern = Pattern
            .compile(KusssHandler.PATTERN_TERM);

    private int cid;
    private Grade grade;
    private Term term;
    private String courseId;
    private Date date;
    private final AssessmentType assessmentType;
    private String title;
    private String code;
    private double ects;
    private double sws;
    private String lvaType;

    public Assessment(AssessmentType type, Date date, String courseId, Term term,
                      Grade grade, int cid, String title, String code, double ects,
                      double sws, String lvaType) {
        this.assessmentType = type;
        this.date = date;
        this.courseId = courseId;
        this.term = term;
        this.grade = grade;
        this.cid = cid;
        this.title = title;
        this.code = code;
        this.ects = ects;
        this.sws = sws;
        this.lvaType = lvaType;
    }

    public Assessment(Context c, AssessmentType type, Element row) {
        this(type, null, "", null, null, 0, "", "", 0, 0, "");

        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        final Elements columns = row.getElementsByTag("td");
        if (columns.size() >= 7) {
            String title = columns.get(1).text();
            Matcher courseIdTermMatcher = courseIdTermPattern.matcher(title); // (courseId,term)
            if (courseIdTermMatcher.find()) {
                String courseIdTerm = courseIdTermMatcher.group();

                Matcher courseIdMatcher = courseIdPattern.matcher(courseIdTerm); // courseId
                if (courseIdMatcher.find()) {
                    setCourseId(courseIdMatcher.group());
                }

                Matcher termMatcher = termPattern.matcher(courseIdTerm); // term
                if (termMatcher.find(courseIdMatcher.end())) {
                    try {
                        setTerm(Term.parseTerm(termMatcher.group()));
                    } catch (ParseException e) {
                        Analytics.sendException(c, e, true);
                    }
                }

                String tmp = title.substring(0, courseIdTermMatcher.start());
                if (courseIdTermMatcher.end() <= title.length()) {
                    String addition = title
                            .substring(courseIdTermMatcher.end())
                            .replaceAll("(\\(.*?\\))", "").trim();
                    if (addition.length() > 0) {
                        tmp = tmp + " (" + addition + ")";
                    }
                }
                title = tmp;
            }

            title = title.trim(); // title + lvaType
            setTitle(title); // title

            setLvaType(columns.get(4).text().trim()); // lvaType

            try {
                setDate(dateFormat.parse(columns.get(0).text())); // date
            } catch (ParseException e) {
                Analytics.sendException(c, e, false, columns.get(0).text());
            }

            if (term == null && date != null) {
                term = Term.fromDate(date);
            }

            setGrade(Grade.parseGrade(columns.get(2).text())); // grade

            try {
                String[] ectsSws = columns.get(5).text().replace(",", ".")
                        .split("/", -1);
                if (ectsSws.length == 2) {
                    setEcts(Double.parseDouble(ectsSws[0]));
                    setSws(Double.parseDouble(ectsSws[1]));
                }
            } catch (Exception e) {
                Analytics.sendException(c, e, false, columns.get(5).text());
            }

            try {
                String cidText = columns.get(6).text();
                if (!TextUtils.isEmpty(cidText)) {
                    setCid(Integer.parseInt(cidText)); // grade
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

    private void setCid(int cid) {
        this.cid = cid;
    }

    private void setGrade(Grade grade) {
        this.grade = grade;
    }

    private void setTerm(Term term) {
        this.term = term;
    }

    private void setCourseId(String courseId) {
        this.courseId = courseId;
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

    public String getCourseId() {
        return this.courseId;
    }

    public Term getTerm() {
        return this.term;
    }

    public Grade getGrade() {
        return this.grade;
    }

    public int getCid() {
        return this.cid;
    }

    public AssessmentType getAssessmentType() {
        return this.assessmentType;
    }

    public boolean isInitialized() {
        return this.assessmentType != null && this.date != null
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

    public void setLvaType(String lvaType) {
        this.lvaType = lvaType;
    }

    public String getLvaType() {
        return lvaType;
    }
}
