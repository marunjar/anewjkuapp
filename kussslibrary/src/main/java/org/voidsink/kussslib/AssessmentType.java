package org.voidsink.kussslib;

public enum AssessmentType {
	INTERIM_COURSE_ASSESSMENT, FINAL_COURSE_ASSESSMENT, RECOGNIZED_COURSE_CERTIFICATE, RECOGNIZED_EXAM, RECOGNIZED_ASSESSMENT, FINAL_EXAM, ALL, NONE_AVAILABLE;

	//TODO: ‹berpr¸fen, ob das noch stimmt
	public static AssessmentType parseAssessmentType(String text) {
		text = text.trim().toLowerCase();
		if (text.equals("vorl√§ufige lehrveranstaltungsbeurteilungen")
				|| text.equals("interim course assessments")) {
			return INTERIM_COURSE_ASSESSMENT;
		} else if (text.equals("lehrveranstaltungsbeurteilungen")
				|| text.equals("course assessments")) {
			return FINAL_COURSE_ASSESSMENT;
		} else if (text.equals("sonstige beurteilungen")
				|| text.equals("recognized course certificates (ilas)")) {
			return RECOGNIZED_COURSE_CERTIFICATE;
		} else if (text.equals("anerkannte beurteilungen")
				|| text.equals("recognized assessments")) {
			return RECOGNIZED_ASSESSMENT;
		} else if (text.equals("pr√ºfungen")
				|| text.equals("exams")) {
			return RECOGNIZED_EXAM;
		} else if (text.equals("anerkannte pr√ºfungen")
				|| text.equals("recognized exams")) {
			return RECOGNIZED_EXAM;
		} else {
			return null;
		}		
	}
}

