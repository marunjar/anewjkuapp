package org.voidsink.anewjkuapp.kusss;

import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.voidsink.anewjkuapp.ImportLvaTask;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.LvaListItem;

import android.content.ContentValues;
import android.database.Cursor;

public class Lva implements LvaListItem {

	private final Pattern lvaNrPattern = Pattern.compile("\\d{3}\\.\\d{3}");

	private String term;
	private int lvaNr;
	private String title;
	private int skz;
	private String teacher;
	private double sws;
	private double ects;
	private String lvaType;

	public Lva(String term, int lvaNr) {
		this.term = term;
		this.lvaNr = lvaNr;
	}

	public Lva(String term, Element row) {
		this(term, 0);

		if (row.childNodeSize() >= 10) {
			try {
				String lvaNrText = row.child(2).text();
				if (lvaNrPattern.matcher(lvaNrText).matches()) {
					this.lvaNr = Integer.parseInt(lvaNrText.replace(".", ""));
					setTitle(row.child(3).text());
					setLvaType(row.child(4).text()); // type (UE, ...)
					setTeacher(row.child(8).text()); // Leiter
					setSKZ(Integer.parseInt(row.child(9).text())); // SKZ
					setECTS(Double.parseDouble(row.child(5).text()
							.replace(",", "."))); // ECTS
					setSWS(Double.parseDouble(row.child(6).text()
							.replace(",", "."))); // SWS
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Lva(Cursor c) {
		this.term = c.getString(ImportLvaTask.COLUMN_LVA_TERM);
		this.lvaNr = c.getInt(ImportLvaTask.COLUMN_LVA_LVANR);
		this.title = c.getString(ImportLvaTask.COLUMN_LVA_TITLE);
		this.skz = c.getInt(ImportLvaTask.COLUMN_LVA_SKZ);
		this.teacher = c.getString(ImportLvaTask.COLUMN_LVA_TEACHER);
		this.sws = c.getDouble(ImportLvaTask.COLUMN_LVA_SWS);
		this.ects = c.getDouble(ImportLvaTask.COLUMN_LVA_ECTS);
		this.lvaType = c.getString(ImportLvaTask.COLUMN_LVA_TYPE);
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

	private void setSWS(double sws) {
		this.sws = sws;
	}

	public double getSWS() {
		return this.sws;
	}

	private void setECTS(double ects) {
		this.ects = ects;
	}

	public double getECTS() {
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

	public int getLvaNr() {
		return this.lvaNr;
	}

	public boolean isInitialized() {
		return !term.isEmpty() && lvaNr > 0;
	}

	public ContentValues getContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(KusssContentContract.Lva.LVA_COL_TITLE, getTitle());
		cv.put(KusssContentContract.Lva.LVA_COL_ECTS, getECTS());
		cv.put(KusssContentContract.Lva.LVA_COL_LVANR, getLvaNr());
		cv.put(KusssContentContract.Lva.LVA_COL_SKZ, getSKZ());
		cv.put(KusssContentContract.Lva.LVA_COL_SWS, getSWS());
		cv.put(KusssContentContract.Lva.LVA_COL_TEACHER, getTeacher());
		cv.put(KusssContentContract.Lva.LVA_COL_TERM, getTerm());
		cv.put(KusssContentContract.Lva.LVA_COL_TYPE, getLvaType());

		return cv;
	}

	public String getKey() {
		return getKey(this.term, this.lvaNr);
	}

	public static String getKey(String term, int lvaNr) {
		return term + "-" + lvaNr;
	}

	@Override
	public boolean isLva() {
		return true;
	}

	@Override
	public int getType() {
		return LvaListItem.TYPE_LVA;
	}

}