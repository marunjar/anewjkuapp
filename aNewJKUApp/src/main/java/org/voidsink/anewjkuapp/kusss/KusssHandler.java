package org.voidsink.anewjkuapp.kusss;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.utils.Analytics;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KusssHandler {

    public static final String PATTERN_LVA_NR_WITH_DOT = "\\d{3}\\.\\w{3}";
    public static final String PATTERN_LVA_NR = "\\d{3}\\w{3}";
    public static final String PATTERN_TERM = "\\d{4}[swSW]";
    public static final String PATTERN_LVA_NR_COMMA_TERM = "\\("
            + PATTERN_LVA_NR + "," + PATTERN_TERM + "\\)";
    public static final String PATTERN_LVA_NR_SLASH_TERM = "\\("
            + PATTERN_LVA_NR + "\\/" + PATTERN_TERM + "\\)";
    private static final String TAG = KusssHandler.class.getSimpleName();
    private static final String URL_KUSSS_INDEX = "https://www.kusss.jku.at/kusss/index.action";
    private static final String URL_MY_LVAS = "https://www.kusss.jku.at/kusss/assignment-results.action";
    private static final String URL_GET_TERMS = "https://www.kusss.jku.at/kusss/listmystudentlvas.action";
    private static final String URL_GET_ICAL = "https://www.kusss.jku.at/kusss/ical-multi-sz.action";
    private static final String URL_MY_GRADES = "https://www.kusss.jku.at/kusss/gradeinfo.action";
    private static final String URL_START_PAGE = "https://www.kusss.jku.at/kusss/studentwelcome.action";
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
    private static final String URL_MY_STUDIES = "https://www.kusss.jku.at/kusss/studentsettings.action";
    private static final String SELECT_MY_STUDIES = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > div.tabcontainer > div.tabcontent > form > table > tbody > tr[class]:has(td)";

    private static final int TIMEOUT_LOGIN = 15 * 1000; // 15s
    private static final int TIMEOUT_SEARCH_EXAM_BY_LVA = 10 * 1000; //10s

    private static KusssHandler handler = null;
    private CookieManager mCookies;

    private KusssHandler() {
        this.mCookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(mCookies);
    }

    public static synchronized KusssHandler getInstance() {
        if (handler == null) {
            handler = new KusssHandler();
        }
        return handler;
    }

    public String getSessionIDFromCookie() {
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
            Log.e(TAG, "getSessionIDFromCookie", e);
            return null;
        }
    }

    public synchronized String login(Context c, String user, String password) {
        if (user == null || password == null) {
            return null;
        }

        try {
            if ((user.length() > 0) && (user.charAt(0) != 'k')) {
                user = "k" + user;
            }

            mCookies.getCookieStore().removeAll();

            Jsoup.connect(URL_KUSSS_INDEX).timeout(TIMEOUT_LOGIN).followRedirects(true).get();

            Connection.Response r = Jsoup.connect(URL_LOGIN).cookies(getCookieMap()).data("j_username", user).data("j_password", password).timeout(TIMEOUT_LOGIN).followRedirects(true).method(Connection.Method.POST).execute();

            if (r.url() != null) {
                r = Jsoup.connect(r.url().toString()).cookies(getCookieMap()).method(Connection.Method.GET).execute();
            }

            Document doc = r.parse();

            String sessionId = getSessionIDFromCookie();
            if (isLoggedIn(c, doc)) {
                return sessionId;
            }

            if (isLoggedIn(c, sessionId)) {
                return sessionId;
            }

            Log.w(TAG, "login failed: isLoggedIn=FALSE");
            return null;
        } catch (SocketTimeoutException e) {
            // bad connection, timeout
            Log.w(TAG, "login failed: connection timeout", e);
            return null;
        } catch (Exception e) {
            Log.w(TAG, "login failed", e);
            Analytics.sendException(c, e, true);
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

    private String getCookieString() {
        String cookies = "";
        for (HttpCookie cookie : mCookies.getCookieStore().getCookies()) {
            cookies += String.format("%s=%s;", cookie.getName(), cookie.getValue());
        }
        return cookies;
    }


    private void writeParams(URLConnection conn, String[] keys, String[] values)
            throws IOException {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            builder.append(keys[i]);
            builder.append("=");
            builder.append(values[i]);
            if (i < keys.length - 1) {
                builder.append("&");
            }
        }

        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(builder.toString());
        wr.flush();
    }

    public synchronized boolean logout(Context c) {
        try {
            Connection.Response r = Jsoup.connect(URL_LOGOUT).cookies(getCookieMap()).method(Connection.Method.GET).execute();

            if (r == null) {
                return false;
            }

            if (!isLoggedIn(c, (String)null)) {
                mCookies.getCookieStore().removeAll();
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.w(TAG, "logout failed", e);
            Analytics.sendException(c, e, true);
            return true;
        }
    }

    public synchronized boolean isLoggedIn(Context c, String sessionId) {
        try {
            Document doc = Jsoup.connect(URL_START_PAGE).cookies(getCookieMap()).timeout(TIMEOUT_LOGIN).followRedirects(true).get();

            return isLoggedIn(c, doc);
        } catch (SocketTimeoutException e) {
            // bad connection, timeout
            return false;
        } catch (IOException e) {
            Log.e(TAG, "isLoggedIn", e);
            Analytics.sendException(c, e, true);
            return false;
        }
    }

    private boolean isLoggedIn(Context c, Document doc) {
        Elements logoutAction = doc.select(SELECT_LOGOUT);
        if (logoutAction.size() > 0) {
            return true;
        }

        return false;
    }

    public synchronized boolean isAvailable(Context c, String sessionId,
                                            String user, String password) {
        if (!isLoggedIn(c, sessionId)) {
            return login(c, user, password) != null;
        }
        return true;
    }

    public Calendar getLVAIcal(Context c, CalendarBuilder mCalendarBuilder) {

        Calendar iCal = null;

        try {

            URL url = new URL(URL_GET_ICAL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Cookie", getCookieString());
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);

            writeParams(conn, new String[]{"selectAll"},
                    new String[]{"ical.category.mycourses"});

            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());

            iCal = mCalendarBuilder.build(in);

            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "getLVAIcal", e);
            Analytics.sendException(c, e, true);
            iCal = null;
        }

        return iCal;
    }

    public Calendar getExamIcal(Context c, CalendarBuilder mCalendarBuilder) {
        Calendar iCal = null;

        try {
            URL url = new URL(URL_GET_ICAL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Cookie", getCookieString());
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);

            writeParams(conn, new String[]{"selectAll"},
                    new String[]{"ical.category.examregs"});

            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());

            iCal = mCalendarBuilder.build(in);

            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "getExamIcal", e);
            Analytics.sendException(c, e, true);
            iCal = null;
        }

        return iCal;
    }

    public Map<String, String> getTerms(Context c) {
        Map<String, String> terms = new HashMap<>();
        try {
            Document doc = Jsoup.connect(URL_GET_TERMS).cookies(getCookieMap()).get();
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
            Log.e(TAG, "getTerms", e);
            Analytics.sendException(c, e, true);
            return null;
        }
        return terms;
    }

    public boolean selectTerm(Context c, Term term) throws IOException {
        Document doc = Jsoup.connect(URL_SELECT_TERM)
                .cookies(getCookieMap())
                .data("term", term.toString())
                .data("previousQueryString", "")
                .data("reloadAction", "coursecatalogue-start.action").post();

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

        List<Course> courses = new ArrayList<>();
        try {
            Log.d(TAG, "getCourses");

            for (Term term : terms) {
                term.setLoaded(false); // init loaded flag
                if (selectTerm(c, term)) {
                    Document doc = Jsoup.connect(URL_MY_LVAS).cookies(getCookieMap()).get();

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
                    throw new IOException(String.format("cannot select term: %s", term));
                }
            }
            if (courses != null && courses.size() == 0) {
                // break if no lvas found, a student without courses is a quite impossible case
                return null;
            }
        } catch (Exception e) {
            Analytics.sendException(c, e, true);
            return null;
        }
        return courses;
    }

    private boolean isSelectable(Context c, Document doc, Term term) {
        try {
            Element termSelector = doc.getElementById("term");
            if (termSelector == null) return false;

            Elements selectable = termSelector.getElementsByAttributeValue("value", term.toString());
            if (selectable.size() != 1) return false;

            return true;
        } catch (Exception e) {
            Analytics.sendException(c, e, true);
            return false;
        }
    }

    private boolean isSelected(Context c, Document doc, Term term) {
        try {
            Elements terms = doc.getElementById("term").getElementsByAttribute(
                    "selected");

            for (Element termEntry : terms) {
                if (termEntry.attr("value").equals(term.toString())) {
                    return true;
                }
            }
        } catch (Exception e) {
            Analytics.sendException(c, e, true);
            return false;
        }
        return false;
    }

    public List<Assessment> getAssessments(Context c) {
        List<Assessment> grades = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL_MY_GRADES).cookies(getCookieMap()).data("months", "0")
                    .get();

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
        } catch (IOException e) {
            Log.e(TAG, "getAssessments", e);
            Analytics.sendException(c, e, true);
            return null;
        }
        Log.d(TAG, grades.size() + " grades found");
        return grades;
    }

    public List<Exam> getNewExams(Context c) {
        List<Exam> exams = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL_GET_NEW_EXAMS)
                    .cookies(getCookieMap())
                    .data("search", "true").data("searchType", "mylvas").get();

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
        } catch (Exception e) {
            Log.e(TAG, "getNewExams", e);
            Analytics.sendException(c, e, true);
            return null;
        }
        return exams;
    }

    public List<Exam> getNewExamsByCourseId(Context c, List<Course> courses, List<Term> terms)
            throws IOException {

        List<Exam> exams = new ArrayList<>();
        try {
            if (courses == null || courses.size() == 0) {
                Log.d(TAG, "no lvas found, reload");
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
                                Log.d(TAG,
                                        existing.getTitle() + " --> "
                                                + grade.getTitle());
                            }
                            gradeCache.put(grade.getCourseId(), grade);
                        }
                    }
                }

                for (Course course : courses) {
                    Assessment grade = gradeCache.get(course.getCourseId());
                    if (grade != null) {
                        if ((grade.getGrade() == Grade.G5)
                                || (grade.getDate().getTime() > (System
                                .currentTimeMillis() - (182 * DateUtils.DAY_IN_MILLIS)))) {
                            Log.d(TAG,
                                    "positive in last 6 Months: "
                                            + grade.getTitle());
                            grade = null;
                        }
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
            Log.e(TAG, "getNewExamsByCourseId", e);
            Analytics.sendException(c, e, true);
            return null;
        }
        return exams;
    }

    private List<Exam> getNewExamsByCourseId(Context c, String courseId) {
        List<Exam> exams = new ArrayList<>();
        try {
            final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

            Log.d(TAG, "getNewExamsByCourseId: " + courseId);
            Document doc = Jsoup
                    .connect(URL_GET_NEW_EXAMS)
                    .timeout(TIMEOUT_SEARCH_EXAM_BY_LVA)
                    .data("search", "true")
                    .data("searchType", "specific")
                    .data("searchDateFrom",
                            df.format(new Date(System.currentTimeMillis())))
                    .data("searchDateTo",
                            df.format(new Date(System.currentTimeMillis()
                                    + DateUtils.YEAR_IN_MILLIS)))
                    .data("searchLvaNr", courseId).data("searchLvaTitle", "")
                    .data("searchCourseClass", "").post();

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
        } catch (IOException e) {
            Log.e(TAG, "getNewExamsByCourseId", e);
            Analytics.sendException(c, e, true);
            exams = null;
        }
        return exams;
    }

    private void loadExams(Context c, List<Exam> exams) throws IOException {
        Log.d(TAG, "loadExams");

        Document doc = Jsoup.connect(URL_GET_EXAMS).cookies(getCookieMap()).get();

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

    public List<Curriculum> getCurricula(Context c) {
        try {
            List<Curriculum> mCurricula = new ArrayList<>();

            Document doc = Jsoup.connect(URL_MY_STUDIES).cookies(getCookieMap()).get();

            Elements rows = doc.select(SELECT_MY_STUDIES);
            for (Element row : rows) {
                Curriculum s = new Curriculum(c, row);
                if (s.isInitialized()) {
                    mCurricula.add(s);
                }
            }
            return mCurricula;
        } catch (Exception e) {
            Analytics.sendException(c, e, true);
            return null;
        }
    }

}
