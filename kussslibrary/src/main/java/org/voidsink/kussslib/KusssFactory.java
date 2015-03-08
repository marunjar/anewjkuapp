package org.voidsink.kussslib;

import java.util.Date;

public interface KusssFactory {

	public Course getCourse(Term term, String courseId, String title, int cid,
			String lecturer, double ects, double sws, CourseType courseType,
			String classCode);

	public Curricula getCurricula(int cid, String title, String uni,
			Date dtStart, Date dtEnd, boolean isStandard, boolean steopDone,
			boolean active);

	public Assessment getAssessment(Date date, String title, Term term,
			String courseId, Grade grade, int cid,
			AssessmentType assessmentType, String classCode, double ects,
			double sws, CourseType courseType);

	public Exam getExam(String courseId, Term term, Date dtStart, Date dtEnd,
			String location, String title, int cid, String description,
			String info, boolean isRegistered, int maxParticipants,
			int participants, Date registrationDtStart, Date registrationDtEnd,
			Date unregistrationDt);

}
