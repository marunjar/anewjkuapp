package org.voidsink.anewjkuapp;

import java.util.Date;

import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;

import android.database.Cursor;

public class ExamListExam {

	private String lvaNr;
	private String info;
	private String title;
	private String description;
	private String term;
	private int skz;
	private Date date;
	private String time;
	private String location;
	private boolean isRegistered;

	public ExamListExam(Cursor c, LvaMap map) {
		this.lvaNr = c.getString(ImportExamTask.COLUMN_EXAM_LVANR);
		this.info = c.getString(ImportExamTask.COLUMN_EXAM_INFO);
		this.description = c.getString(ImportExamTask.COLUMN_EXAM_DESCRIPTION);
		this.term = c.getString(ImportExamTask.COLUMN_EXAM_TERM);
		this.date = new Date(c.getLong(ImportExamTask.COLUMN_EXAM_DATE));
		this.time = c.getString(ImportExamTask.COLUMN_EXAM_TIME);
		this.location = c.getString(ImportExamTask.COLUMN_EXAM_LOCATION);
		this.isRegistered = KusssDatabaseHelper.toBool(c.getInt(ImportExamTask.COLUMN_EXAM_IS_REGISTERED));
		Lva lva = map.getLVA(this.term, this.lvaNr);
		if (lva != null) {
			this.title = lva.getTitle();
			this.skz = lva.getSKZ();
		} else {
			// fallback
			this.title = c.getString(ImportExamTask.COLUMN_EXAM_TITLE);
		}
	}

	public boolean mark() {
		return isRegistered();
	}

	public String getTitle() {
		return this.title;
	}

	public String getDescription() {
		return this.description;
	}

	public String getInfo() {
		return this.info;
	}

	public String getLvaNr() {
		return this.lvaNr;
	}

	public String getTerm() {
		return this.term;
	}

	public int getSkz() {
		return this.skz;
	}

	public Date getDate() {
		return this.date;
	}

	public String getTime() {
		return this.time;
	}

	public String getLocation() {
		return this.location;
	}

	public boolean isRegistered() {
		return this.isRegistered;
	}
	
}
