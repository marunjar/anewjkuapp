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

package org.voidsink.anewjkuapp.kusss;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.jsoup.Connection.Method.GET;
import static org.jsoup.Connection.Method.POST;

public class KusssHandler {

    public static final String PATTERN_LVA_NR_WITH_DOT = "\\d{3}\\.\\w{3}";
    public static final String PATTERN_LVA_NR = "\\d{3}\\w{3}";
    public static final String PATTERN_TERM = "\\d{4}[swSW]";
    public static final String PATTERN_LVA_NR_COMMA_TERM = "\\("
            + PATTERN_LVA_NR + "," + PATTERN_TERM + "\\)";
    public static final String PATTERN_LVA_NR_SLASH_TERM = "\\("
            + PATTERN_LVA_NR + "\\/" + PATTERN_TERM + "\\)";
    private static final String URL_KUSSS_INDEX = "https://www.kusss.jku.at/kusss/index.action";
    private static final String URL_MY_LVAS = "https://www.kusss.jku.at/kusss/assignment-results.action";
    private static final String URL_GET_TERMS = "https://www.kusss.jku.at/kusss/listmystudentlvas.action";
    private static final String URL_GET_ICAL = "https://www.kusss.jku.at/kusss/ical-multi-sz.action";
    private static final String URL_GET_ICAL_FORM = "https://www.kusss.jku.at/kusss/ical-multi-form-sz.action";
    private static final String URL_MY_GRADES = "https://www.kusss.jku.at/kusss/gradeinfo.action";
    private static final String URL_LOGOUT = "https://www.kusss.jku.at/kusss/logout.action";
    private static final String URL_LOGIN = "https://www.kusss.jku.at/kusss/login.action";
    private static final String URL_GET_NEW_EXAMS = "https://www.kusss.jku.at/kusss/szsearchexam.action";
    private static final String URL_GET_EXAMS = "https://www.kusss.jku.at/kusss/szexaminationlist.action";
    private static final String URL_SELECT_TERM = "https://www.kusss.jku.at/kusss/select-term.action";
    private static final String SELECT_MY_LVAS = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > table > tbody > tr:has(td)";
    private static final String SELECT_MY_GRADES = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > *";
    private static final String SELECT_LOGOUT = "body > table > tbody > tr > td > div > ul > li > a[href*=logout.action]";
    // private static final String SELECT_ACTUAL_EXAMS =
    // "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > div.tabcontainer > div.tabcontent > table > tbody > tr > td > form > table > tbody > tr:has(td)";
    private static final String SELECT_NEW_EXAMS = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > div.tabcontainer > div.tabcontent > div.sidetable > form > table > tbody > tr:has(td)";
    private static final String SELECT_EXAMS = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > div.tabcontainer > div.tabcontent > table > tbody > tr > td > form > table > tbody > tr:has(td)";
    private static final String URL_MY_STUDIES = "https://www.kusss.jku.at/kusss/studentsettings.action?set.studentsettings.tabbedPanel.selectedTab=studies";
    private static final String SELECT_MY_STUDIES = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > div.tabcontainer > div.tabcontent > form > table > tbody > tr[class]:has(td)";

    private static final int TIMEOUT_LOGIN = 15 * 1000; // 15s
    private static final int TIMEOUT_SEARCH_EXAM_BY_LVA = 15 * 1000; //15s
    private static final int TIMEOUT_CALENDAR_READ = 15 * 1000; // 15s

    private volatile static KusssHandler handler = null;
    private final CookieManager mCookies;
    private String mUserAgent;

    private static final Logger logger = LoggerFactory.getLogger(KusssHandler.class);

