package org.voidsink.anewjkuapp.kusss;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.GradeListItem;
import org.voidsink.anewjkuapp.ImportGradeTask;
import org.voidsink.anewjkuapp.KusssContentContract;

public class ExamGrade implements GradeListItem{

	@SuppressLint("SimpleDateFormat")
	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy");

	// private final Pattern lvaNrTermPattern = Pattern
	// .compile("\\(\\d{6}\\,\\d{4}[swSW]\\)");

	private final Pattern lvaNrTermPattern = Pattern.compile("(\\(.*?\\))");

	private final Pattern lvaNrPattern = Pattern.compile("\\d{6}");

	private final Pattern termPattern = Pattern.compile("\\d{4}[swSW]");

	private int skz;
	private Grade grade;
	private String term;
	private int lvaNr;
	private Date date;
	private final GradeType gradeType;
	private String title;
	private String code;

	public ExamGrade(GradeType type, Date date, int lvaNr, String term,
			Grade grade, int skz, String title, String code) {
		this.gradeType = type;
		this.date = date;
		this.lvaNr = lvaNr;
		this.term = term;
		this.grade = grade;
		this.skz = skz;
		this.title = title;
		this.code = code;
	}

	public ExamGrade(GradeType type, Element row) {
		this(type, null, 0, "", null, 0, "", "");

		Elements columns = row.getElementsByTag("td");
		if (columns.size() >= 7) {
			try {
				String title = columns.get(1).text();
				Matcher lvaNrTermMatcher = lvaNrTermPattern.matcher(title); // (lvaNr,term)
				if (lvaNrTermMatcher.find()) {
					String lvaNrTerm = lvaNrTermMatcher.group();

					Matcher lvaNrMatcher = lvaNrPattern.matcher(lvaNrTerm); // lvaNr
					if (lvaNrMatcher.find()) {
						setLvaNr(Integer.parseInt(lvaNrMatcher.group()));
					}

					Matcher termMatcher = termPattern.matcher(lvaNrTerm); // term
					if (termMatcher.find()) {
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

				title = title + " " + columns.get(4).text(); // title + lvaType

				setTitle(title); // title

				setDate(dateFormat.parse(columns.get(0).text())); // date

				setGrade(Grade.parseGrade(columns.get(2).text())); // grade

				setSKZ(Integer.parseInt(columns.get(6).text())); // grade

				setCode(columns.get(3).text());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	
	public ExamGrade(Cursor c) {
		this.lvaNr = c.getInt(ImportGradeTask.COLUMN_GRADE_LVANR);
		this.term = c.getString(ImportGradeTask.COLUMN_GRADE_TERM);
		this.date = new Date(c.getLong(ImportGradeTask.COLUMN_GRADE_DATE));
		this.gradeType = GradeType.parseGradeType(c
				.getInt(ImportGradeTask.COLUMN_GRADE_TYPE));
		this.grade = Grade.parseGradeType(c
				.getInt(ImportGradeTask.COLUMN_GRADE_GRADE));
		this.skz = c.getInt(ImportGradeTask.COLUMN_GRADE_SKZ);
		this.title = c.getString(ImportGradeTask.COLUMN_GRADE_TITLE);
		this.code = c.getString(ImportGradeTask.COLUMN_GRADE_CODE);
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

	private void setLvaNr(int lvaNr) {
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

	public int getLvaNr() {
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
		return this.gradeType != null && this.date != null && this.grade != null;
	}

	public String getTitle() {
		return this.title;
	}

	public ContentValues getContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(KusssContentContract.Grade.GRADE_COL_DATE, getDate().getTime());
		cv.put(KusssContentContract.Grade.GRADE_COL_GRADE, getGrade().ordinal());
		cv.put(KusssContentContract.Grade.GRADE_COL_LVANR, getLvaNr());
		cv.put(KusssContentContract.Grade.GRADE_COL_SKZ, getSkz());
		cv.put(KusssContentContract.Grade.GRADE_COL_TERM, getTerm());
		cv.put(KusssContentContract.Grade.GRADE_COL_TYPE, getGradeType().ordinal());
		cv.put(KusssContentContract.Grade.GRADE_COL_CODE, getCode());
		cv.put(KusssContentContract.Grade.GRADE_COL_TITLE, getTitle());
		return cv;
	}

	@Override
	public boolean isGrade() {
		return true;
	}

	@Override
	public int getType() {
		return GradeListItem.TYPE_GRADE;
	}
}
