package org.voidsink.anewjkuapp;

import java.util.Date;

import org.voidsink.anewjkuapp.kusss.Grade;
import org.voidsink.anewjkuapp.kusss.GradeType;

import android.database.Cursor;

public class GradeListGrade implements GradeListItem {

	private int lvaNr;
	private String term;
	private Date date;
	private String title;
	private Grade grade;
	private GradeType gradeType;
	private int skz;

	public GradeListGrade(Cursor c) {
		this.lvaNr = c.getInt(ImportGradeTask.COLUMN_GRADE_LVANR);
		this.term = c.getString(ImportGradeTask.COLUMN_GRADE_TERM);
		this.date = new Date(c.getLong(ImportGradeTask.COLUMN_GRADE_DATE));
		this.gradeType = GradeType.parseGradeType(c
				.getInt(ImportGradeTask.COLUMN_GRADE_TYPE));
		this.grade = Grade.parseGradeType(c
				.getInt(ImportGradeTask.COLUMN_GRADE_GRADE));
		this.skz = c.getInt(ImportGradeTask.COLUMN_GRADE_SKZ);
		this.title = c.getString(ImportGradeTask.COLUMN_GRADE_TITLE);
	}

	@Override
	public boolean isGrade() {
		return true;
	}

	@Override
	public int getType() {
		return GRADE_TYPE;
	}

	public GradeType getGradeType() {
		return this.gradeType;
	}

	public String getTitle() {
		return this.title;
	}

	public int getLvaNr() {
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

	public Grade getGrade() {
		return this.grade;
	}

}
