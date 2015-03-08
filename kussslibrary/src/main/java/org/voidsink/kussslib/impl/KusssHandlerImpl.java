package org.voidsink.kussslib.impl;

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
import java.util.concurrent.TimeUnit;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.kussslib.Assessment;
import org.voidsink.kussslib.AssessmentType;
import org.voidsink.kussslib.Course;
import org.voidsink.kussslib.Curricula;
import org.voidsink.kussslib.EventType;
import org.voidsink.kussslib.Exam;
import org.voidsink.kussslib.Grade;
import org.voidsink.kussslib.KusssHandler;
import org.voidsink.kussslib.Term;

public class KusssHandlerImpl implements KusssHandler {

	private CookieManager mCookies;
	private String sessionId = "";
	private IExceptionListener exceptionListener = null;

	//TODO: Prüfen, ob die Patterns hier noch hineingehören
    public static final String PATTERN_LVA_NR_WITH_DOT = "\\d{3}\\.\\w{3}";
    public static final String PATTERN_LVA_NR = "\\d{3}\\w{3}";
    public static final String PATTERN_TERM = "\\d{4}[swSW]";
    public static final String PATTERN_LVA_NR_COMMA_TERM = "\\("
            + PATTERN_LVA_NR + "," + PATTERN_TERM + "\\)";
    public static final String PATTERN_LVA_NR_SLASH_TERM = "\\("
            + PATTERN_LVA_NR + "\\/" + PATTERN_TERM + "\\)";
    
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
	private static final String URL_MY_STUDIES = "https://www.kusss.jku.at/kusss/studentsettings.action";

	private static final String SELECT_MY_LVAS = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > table > tbody > tr:has(td)";
	private static final String SELECT_MY_GRADES = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > *";
	private static final String SELECT_NOT_LOGGED_IN = "body > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > h4";
	// private static final String SELECT_ACTUAL_EXAMS =
	// "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > div.tabcontainer > div.tabcontent > table > tbody > tr > td > form > table > tbody > tr:has(td)";
	private static final String SELECT_NEW_EXAMS = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > div.tabcontainer > div.tabcontent > div.sidetable > form > table > tbody > tr:has(td)";
	private static final String SELECT_EXAMS = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > div.tabcontainer > div.tabcontent > table > tbody > tr > td > form > table > tbody > tr:has(td)";
	private static final String SELECT_MY_STUDIES = "body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > div.tabcontainer > div.tabcontent > form > table > tbody > tr[class]:has(td)";

	private static final int TIMEOUT_LOGIN = 15 * 1000; // 15s
	private static final int TIMEOUT_SEARCH_EXAM_BY_LVA = 10 * 1000; // 10s
	
	private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
	private static final long YEAR_IN_MILLIS = 365 * DAY_IN_MILLIS; 
					
	
	private void onHandleException(Exception e, boolean fatal) {
		if (exceptionListener != null) {
			exceptionListener.onExceptionOccured(e, fatal);
		}
	}

