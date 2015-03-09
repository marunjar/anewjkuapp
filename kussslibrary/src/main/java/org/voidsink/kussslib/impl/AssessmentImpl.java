package org.voidsink.kussslib.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.voidsink.kussslib.Assessment;
import org.voidsink.kussslib.AssessmentType;
import org.jsoup.select.Elements;
import org.voidsink.kussslib.CourseType;
import org.voidsink.kussslib.Grade;
import org.voidsink.kussslib.Term;

public class AssessmentImpl implements Assessment {

	private static final Pattern courseIdTermPattern = Pattern
			.compile(Parser.PATTERN_LVA_NR_COMMA_TERM);
	private static final Pattern courseIdPattern = Pattern
			.compile(Parser.PATTERN_LVA_NR);
	private static final Pattern termPattern = Pattern
			.compile(Parser.PATTERN_TERM);

	private Date date;
	private String title;
	private Term term;
	private String courseId;
	private Grade grade;
	private int cid;
	private AssessmentType assessmentType;
	private String classCode;
	private double ects;
	private double sws;
	private CourseType courseType;

	AssessmentImpl(Date date, String title, Term term, String courseId,
			Grade grade, int cid, AssessmentType assessmentType,
			String classCode, double ects, double sws, CourseType courseType) {

		this.date = date;
		this.title = title;
		this.term = term;
		this.courseId = courseId;
		this.grade = grade;
		this.cid = cid;
		this.assessmentType = assessmentType;
		this.classCode = classCode;
		this.ects = ects;
		this.sws = sws;
		this.courseType = courseType;
	}

	public AssessmentImpl(AssessmentType type, Element row)
			throws ParseException {

		this(null, "", null, "", null, 0, type, "", 0, 0, null);

		final Elements columns = row.getElementsByTag("td");

		if (columns.size() >= 7) {

			title = columns.get(1).text();
			Matcher courseIdTermMatcher = courseIdTermPattern.matcher(title); // (courseId,term)

			if (courseIdTermMatcher.find()) {

				String courseIdTerm = courseIdTermMatcher.group();
				Matcher courseIdMatcher = courseIdPattern.matcher(courseIdTerm); // courseId

				if (courseIdMatcher.find()) {
					courseId = courseIdMatcher.group();
				}

				Matcher termMatcher = termPattern.matcher(courseIdTerm);

				if (termMatcher.find(courseIdMatcher.end())) {

					String termStr = termMatcher.group();
					term = Term.parseTerm(termStr);
				}

				String tmp = title.substring(0, courseIdTermMatcher.start());

				if (courseIdTermMatcher.end() <= title.length()) {

					String addition = title
							.substring(courseIdTermMatcher.end(),
									title.length())
							.replaceAll("(\\(.*?\\))", "").trim();

					if (addition.length() > 0) {
						tmp = tmp + " (" + addition + ")";
					}
				}

				title = tmp;
			}

			title = title.trim(); // title
			courseType = CourseType.parseCourseType(columns.get(4).text()); // courseType

			date = Parser.parseDate(columns.get(0).text(),
					String.format(Parser.FAILED_PARSING_DATE, "issue")); // date
			grade = Grade.parseGrade(columns.get(2).text()); // grade

			try {
				String[] ectsSws = columns.get(5).text().replace(",", ".")
						.split("/");
				if (ectsSws.length == 2) {
					ects = Double.parseDouble(ectsSws[0]);
					sws = Double.parseDouble(ectsSws[1]);
				}
			} catch (NumberFormatException e) {
				// TODO: Error position? Oder NumberFormatException weitergeben?
				throw new ParseException(String.format(
						Parser.FAILED_PARSING_NUMERIC_VALUE, "ects/sws"), 0);
			}

			try {
				String cidText = columns.get(6).text();
				if (!TextUtils.isEmpty(cidText)) {
					cid = Integer.parseInt(cidText); // curriculum id
				}
			} catch (NumberFormatException e) {
				// TODO: Error position? Oder NumberFormatException weitergeben?
				throw new ParseException(String.format(
						Parser.FAILED_PARSING_NUMERIC_VALUE, "curriculum id"),
						0);
			}

			classCode = columns.get(3).text();
		}
	}

	// TODO: stimmt das noch so?
	public boolean isInitialized() {
		return this.assessmentType != null && this.date != null
				&& this.grade != null;
	}

	public Date getDate() {
		return date;
	}

	public String getTitle() {
		return title;
	}

	public Term getTerm() {
		return term;
	}

	public String getCourseId() {
		return courseId;
	}

	public Grade getGrade() {
		return grade;
	}

	public int getCid() {
		return cid;
	}

	public AssessmentType assessmentType() {
		return assessmentType;
	}

	public String getClassCode() {
		return classCode;
	}

	public double getEcts() {
		return ects;
	}

	public double getSws() {
		return sws;
	}

	public CourseType getCourseType() {
		return courseType;
	}

}
