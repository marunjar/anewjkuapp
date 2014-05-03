package org.voidsink.anewjkuapp.kusss;

import org.voidsink.anewjkuapp.R;

import android.annotation.SuppressLint;

public enum GradeType {
	INTERIM_COURSE_ASSESSMENT, FINAL_COURSE_ASSESSMENT, RECOGNIZED_COURSE_CERTIFICATE, RECOGNIZED_EXAM;

	public int getStringResID() {
		switch (this) {
		case INTERIM_COURSE_ASSESSMENT:
			return R.string.grade_type_interim;
		case FINAL_COURSE_ASSESSMENT:
			return R.string.grade_type_final;
		case RECOGNIZED_COURSE_CERTIFICATE:
			return R.string.grade_type_recognized_ca;
		case RECOGNIZED_EXAM:
			return R.string.grade_type_recognized_exam;
		default:
			return R.string.grade_type_unknown;
		}
	}

	@SuppressLint("DefaultLocale")
	public static GradeType parseGradeType(String text) {
		text = text.trim().toLowerCase();
		if (text.equals("vorläufige lehrveranstaltungsbeurteilungen")
				|| text.equals("interim course assessments")) {
			return INTERIM_COURSE_ASSESSMENT;
		} else if (text.equals("lehrveranstaltungsbeurteilungen")
				|| text.equals("course assessments")) {
			return FINAL_COURSE_ASSESSMENT;
		} else if (text.equals("anerkannte lehrveranstaltungsbeurteilungen")
				|| text.equals("recognized course certificates")) {
			return RECOGNIZED_COURSE_CERTIFICATE;
		} else if (text.equals("anerkannte prüfungen")
				|| text.equals("recognized exams")) {
			return RECOGNIZED_EXAM;
		} else {
			return null;
		}
	}

	public static GradeType parseGradeType(int ordinal) {
		return GradeType.values()[ordinal];
	}
}
