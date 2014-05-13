package org.voidsink.anewjkuapp.kusss;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.text.format.DateUtils;
import android.util.Log;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

public class KusssHandler {

	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat df = new SimpleDateFormat(
			"dd.MM.yyyy");

	private static final String TAG = KusssHandler.class.getSimpleName();

	private static final String URL_MY_LVAS = "https://www.kusss.jku.at/kusss/listmystudentlvas.action";
	private static final String URL_GET_TERMS = "https://www.kusss.jku.at/kusss/listmystudentlvas.action";
	private static final String URL_GET_ICAL = "https://www.kusss.jku.at/kusss/ical-multi-sz.action";
	private static final String URL_MY_GRADES = "https://www.kusss.jku.at/kusss/gradeinfo.action";
	private static final String URL_START_PAGE = "https://www.kusss.jku.at/kusss/studentwelcome.action";
	private static final String URL_LOGOUT = "https://www.kusss.jku.at/kusss/logout.action";
	private static final String URL_LOGIN = "https://www.kusss.jku.at/kusss/login.action";
	private static final String URL_GET_EXAMS = "https://www.kusss.jku.at/kusss/szsearchexam.action";
	private static final String URL_SELECT_TERM = "https://www.kusss.jku.at/kusss/select-term.action";

	private static final String SELECT_MY_LVAS = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > table > tbody > tr:has(td)";
	private static final String SELECT_MY_GRADES = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > *";
	private static final String SELECT_NOT_LOGGED_IN = "body > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > h4";
	// private static final String SELECT_ACTUAL_EXAMS =
	// "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > div.tabcontainer > div.tabcontent > table > tbody > tr > td > form > table > tbody > tr:has(td)";
	private static final String SELECT_NEW_EXAMS = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > div.tabcontainer > div.tabcontent > div.sidetable > form > table > tbody > tr:has(td)";

	private CookieManager mCookies;

	public KusssHandler() {
		this.mCookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(mCookies);
	}

