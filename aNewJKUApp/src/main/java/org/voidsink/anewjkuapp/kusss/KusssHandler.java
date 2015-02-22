package org.voidsink.anewjkuapp.kusss;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import java.util.Collections;
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
    private static final String SELECT_NOT_LOGGED_IN = "body > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > h4";
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

            Document doc = Jsoup.connect(URL_LOGIN).timeout(TIMEOUT_LOGIN).data("j_username", user)
                    .data("j_password", password).post();

            //TODO: check document for successful login message

            String sessionId = getSessionIDFromCookie();

            if (isLoggedIn(c, sessionId)) {
                return sessionId;
            }
            return null;
        } catch (SocketTimeoutException e) {
            // bad connection, timeout
            return null;
        } catch (Exception e) {
            Log.w(TAG, "login failed", e);
            Analytics.sendException(c, e, true);
            return null;
        }
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
            Connection.Response r = Jsoup.connect(URL_LOGOUT).method(Connection.Method.GET).execute();

            if (r == null) {
                return false;
            }

            return !isLoggedIn(c, null);
        } catch (Exception e) {
            Log.w(TAG, "logout failed", e);
            Analytics.sendException(c, e, true);
            return true;
        }
    }

    public synchronized boolean isLoggedIn(Context c, String sessionId) {
        try {
            String actSessionId = getSessionIDFromCookie();
            if (actSessionId == null || sessionId == null
                    || !sessionId.equals(actSessionId)) {
                Log.d(TAG, "not logged in, wrong sessionID");
                return false;
            }

            Document doc = Jsoup.connect(URL_START_PAGE).timeout(TIMEOUT_LOGIN).get();

            Elements notLoggedIn = doc.select(SELECT_NOT_LOGGED_IN);
            if (notLoggedIn.size() > 0) {
                return false;
            }
        } catch (SocketTimeoutException e) {
            // bad connection, timeout
            return false;
        } catch (IOException e) {
            Log.e(TAG, "isLoggedIn", e);
            Analytics.sendException(c, e, true);
            return false;
        }
        return true;
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
            Document doc = Jsoup.connect(URL_GET_TERMS).get();
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

    public boolean selectTerm(Context c, String term) throws IOException {
        Document doc = Jsoup.connect(URL_SELECT_TERM)
                .data("term", term)
                .data("previousQueryString", "")
                .data("reloadAction", "coursecatalogue-start.action").post();

        //TODO: check document for successful selection of term
//            if (!isSelected(doc, term)) {
//                throw new IOException(String.format("selection of term failed: %s", term));
//            }
        return true;
    }

    public List<Lva> getLvas(Context c, List<Term> terms) {
        if (terms == null || terms.size() == 0) {
            return null;
        }

        List<Lva> lvas = new ArrayList<>();
        try {
            Log.d(TAG, "getLvas");

            for (Term term : terms) {
                term.setLoaded(false); // init loaded flag
                if (selectTerm(c, term.getTerm())) {
                    Document doc = Jsoup.connect(URL_MY_LVAS).get();

                    if (isSelectable(c, doc, term.getTerm())) {
                        if (isSelected(c, doc, term.getTerm())) {
                            // .select("body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > table > tbody > tr");
                            Elements rows = doc.select(SELECT_MY_LVAS);
                            for (Element row : rows) {
                                Lva lva = new Lva(c, term.getTerm(), row);
                                if (lva.isInitialized()) {
                                    lvas.add(lva);
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
            if (lvas != null && lvas.size() == 0) {
                // break if no lvas found, a student without courses is a quite impossible case
                throw new IOException("no lvas found");
            }
        } catch (Exception e) {
            Analytics.sendException(c, e, true);
            return null;
        }
        return lvas;
    }

    private boolean isSelectable(Context c, Document doc, String term) {
        try {
            Element termSelector = doc.getElementById("term");
            if (termSelector == null) return false;

            Elements selectable = termSelector.getElementsByAttributeValue("value", term);
            if (selectable.size() != 1) return false;

            return true;
        } catch (Exception e) {
            Analytics.sendException(c, e, true);
            return false;
        }
    }

    private boolean isSelected(Context c, Document doc, String term) {
        try {
            Elements terms = doc.getElementById("term").getElementsByAttribute(
                    "selected");

            for (Element termEntry : terms) {
                if (termEntry.attr("value").equals(term)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Analytics.sendException(c, e, true);
            return false;
        }
        return false;
    }

    public List<ExamGrade> getGrades(Context c) {
        List<ExamGrade> grades = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL_MY_GRADES).data("months", "0")
                    .get();

            Elements rows = doc.select(SELECT_MY_GRADES);

            GradeType type = null;
            for (Element row : rows) {
                if (row.tag().toString().equals("h3")) {
                    type = GradeType.parseGradeType(row.text());
                } else if (row.tag().toString().equals("table")) {
                    Elements gradeRows = row
                            .select("tbody > tr[class]:has(td)");
                    for (Element gradeRow : gradeRows) {
                        ExamGrade grade = new ExamGrade(c, type, gradeRow);
                        if (grade.isInitialized()) {
                            grades.add(grade);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "getGrades", e);
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

    public List<Exam> getNewExamsByLvaNr(Context c, List<Lva> lvas, List<Term> terms)
            throws IOException {

        List<Exam> exams = new ArrayList<>();
        try {
            if (lvas == null || lvas.size() == 0) {
                Log.d(TAG, "no lvas found, reload");
                lvas = getLvas(c, terms);
            }
            if (lvas != null && lvas.size() > 0) {
                Map<String, ExamGrade> gradeCache = new HashMap<>();

                List<ExamGrade> grades = getGrades(c);
                if (grades != null) {
                    for (ExamGrade grade : grades) {
                        if (!grade.getLvaNr().isEmpty()) {
                            ExamGrade existing = gradeCache.get(grade.getLvaNr());
                            if (existing != null) {
                                Log.d(TAG,
                                        existing.getTitle() + " --> "
                                                + grade.getTitle());
                            }
                            gradeCache.put(grade.getLvaNr(), grade);
                        }
                    }
                }

                for (Lva lva : lvas) {
                    ExamGrade grade = gradeCache.get(lva.getLvaNr());
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
                        List<Exam> newExams = getNewExamsByLvaNr(c,
                                lva.getLvaNr());
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
            Log.e(TAG, "getNewExamsByLvaNr", e);
            Analytics.sendException(c, e, true);
            return null;
        }
        return exams;
    }

    private List<Exam> getNewExamsByLvaNr(Context c, String lvaNr) {
        List<Exam> exams = new ArrayList<>();
        try {
            final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

            Log.d(TAG, "getNewExamsByLvaNr: " + lvaNr);
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
                    .data("searchLvaNr", lvaNr).data("searchLvaTitle", "")
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
            Log.e(TAG, "getNewExamsByLvaNr", e);
            Analytics.sendException(c, e, true);
            exams = null;
        }
        return exams;
    }

    private void loadExams(Context c, List<Exam> exams) throws IOException {
        Log.d(TAG, "loadExams");

        Document doc = Jsoup.connect(URL_GET_EXAMS).get();

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

    public List<Studies> getStudies(Context c) {
        try {
            List<Studies> studies = new ArrayList<>();

            Document doc = Jsoup.connect(URL_MY_STUDIES).get();

            Elements rows = doc.select(SELECT_MY_STUDIES);
            for (Element row : rows) {
                Studies s = new Studies(c, row);
                if (s.isInitialized()) {
                    studies.add(s);
                }
            }
            return studies;
        } catch (Exception e) {
            Analytics.sendException(c, e, true);
            return null;
        }
    }

    public void showExamInBrowser(Context c, String lvaNr) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_GET_EXAMS));
        c.startActivity(intent);
        // TODO: create activity with webview that uses stored credentials to login and open page with search for lvanr
    }
}