    private KusssHandler() {
        this.mCookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(mCookies);

        String userAgent = null;
        try {
            userAgent = System.getProperty("http.agent");
        } catch (Exception ignored) {
        }
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = "Mozilla/5.0";
        }
        setUserAgent(userAgent);
    }

    private static boolean isNetworkAvailable(Context context) {
        return AppUtils.isNetworkAvailable(context, false);
    }

    public static synchronized KusssHandler getInstance() {
        if (handler == null) {
            synchronized (KusssHandler.class) {
                if (handler == null) handler = new KusssHandler();
            }
        }
        return handler;
    }

    private String getSessionIDFromCookie() {
        try {
            List<HttpCookie> cookies = mCookies.getCookieStore().get(
                    new URI("https://www.kusss.jku.at/"));

            for (HttpCookie cookie : cookies) {
                if (cookie.getName().equals("JSESSIONID")) {
                    return cookie.getValue();
                }
            }
            return null;
        } catch (URISyntaxException e) {
            logger.error("getSessionIDFromCookie", e);
            return null;
        }
    }

    /**
     * workaround for jsoup parsing error, see
     * https://github.com/marunjar/anewjkuapp/issues/139
     * https://github.com/jhy/jsoup/issues/1218
     *
     * @param response response of a Jsoup connetion.execute()
     * @return Document the parsed html
     */
    private Document parseWorkaround(Connection.Response response) {
        String body = response.body();
        return Jsoup.parse(body);
    }

    /**
     * workaround for jsoup parsing error, see
     * https://github.com/marunjar/anewjkuapp/issues/139
     * https://github.com/jhy/jsoup/issues/1218
     *
     * @param connection a Jsoup connection
     * @return Document the parsed html
     * @throws IOException of connection.execute()
     */
    private Document getWorkaround(Connection connection) throws IOException {
        Connection.Response response = connection.method(GET).execute();
        return parseWorkaround(response);
    }

    /**
     * workaround for jsoup parsing error, see
     * https://github.com/marunjar/anewjkuapp/issues/139
     * https://github.com/jhy/jsoup/issues/1218
     *
     * @param connection a Jsoup connection
     * @return Document the parsed html
     * @throws IOException of connection.execute()
     */
    private Document postWorkaround(Connection connection) throws IOException {
        Connection.Response response = connection.method(POST).execute();
        return parseWorkaround(response);
    }

    public synchronized String login(Context c, String user, String password) {
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(password)) {
            return null;
        }
        if (!isNetworkAvailable(c)) {
            return null;
        }

        try {

            if ((user.length() > 0) && (user.charAt(0) != 'k')) {
                user = "k" + user;
            }

            mCookies.getCookieStore().removeAll();

            getWorkaround(Jsoup.connect(URL_KUSSS_INDEX).userAgent(getUserAgent()).timeout(TIMEOUT_LOGIN).followRedirects(true));

            Connection.Response r = Jsoup.connect(URL_LOGIN).userAgent(getUserAgent()).cookies(getCookieMap()).data("j_username", user).data("j_password", password).timeout(TIMEOUT_LOGIN).followRedirects(true).method(POST).execute();

            if (r.url() != null) {
                r = Jsoup.connect(r.url().toString()).userAgent(getUserAgent()).cookies(getCookieMap()).method(GET).execute();
            }

            Document doc = parseWorkaround(r);

            String sessionId = getSessionIDFromCookie();
            if (isLoggedIn(c, doc)) {
                return sessionId;
            }

            if (isLoggedIn(c, sessionId)) {
                return sessionId;
            }

            logger.warn("login failed: isLoggedIn=FALSE");
            return null;
        } catch (SocketTimeoutException e) {
            // bad connection, timeout
            logger.warn("login failed: connection timeout", e);
            return null;
        } catch (Exception e) {
            AnalyticsHelper.sendException(c, e, true);
            return null;
        }
    }

    private Map<String, String> getCookieMap() {
        Map<String, String> cookies = new HashMap<>();
        for (HttpCookie cookie : mCookies.getCookieStore().getCookies()) {
            cookies.put(cookie.getName(), cookie.getValue());
        }
        return cookies;
    }

    public synchronized void logout(Context c) {
        try {
            if (isNetworkAvailable(c)) {
                Jsoup.connect(URL_LOGOUT).userAgent(getUserAgent()).cookies(getCookieMap()).method(GET).execute();
            }
        } catch (Exception e) {
            logger.warn("logout failed", e);
            AnalyticsHelper.sendException(c, e, true);
        }
        mCookies.getCookieStore().removeAll();
    }

    public synchronized boolean isLoggedIn(Context c, String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return false;
        }
        if (!isNetworkAvailable(c)) {
            return false;
        }
        try {
            Document doc = getWorkaround(Jsoup.connect(URL_KUSSS_INDEX).userAgent(getUserAgent()).cookies(getCookieMap()).timeout(TIMEOUT_LOGIN).followRedirects(true));

            return isLoggedIn(c, doc);
        } catch (SocketTimeoutException e) {
            // bad connection, timeout
            return false;
        } catch (IOException e) {
            AnalyticsHelper.sendException(c, e, true);
            return false;
        }
    }

    private boolean isLoggedIn(Context c, Document doc) {
        if (!isNetworkAvailable(c)) {
            return false;
        }

        Elements logoutAction = doc.select(SELECT_LOGOUT);

        return (logoutAction.size() > 0);
    }

    public synchronized boolean isAvailable(Context c, String sessionId,
                                            String user, String password) {
        return isNetworkAvailable(c) && (isLoggedIn(c, sessionId) || login(c, user, password) != null);
    }

    private String getUidPrefix(String calendarName) {
        switch (calendarName) {
            case CalendarUtils.ARG_CALENDAR_EXAM:
                return "at-jku-kusss-exam-";
            case CalendarUtils.ARG_CALENDAR_COURSE:
                return "at-jku-kusss-coursedate-";
            default:
                return null;
        }
    }

    public List<KusssCalendar> getIcal(Context c, CalendarBuilder calendarBuilder, String calendarName, Date date, boolean loadAll) {
        List<KusssCalendar> calendars = new ArrayList<>();

        Term currentTerm = Term.fromDate(date);

        if (loadAll) {
            List<Term> terms = getTerms(c);
            for (Term term : terms) {
                calendars.add(new KusssCalendar(CalendarUtils.getCalendarName(c, calendarName), term, getUidPrefix(calendarName), term.equals(currentTerm), loadIcal(c, calendarBuilder, calendarName, term)));
            }
        } else {
            calendars.add(new KusssCalendar(CalendarUtils.getCalendarName(c, calendarName), currentTerm, getUidPrefix(calendarName), true, loadIcal(c, calendarBuilder, calendarName, currentTerm)));

            Term nextTerm = Term.fromDate(new Date(date.getTime() + (4 * DateUtils.WEEK_IN_MILLIS)));
            if (!nextTerm.equals(currentTerm)) {
                calendars.add(new KusssCalendar(CalendarUtils.getCalendarName(c, calendarName), nextTerm, getUidPrefix(calendarName), false, loadIcal(c, calendarBuilder, calendarName, nextTerm)));
            }
        }

        return calendars;
    }

    private Calendar loadIcal(Context c, CalendarBuilder calendarBuilder, String calendarName, Term term) {
        if (calendarName == null) {
            return null;
        }
        if (!isLoggedIn(c, getSessionIDFromCookie())) {
            return null;
        }

        try {
            if (!selectTerm(c, term)) {
                return null;
            }
            Document doc = getWorkaround(Jsoup.connect(URL_GET_ICAL_FORM).userAgent(getUserAgent()).cookies(getCookieMap()).timeout(TIMEOUT_LOGIN).followRedirects(true));
            if (!isSelectable(c, doc, term)) {
                return null;
            }
            if (!isSelected(c, doc, term)) {
                return null;
            }
        } catch (IOException e) {
            AnalyticsHelper.sendException(c, e, true, "loadIcal: selectTerm");
            return null;
        }

        return loadIcalJsoup(c, calendarBuilder, calendarName);
    }

    @SuppressWarnings("unused")
    private Calendar loadIcalFromFile(Context c, CalendarBuilder calendarBuilder, String calendarname) {
        Calendar iCal;

        try (InputStream assetData = new BufferedInputStream(c.getAssets().open("kusss.ics"))) {
            iCal = calendarBuilder.build(assetData);
        } catch (IOException e) {
            e.printStackTrace();
            iCal = null;
        } catch (ParserException e) {
            e.printStackTrace();
            iCal = null;
        }
        return iCal;
    }

    private Calendar loadIcalJsoup(Context c, CalendarBuilder calendarBuilder, String calendarName) {
        Connection connection = Jsoup.connect(URL_GET_ICAL)
                .userAgent(getUserAgent())
                .cookies(getCookieMap())
                .timeout(TIMEOUT_CALENDAR_READ)
                .method(POST);
        switch (calendarName) {
            case CalendarUtils.ARG_CALENDAR_EXAM:
                connection.data("selectAll", "ical.category.examregs");
                break;
            case CalendarUtils.ARG_CALENDAR_COURSE:
                connection.data("selectAll", "ical.category.mycourses");
                break;
            default: {
                return null;
            }
        }

        Calendar iCal;
        String body = null;
        String contentType = null;
        try {
            Connection.Response response = connection.execute();

            contentType = response.contentType();
            if (!contentType.contains("text/calendar")) {
                throw new UnsupportedOperationException("wrong content type");
            }

            body = response.body();
            if (!TextUtils.isEmpty(body)) {
                iCal = calendarBuilder.build(new ByteArrayInputStream(body.getBytes(response.charset() != null ? response.charset() : Charset.defaultCharset().name())));
            } else {
                iCal = new Calendar();
            }
        } catch (ParserException | IOException e) {
            AnalyticsHelper.sendException(c, e, true, "loadIcalJsoup", contentType, body);
            iCal = null;
        }
        return iCal;
    }

    private List<Term> getTerms(Context c) {
        List<Term> terms = new ArrayList<>();

        Map<String, String> termMap = getTermMap(c);
        if (termMap != null) {
            for (String termValue : termMap.values()) {
                try {
                    Term term = Term.parseTerm(termValue);
                    terms.add(term);
                } catch (ParseException e) {
                    AnalyticsHelper.sendException(c, e, true);
                }
            }

            Collections.sort(terms);
        }
        return terms;
    }

    private Map<String, String> getTermMap(Context c) {
        if (!isLoggedIn(c, getSessionIDFromCookie())) {
            return null;
        }

        Map<String, String> terms = new HashMap<>();
        try {
            Document doc = getWorkaround(Jsoup.connect(URL_GET_TERMS).userAgent(getUserAgent()).cookies(getCookieMap()));
            Element termDropdown = doc.getElementById("term");
            if (termDropdown != null) {
                Elements termDropdownEntries = termDropdown
                        .getElementsByClass("dropdownentry");

                for (Element termDropdownEntry : termDropdownEntries) {
                    terms.put(termDropdownEntry.attr("value"),
                            termDropdownEntry.text());
                }
            }
        } catch (Exception e) {
            AnalyticsHelper.sendException(c, e, true);
            return null;
        }
        return terms;
    }

    private boolean selectTerm(Context c, Term term) throws IOException {
        if (!isLoggedIn(c, getSessionIDFromCookie())) {
            return false;
        }
        postWorkaround(
                Jsoup.connect(URL_SELECT_TERM)
                        .userAgent(getUserAgent())
                        .cookies(getCookieMap())
                        .data("term", term.toString())
                        .data("previousQueryString", "")
                        .data("reloadAction", "coursecatalogue-start.action")
        );

        //TODO: check document for successful selection of term
//            if (!isSelected(doc, term)) {
//                throw new IOException(String.format("selection of term failed: %s", term));
//            }
        return true;
    }

    public List<Course> getLvas(Context c, List<Term> terms) {
        if (terms == null || terms.size() == 0) {
            return null;
        }
        if (!isLoggedIn(c, getSessionIDFromCookie())) {
            return null;
        }

        ArrayList<Course> courses = new ArrayList<>();
        try {
            logger.debug("getCourses");

            for (Term term : terms) {
                term.setLoaded(false); // init loaded flag
                if (selectTerm(c, term)) {
                    Document doc = getWorkaround(Jsoup.connect(URL_MY_LVAS).userAgent(getUserAgent()).cookies(getCookieMap()));

                    if (isSelectable(c, doc, term)) {
                        if (isSelected(c, doc, term)) {
                            // .select("body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > table > tbody > tr");
                            Elements rows = doc.select(SELECT_MY_LVAS);
                            for (Element row : rows) {
                                Course course = new Course(c, term, row);
                                if (course.isInitialized()) {
                                    courses.add(course);
                                }
                            }
                            term.setLoaded(true);
                        } else {
                            throw new IOException(String.format("term not selected: %s", term));
                        }
                    }
                } else {
                    // break if selection failed
                    return null;
                }
            }
            if (courses.size() == 0) {
                // break if no lvas found, a student without courses is a quite impossible case
                return null;
            }
        } catch (Exception e) {
            AnalyticsHelper.sendException(c, e, true);
            return null;
        }
        return courses;
    }

    private boolean isSelectable(Context c, Document doc, Term term) {
        try {
            Element termSelector = doc.getElementById("term");
            if (termSelector == null) return false;

            Elements selectable = termSelector.getElementsByAttributeValue("value", term.toString());
            return selectable.size() == 1;
        } catch (Exception e) {
            AnalyticsHelper.sendException(c, e, true);
            return false;
        }
    }

    private boolean isSelected(Context c, Document doc, Term term) {
        try {
            Elements terms = doc.getElementById("term").getElementsByAttribute(
                    "selected");

            for (Element termEntry : terms) {
                if (termEntry.attr("value").equalsIgnoreCase(term.toString())) {
                    return true;
                }
            }
        } catch (Exception e) {
            AnalyticsHelper.sendException(c, e, true);
            return false;
        }
        return false;
    }

    public List<Assessment> getAssessments(Context c) {
        if (!isNetworkAvailable(c)) {
            return null;
        }
        List<Assessment> grades = new ArrayList<>();
        try {
            Document doc = getWorkaround(Jsoup.connect(URL_MY_GRADES).userAgent(getUserAgent()).cookies(getCookieMap()).data("months", "0"));

            if (isLoggedIn(c, doc)) {
                Elements rows = doc.select(SELECT_MY_GRADES);

                AssessmentType type = null;
                for (Element row : rows) {
                    if (row.tag().toString().equals("h3")) {
                        type = AssessmentType.parseGradeType(row.text());
                    } else if (row.tag().toString().equals("table")) {
                        Elements gradeRows = row
                                .select("tbody > tr[class]:has(td)");
                        for (Element gradeRow : gradeRows) {
                            Assessment grade = new Assessment(c, type, gradeRow);
                            if (grade.isInitialized()) {
                                grades.add(grade);
                            }
                        }
                    }
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            AnalyticsHelper.sendException(c, e, true);
            return null;
        }
        logger.debug("{} grades found", grades.size());
        return grades;
    }

    public List<Exam> getNewExams(Context c) {
        if (!isNetworkAvailable(c)) {
            return null;
        }
        List<Exam> exams = new ArrayList<>();
        try {
            Document doc = getWorkaround(
                    Jsoup.connect(URL_GET_NEW_EXAMS)
                            .userAgent(getUserAgent())
                            .cookies(getCookieMap())
                            .data("search", "true").data("searchType", "mylvas"));
            if (isLoggedIn(c, doc)) {
                Elements rows = doc.select(SELECT_NEW_EXAMS);

                int i = 0;
                while (i < rows.size()) {
                    Element row = rows.get(i);
                    Exam exam = new Exam(c, row, true);
                    i++;

                    if (exam.isInitialized()) {
                        while (i < rows.size()
                                && rows.get(i).attr("class")
                                .equals(row.attr("class"))) {
                            exam.addAdditionalInfo(rows.get(i));
                            i++;
                        }
                        exams.add(exam);
                    }
                }

                // add registered exams
                loadExams(c, exams);
            } else {
                exams = null;
            }
        } catch (Exception e) {
            AnalyticsHelper.sendException(c, e, true);
            return null;
        }
        return exams;
    }

    public List<Exam> getNewExamsByCourseId(Context c, List<Course> courses, List<Term> terms) {
        if (!isLoggedIn(c, getSessionIDFromCookie())) {
            return null;
        }

        List<Exam> exams = new ArrayList<>();
        try {
            if (courses == null || courses.size() == 0) {
                logger.debug("no lvas found, reload");
                courses = getLvas(c, terms);
            }
            if (courses != null && courses.size() > 0) {
                Map<String, Assessment> gradeCache = new HashMap<>();

                List<Assessment> grades = getAssessments(c);
                if (grades != null) {
                    for (Assessment grade : grades) {
                        if (!grade.getCourseId().isEmpty()) {
                            Assessment existing = gradeCache.get(grade.getCourseId());
                            if (existing != null) {
                                logger.debug("{} --> {}", existing.getTitle(), grade.getTitle());
                            }
                            gradeCache.put(grade.getCourseId(), grade);
                        }
                    }
                }

                for (Course course : courses) {
                    Assessment grade = gradeCache.get(course.getCourseId());
                    if (grade != null &&
                            ((grade.getGrade() == Grade.G5) ||
                                    (grade.getDate().getTime() > (System.currentTimeMillis() - (182 * DateUtils.DAY_IN_MILLIS))))) {
                        logger.debug("positive in last 6 Months: {}", grade.getTitle());
                        grade = null;
                    }
                    if (grade == null) {
                        List<Exam> newExams = getNewExamsByCourseId(c,
                                course.getCourseId());
                        if (newExams != null) {
                            for (Exam newExam : newExams) {
                                if (newExam != null) {
                                    exams.add(newExam);
                                }
                            }
                        }
                    }
                }
            }

            // add registered exams
            loadExams(c, exams);
        } catch (Exception e) {
            AnalyticsHelper.sendException(c, e, true);
            return null;
        }
        return exams;
    }

    private List<Exam> getNewExamsByCourseId(Context c, String courseId) {
        if (!isNetworkAvailable(c)) {
            return null;
        }

        List<Exam> exams = new ArrayList<>();
        try {
            final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

            logger.debug("getNewExamsByCourseId: {}", courseId);
            Document doc = postWorkaround(Jsoup
                    .connect(URL_GET_NEW_EXAMS)
                    .userAgent(getUserAgent())
                    .cookies(getCookieMap())
                    .timeout(TIMEOUT_SEARCH_EXAM_BY_LVA)
                    .data("search", "true")
                    .data("searchType", "specific")
                    .data("searchDateFrom",
                            df.format(new Date(System.currentTimeMillis())))
                    .data("searchDateTo",
                            df.format(new Date(System.currentTimeMillis()
                                    + DateUtils.YEAR_IN_MILLIS)))
                    .data("searchLvaNr", courseId).data("searchLvaTitle", "")
                    .data("searchCourseClass", "")
            );

            if (isLoggedIn(c, doc)) {
                Elements rows = doc.select(SELECT_NEW_EXAMS);

                int i = 0;
                while (i < rows.size()) {
                    Element row = rows.get(i);
                    Exam exam = new Exam(c, row, true);
                    i++;

                    if (exam.isInitialized()) {
                        while (i < rows.size()
                                && rows.get(i).attr("class")
                                .equals(row.attr("class"))) {
                            exam.addAdditionalInfo(rows.get(i));
                            i++;
                        }
                        exams.add(exam);
                    }
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            AnalyticsHelper.sendException(c, e, true);
            exams = null;
        }
        return exams;
    }

    private void loadExams(Context c, List<Exam> exams) throws IOException {
        if (!isNetworkAvailable(c)) {
            return;
        }

        logger.debug("loadExams");

        Document doc = getWorkaround(Jsoup.connect(URL_GET_EXAMS).userAgent(getUserAgent()).cookies(getCookieMap()));

        if (isLoggedIn(c, doc)) {
            Elements rows = doc.select(SELECT_EXAMS);

            int i = 0;
            while (i < rows.size()) {
                Element row = rows.get(i);
                Exam exam = new Exam(c, row, false);
                i++;

                if (exam.isInitialized()) {
                    while (i < rows.size()
                            && rows.get(i).attr("class").equals(row.attr("class"))) {
                        exam.addAdditionalInfo(rows.get(i));
                        i++;
                    }
                    exams.add(exam);
                }
            }
        }
    }

    public List<Curriculum> getCurricula(Context c) {
        if (!isNetworkAvailable(c)) {
            return null;
        }
        try {
            List<Curriculum> mCurricula = new ArrayList<>();

            Document doc = getWorkaround(Jsoup.connect(URL_MY_STUDIES).userAgent(getUserAgent()).cookies(getCookieMap()));

            if (isLoggedIn(c, doc)) {
                Elements rows = doc.select(SELECT_MY_STUDIES);
                for (Element row : rows) {
                    Curriculum s = new Curriculum(c, row);
                    if (s.isInitialized()) {
                        mCurricula.add(s);
                    }
                }
                return mCurricula;
            } else {
                return null;
            }
        } catch (Exception e) {
            AnalyticsHelper.sendException(c, e, true);
            return null;
        }
    }

    private String getUserAgent() {
        return this.mUserAgent;
    }

    public void setUserAgent(String userAgent) {
        if (!TextUtils.isEmpty(userAgent)) {
            this.mUserAgent = userAgent;
        }
    }
}