	public Boolean login(String user, String password) {
		try {
			if ((user.length() > 0) && (user.charAt(0) != 'k')) {
				user = "k" + user;
			}
			Jsoup.connect(URL_LOGIN).data("j_username", user)
					.data("j_password", password).get();

			return isLoggedIn();
		} catch (Exception e) {
			Log.e(TAG, "login", e);
			return false;
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

	public boolean logout() {
		try {
			Jsoup.connect(URL_LOGOUT).get();

			return !isLoggedIn();
		} catch (Exception e) {
			Log.e(TAG, "logout", e);
			return true;
		}
	}

	public boolean isLoggedIn() {
		try {
			Document doc = Jsoup.connect(URL_START_PAGE).get();

			Elements notLoggedIn = doc.select(SELECT_NOT_LOGGED_IN);
			if (notLoggedIn.size() > 0) {
				return false;
			}

		} catch (IOException e) {
			Log.e(TAG, "isLoggedIn", e);
			return false;
		}
		return true;
	}

	public boolean isAvailable(String user, String password) {
		if (!isLoggedIn()) {
			return login(user, password);
		}
		return true;
	}

	public Calendar getLVAIcal(CalendarBuilder mCalendarBuilder) {

		Calendar iCal = null;

		try {

			URL url = new URL(URL_GET_ICAL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);

			writeParams(conn, new String[] { "selectAll" },
					new String[] { "ical.category.mycourses" });

			iCal = mCalendarBuilder.build(conn.getInputStream());
		} catch (Exception e) {
			Log.e(TAG, "getLVAIcal", e);
			iCal = null;
		}

		return iCal;
	}

	public Calendar getExamIcal(CalendarBuilder mCalendarBuilder) {
		Calendar iCal = null;

		try {
			URL url = new URL(URL_GET_ICAL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);

			writeParams(conn, new String[] { "selectAll" },
					new String[] { "ical.category.examregs" });

			iCal = mCalendarBuilder.build(conn.getInputStream());
		} catch (Exception e) {
			Log.e(TAG, "getExamIcal", e);
			iCal = null;
		}

		return iCal;
	}

	public Map<String, String> getTerms() {
		Map<String, String> terms = new HashMap<String, String>();
		try {
			Document doc = Jsoup.connect(URL_GET_TERMS).get();
			Element termDropdown = doc.getElementById("term");
			Elements termDropdownEntries = termDropdown
					.getElementsByClass("dropdownentry");

			for (Element termDropdownEntry : termDropdownEntries) {
				terms.put(termDropdownEntry.attr("value"),
						termDropdownEntry.text());
			}
		} catch (Exception e) {
			Log.e(TAG, "getTerms", e);
			return null;
		}
		return terms;
	}

	public boolean selectTerm(String term) {
		try {
			Jsoup.connect(URL_SELECT_TERM).data("term", term)
					.data("previousQueryString", "")
					.data("reloadAction", "listmystudentlvas.action").post();
		} catch (IOException e) {
			Log.e(TAG, "selectTerm", e);
			return false;
		}
		Log.d(TAG, term + " selected");
		return true;
	}

	public List<Lva> getLvas() {
		List<Lva> lvas = new ArrayList<Lva>();
		try {
			List<String> terms = new ArrayList<>(getTerms().keySet());
			if (terms != null) {
				Collections.sort(terms);
				for (String term : terms) {
					if (selectTerm(term)) {
						Document doc = Jsoup.connect(URL_MY_LVAS).get();

						if (isSelected(doc, term)) {
							// .select("body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > table > tbody > tr");
							Elements rows = doc.select(SELECT_MY_LVAS);
							for (Element row : rows) {
								Lva lva = new Lva(term, row);
								if (lva.isInitialized()) {
									lvas.add(lva);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "getLvas", e);
			return null;
		}
		return lvas;
	}

	private boolean isSelected(Document doc, String term) {
		try {
			Elements terms = doc.getElementById("term").getElementsByAttribute(
					"selected");

			for (Element termEntry : terms) {
				if (termEntry.attr("value").equals(term)) {
					return true;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "isSelected", e);
			return false;
		}
		return false;
	}

	public List<ExamGrade> getGrades() {
		List<ExamGrade> grades = new ArrayList<ExamGrade>();
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
						ExamGrade grade = new ExamGrade(type, gradeRow);
						if (grade.isInitialized()) {
							grades.add(grade);
						}
					}
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "getGrades", e);
			return null;
		}
		Log.d(TAG, grades.size() + " grades found");
		return grades;
	}

	public List<Exam> getNewExams() {
		List<Exam> exams = new ArrayList<Exam>();
		try {
			Document doc = Jsoup.connect(URL_GET_EXAMS).data("search", "true")
					.data("searchType", "mylvas").get();

			Elements rows = doc.select(SELECT_NEW_EXAMS);

			int i = 0;
			while (i < rows.size()) {
				Element row = rows.get(i);
				Exam exam = new Exam(row);
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
			Log.e(TAG, "getNewExams", e);
			return null;
		}
		return exams;
	}

	public List<Exam> getNewExamsByLvaNr(List<Lva> lvas) throws IOException {

		List<Exam> exams = new ArrayList<Exam>();
		if (lvas.size() > 0) {
			List<ExamGrade> grades = getGrades();

			Map<Integer, ExamGrade> gradeCache = new HashMap<Integer, ExamGrade>();
			for (ExamGrade grade : grades) {
				if (grade.getLvaNr() > 0) {
					ExamGrade existing = gradeCache.get(grade.getLvaNr());
					if (existing != null) {
						Log.d(TAG,
								existing.getTitle() + " --> "
										+ grade.getTitle());
					}
					gradeCache.put(grade.getLvaNr(), grade);
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
					List<Exam> newExams = getNewExamsByLvaNr(lva.getLvaNr());
					for (Exam newExam : newExams) {
						if (newExam != null) {
							exams.add(newExam);
						}
					}
				}
			}
		}
		return exams;
	}

	private List<Exam> getNewExamsByLvaNr(int lvaNr) {
		List<Exam> exams = new ArrayList<Exam>();
		try {
			Log.d(TAG, "getNewExamsByLvaNr: " + lvaNr);
			Document doc = Jsoup
					.connect(URL_GET_EXAMS)
					.data("search", "true")
					.data("searchType", "specific")
					.data("searchDateFrom",
							df.format(new Date(System.currentTimeMillis())))
					.data("searchDateTo",
							df.format(new Date(System.currentTimeMillis()
									+ DateUtils.YEAR_IN_MILLIS)))
					.data("searchLvaNr", Integer.toString(lvaNr))
					.data("searchLvaTitle", "").data("searchCourseClass", "")
					.get();

			Elements rows = doc.select(SELECT_NEW_EXAMS);

			int i = 0;
			while (i < rows.size()) {
				Element row = rows.get(i);
				Exam exam = new Exam(row);
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
			exams = null;
		}
		return exams;
	}

	public static KusssHandler handler = new KusssHandler();
}
