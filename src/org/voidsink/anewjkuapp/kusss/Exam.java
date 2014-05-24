package org.voidsink.anewjkuapp.kusss;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.KusssContentContract;
import android.content.ContentValues;

public class Exam {

	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy");
	private final Pattern lvaNrTermPattern = Pattern
			.compile("\\(\\d{6}\\,\\d{4}[swSW]\\)");
	private final Pattern lvaNrPattern = Pattern.compile("\\d{6}");
	private final Pattern termPattern = Pattern.compile("\\d{4}[swSW]");
	private final Pattern timePattern = Pattern.compile("\\d{2}\\:\\d{2}");

	private int lvaNr = 0;
	private String term = "";
	private Date date = null;
	private String time = "";
	private String location = "";
	private String description = "";
	private String info = "";
	private String title = "";

	public Exam(Element row) {
		Elements columns = row.getElementsByTag("td");
		// allow only exams that can be selected
		if (columns.size() >= 5 && columns.get(0).select("input").size() == 1) {
			try {
				Matcher lvaNrTermMatcher = lvaNrTermPattern.matcher(columns
						.get(1).text()); // (lvaNr,term)
				if (lvaNrTermMatcher.find()) {
					String lvaNrTerm = lvaNrTermMatcher.group();
					setTitle(columns.get(1).text()
							.substring(0, lvaNrTermMatcher.start()));

					Matcher lvaNrMatcher = lvaNrPattern.matcher(lvaNrTerm); // lvaNr
					if (lvaNrMatcher.find()) {
						setLvaNr(Integer.parseInt(lvaNrMatcher.group()));
					}

					Matcher termMatcher = termPattern.matcher(lvaNrTerm); // term
					if (termMatcher.find()) {
						setTerm(termMatcher.group());
					}

					setDate(dateFormat.parse(columns.get(2).text())); // date

					setTimeLocation(columns.get(3).text());
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}
	}

	private void setTitle(String title) {
		this.title = title;
	}

	private void setLvaNr(int lvaNr) {
		this.lvaNr = lvaNr;
	}

	private void setTerm(String term) {
		this.term = term;
	}

	private void setDate(Date date) {
		this.date = date;
	}

	private void setTimeLocation(String timeLocation) {
		String[] splitted = timeLocation.split("\\/");

		Matcher timeMatcher = timePattern.matcher(splitted[0]);
		String time = "";
		while (timeMatcher.find()) {
			if (!time.isEmpty()) {
				time += " - ";
			}
			time += timeMatcher.group();
		}

		this.time = time;
		this.location = splitted[1];
	}

	private void setDescription(String description) {
		this.description = description;
	}

	private void setInfo(String info) {
		this.info = info;
	}

	public boolean isInitialized() {
		return this.lvaNr > 0 && !this.term.isEmpty() && this.date != null;
	}

	public void addAdditionalInfo(Element row) {
		Elements columns = row.getElementsByTag("td");
		if (columns.size() == 1) {
			String text = columns.get(0).text().trim();
			if (!text.isEmpty()) {
				Elements info = columns.get(0).getElementsByAttributeValue(
						"class", "info_icon");
				if (info.size() > 0) {
					setInfo(text);
				} else {
					setDescription(text);
				}
			}
		}
	}

	public String getTime() {
		return this.time;
	}

	public String getLocation() {
		return this.location;
	}

	public String getInfo() {
		return this.info;
	}

	public String getDescription() {
		return this.description;
	}

	public int getLvaNr() {
		return this.lvaNr;
	}

	public String getTerm() {
		return this.term;
	}

	public Date getDate() {
		return this.date;
	}

	public ContentValues getContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(KusssContentContract.Exam.EXAM_COL_DATE, getDate().getTime());
		cv.put(KusssContentContract.Exam.EXAM_COL_DESCRIPTION, getDescription());
		cv.put(KusssContentContract.Exam.EXAM_COL_INFO, getInfo());
		cv.put(KusssContentContract.Exam.EXAM_COL_LOCATION, getLocation());
		cv.put(KusssContentContract.Exam.EXAM_COL_LVANR, getLvaNr());
		cv.put(KusssContentContract.Exam.EXAM_COL_TERM, getTerm());
		cv.put(KusssContentContract.Exam.EXAM_COL_TIME, getTime());
		return cv;
	}

	public String getTitle() {
		return title;
	}

	public String getKey() {
		return getKey(this.lvaNr, this.term, this.date.getTime());
	}
	
	public static String getKey(int lvaNr, String term, long date) {
		return String.format("%d-%s-%d", lvaNr, term, date);
	}
}
