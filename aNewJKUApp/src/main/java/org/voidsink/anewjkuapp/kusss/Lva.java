package org.voidsink.anewjkuapp.kusss;

import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.anewjkuapp.update.ImportLvaTask;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.utils.Analytics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class Lva {

	private static final Pattern lvaNrPattern = Pattern
			.compile(KusssHandler.PATTERN_LVA_NR_WITH_DOT);

	private String term;
	private String lvaNr;
	private String title;
	private int skz;
	private String teacher;
	// private double sws;
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

	private void setCode(String code) {
		this.code = code;
	}

	public Lva(Cursor c) {
		this.term = c.getString(ImportLvaTask.COLUMN_LVA_TERM);
		this.lvaNr = c.getString(ImportLvaTask.COLUMN_LVA_LVANR);
		this.title = c.getString(ImportLvaTask.COLUMN_LVA_TITLE);
		this.skz = c.getInt(ImportLvaTask.COLUMN_LVA_SKZ);
		this.teacher = c.getString(ImportLvaTask.COLUMN_LVA_TEACHER);
		// this.sws = c.getDouble(ImportLvaTask.COLUMN_LVA_SWS);
		this.ects = c.getDouble(ImportLvaTask.COLUMN_LVA_ECTS);
		this.lvaType = c.getString(ImportLvaTask.COLUMN_LVA_TYPE);
		this.code = c.getString(ImportLvaTask.COLUMN_LVA_CODE);
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

	// private void setSWS(double sws) {
	// this.sws = sws;
	// }

	// public double getSWS() {
	// return this.sws;
	// }

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

	public ContentValues getContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(KusssContentContract.Lva.LVA_COL_TITLE, getTitle());
		cv.put(KusssContentContract.Lva.LVA_COL_ECTS, getEcts());
		cv.put(KusssContentContract.Lva.LVA_COL_LVANR, getLvaNr());
		cv.put(KusssContentContract.Lva.LVA_COL_SKZ, getSKZ());
		cv.put(KusssContentContract.Lva.LVA_COL_CODE, getCode());
		cv.put(KusssContentContract.Lva.LVA_COL_TEACHER, getTeacher());
		cv.put(KusssContentContract.Lva.LVA_COL_TERM, getTerm());
		cv.put(KusssContentContract.Lva.LVA_COL_TYPE, getLvaType());

		return cv;
	}

	public String getKey() {
		return getKey(this.term, this.lvaNr);
	}

	public static String getKey(String term, String lvaNr) {
		return term + "-" + lvaNr;
	}

	public String getCode() {
		return this.code;
	}

}