	@Override
	public synchronized boolean login(String user, String password) {
		if (user == null || password == null) {
			return false;
		}
		try {
			if ((user.length() > 0) && (user.charAt(0) != 'k')) {
				user = "k" + user;
			}

			Document doc = Jsoup.connect(URL_LOGIN).timeout(TIMEOUT_LOGIN)
					.data("j_username", user).data("j_password", password)
					.post();

			// TODO: check document for successful login message

			sessionId = getSessionIDFromCookie();

			if (isLoggedIn()) {
				return true;
			}
			sessionId = null;
			return false;
		} catch (SocketTimeoutException e) {
			// bad connection, timeout
			sessionId = null;
			return false;
		} catch (Exception e) {
			onHandleException(e, true);
			sessionId = null;
			return false;
		}
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
			return null;
		}
	}

	@Override
	public boolean logout() {
		try {
			Connection.Response r = Jsoup.connect(URL_LOGOUT)
					.method(Connection.Method.GET).execute();

			if (r == null) {
				return false;
			}

			return !isLoggedIn();
		} catch (Exception e) {
			onHandleException(e, true);
			return true;
		}
	}

	@Override
	public boolean isLoggedIn() {
		try {
			String actSessionId = getSessionIDFromCookie();
			if (actSessionId == null || sessionId == null
					|| !sessionId.equals(actSessionId)) {
				return false;
			}

			Document doc = Jsoup.connect(URL_START_PAGE).timeout(TIMEOUT_LOGIN)
					.get();

			Elements notLoggedIn = doc.select(SELECT_NOT_LOGGED_IN);
			if (notLoggedIn.size() > 0) {
				return false;
			}
		} catch (SocketTimeoutException e) {
			// bad connection, timeout
			return false;
		} catch (IOException e) {
			onHandleException(e, true);
			return false;
		}
		return true;
	}

	@Override
	public Calendar getEvents(EventType eventType,
			CalendarBuilder calendarBuilder) {
		Calendar iCal = null;

		try {
			URL url = new URL(URL_GET_ICAL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(15000);

			switch (eventType) {
				case EXAM: {
					writeParams(conn, new String[] { "selectAll" },
							new String[] { "ical.category.examregs" });
					break;
				}
				case COURSE: {
					writeParams(conn, new String[] { "selectAll" },
							new String[] { "ical.category.mycourses" });
					break;
				}
			}

			BufferedInputStream in = new BufferedInputStream(
					conn.getInputStream());

			iCal = calendarBuilder.build(in);

			conn.disconnect();
		} catch (Exception e) {
			onHandleException(e, true);
			iCal = null;
		}

		return iCal;
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

	@Override
	public Calendar getEvents(EventType eventType) {
		CalendarBuilder calendarBuilder = new CalendarBuilder();
		return getEvents(eventType, calendarBuilder);
	}

	@Override
	public List<Assessment> getAssessments() {
		List<Assessment> assessments = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL_MY_GRADES).data("months", "0")
                    .get();

            Elements rows = doc.select(SELECT_MY_GRADES);

            AssessmentType type = null;
            for (Element row : rows) {
                if (row.tag().toString().equals("h3")) {
                    type = AssessmentType.parseAssessmentType(row.text());
                } else if (row.tag().toString().equals("table")) {
                    Elements gradeRows = row
                            .select("tbody > tr[class]:has(td)");
                    for (Element gradeRow : gradeRows) {
                        AssessmentImpl assessment = new AssessmentImpl(type, gradeRow);
                        if (assessment.isInitialized()) {
                            assessments.add(assessment);
                        }
                    }
                }
            }
        } catch (IOException e) {
        	onHandleException(e, true);
            return null;
        }
        return assessments;
	}

	@Override
	public List<Exam> getExams() {
		List<Exam> exams = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL_GET_NEW_EXAMS)
                    .data("search", "true").data("searchType", "mylvas").get();

            Elements rows = doc.select(SELECT_NEW_EXAMS);

            int i = 0;
            while (i < rows.size()) {
                Element row = rows.get(i);
                ExamImpl exam = new ExamImpl(row, true);
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
            loadRegisteredExams(exams);
        } catch (Exception e) {
        	onHandleException(e, true);
            return null;
        }
        return exams;
	}

	@Override
	public List<Exam> getExamsByCourses(List<Course> courses) {
        if (courses == null || courses.size() == 0) {
            return null;
        }
		
		List<Exam> exams = new ArrayList<>();
        try {
            Map<String, Assessment> gradeCache = new HashMap<>();

            List<Assessment> assessments = getAssessments();
            if (assessments != null) {
                for (Assessment assessment : assessments) {
                    if (!assessment.getCourseId().isEmpty()) {
                        Assessment existing = gradeCache.get(assessment.getCourseId());
                        if (existing != null) {
//                            Log.d(TAG,
//                                    existing.getTitle() + " --> "
//                                            + assessment.getTitle());
                        }
                        gradeCache.put(assessment.getCourseId(), assessment);
                    }
                }
            }

            for (Course course : courses) {
                Assessment assessment = gradeCache.get(course.getCourseId());
                if (assessment != null) {
                    if ((assessment.getGrade() == Grade.G5)
                            || (assessment.getDate().getTime() > (System
                            .currentTimeMillis() - (182 * DAY_IN_MILLIS)))) {
//                        Log.d(TAG,
//                                "positive in last 6 Months: "
//                                        + grade.getTitle());
                    	assessment = null;
                    }
                }
                if (assessment == null) {
                    List<Exam> newExams = getExamsByCourseId(course.getCourseId());
                    if (newExams != null) {
                        for (Exam newExam : newExams) {
                            if (newExam != null) {
                                exams.add(newExam);
                            }
                        }
                    }
                }
            }

            // add registered exams
            loadRegisteredExams(exams);
        } catch (Exception e) {
            onHandleException(e, true);
            return null;
        }
        return exams;
	}
	
	private List<Exam> getExamsByCourseId(String courseId) {
		List<Exam> exams = new ArrayList<>();
        try {
            final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

            Document doc = Jsoup
                    .connect(URL_GET_NEW_EXAMS)
                    .timeout(TIMEOUT_SEARCH_EXAM_BY_LVA)
                    .data("search", "true")
                    .data("searchType", "specific")
                    .data("searchDateFrom",
                            df.format(new Date(System.currentTimeMillis())))
                    .data("searchDateTo",
                            df.format(new Date(System.currentTimeMillis() + YEAR_IN_MILLIS)))
                    .data("searchLvaNr", courseId).data("searchLvaTitle", "")
                    .data("searchCourseClass", "").post();

            Elements rows = doc.select(SELECT_NEW_EXAMS);

            int i = 0;
            while (i < rows.size()) {
                Element row = rows.get(i);
                ExamImpl exam = new ExamImpl(row, true);
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
            onHandleException(e, true);
            exams = null;
        }
        return exams;
	}
	
	private void loadRegisteredExams(List<Exam> exams) throws IOException {
        Document doc = Jsoup.connect(URL_GET_EXAMS).get();

        Elements rows = doc.select(SELECT_EXAMS);

        int i = 0;
        while (i < rows.size()) {
            Element row = rows.get(i);
            ExamImpl exam = new ExamImpl(row, false);
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
	

	@Override
	public List<Curricula> getCurricula() {
		try {
            List<Curricula> curricula = new ArrayList<>();

            Document doc = Jsoup.connect(URL_MY_STUDIES).get();

            Elements rows = doc.select(SELECT_MY_STUDIES);
            for (Element row : rows) {
            	CurriculaImpl c = new CurriculaImpl(row);
                if (c.isInitialized()) {
                	curricula.add(c);
                }
            }
            return curricula;
        } catch (Exception e) {
        	onHandleException(e, true);
            return null;
        }
	}

	public KusssHandlerImpl() {
		this.mCookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(mCookies);
	}

	@Override
	public void setExceptionListener(KusssHandler.IExceptionListener listener) {
		this.exceptionListener = listener;
	}

	@Override
	public List<Course> getCourses(List<Term> terms) {
		if (terms == null || terms.size() == 0) {
			return null;
		}

		List<Course> courses = new ArrayList<>();
		try {
			for (Term term : terms) {
				term.setLoaded(false); // init loaded flag
				if (selectTerm(term)) {
					Document doc = Jsoup.connect(URL_MY_LVAS).get();

					if (isSelectable(doc, term)) {
						if (isSelected(doc, term)) {
							// .select("body.intra > table > tbody > tr > td > table > tbody > tr > td.contentcell > div.contentcell > table > tbody > tr");
							Elements rows = doc.select(SELECT_MY_LVAS);
							for (Element row : rows) {
								CourseImpl course = new CourseImpl(term, row);
								if (course.isInitialized()) {
									courses.add(course);
								}
							}
							term.setLoaded(true);
						} else {
							throw new IOException(String.format(
									"term not selected: %s", term));
						}
					}
				} else {
					// break if selection failed
					throw new IOException(String.format(
							"cannot select term: %s", term));
				}
			}
			if (courses != null && courses.size() == 0) {
				// break if no lvas found, a student without courses is a quite
				// impossible case
				throw new IOException("no lvas found");
			}
		} catch (Exception e) {
			onHandleException(e, true);
			return null;
		}
		return courses;
	}

	public boolean selectTerm(Term term) throws IOException {
		Document doc = Jsoup.connect(URL_SELECT_TERM)
				.data("term", term.toString()).data("previousQueryString", "")
				.data("reloadAction", "coursecatalogue-start.action").post();

		// TODO: check document for successful selection of term
		// if (!isSelected(doc, term)) {
		// throw new IOException(String.format("selection of term failed: %s",
		// term));
		// }
		return true;
	}

	private boolean isSelectable(Document doc, Term term) {
		try {
			Element termSelector = doc.getElementById("term");
			if (termSelector == null)
				return false;

			Elements selectable = termSelector.getElementsByAttributeValue(
					"value", term.toString());
			if (selectable.size() != 1)
				return false;

			return true;
		} catch (Exception e) {
			onHandleException(e, true);
			return false;
		}
	}

	private boolean isSelected(Document doc, Term term) {
		try {
			Elements terms = doc.getElementById("term").getElementsByAttribute(
					"selected");

			for (Element termEntry : terms) {
				if (termEntry.attr("value").equals(term.toString())) {
					return true;
				}
			}
		} catch (Exception e) {
			onHandleException(e, true);
			return false;
		}
		return false;
	}

}
