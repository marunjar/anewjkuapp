package org.voidsink.kussslib.impl;

import java.util.Date;

import org.voidsink.kussslib.Assessment;
import org.voidsink.kussslib.AssessmentType;
import org.voidsink.kussslib.Course;
import org.voidsink.kussslib.CourseType;
import org.voidsink.kussslib.Curricula;
import org.voidsink.kussslib.Exam;
import org.voidsink.kussslib.Grade;
import org.voidsink.kussslib.KusssFactory;
import org.voidsink.kussslib.Term;

public class KusssFactoryImpl implements KusssFactory {

	@Override
	public Course getCourse(Term term, String courseId, String title, int cid,
			String lecturer, double ects, double sws, CourseType courseType,
			String classCode) {
		return new CourseImpl(term, courseId, title, cid, lecturer, ects, sws,
				courseType, classCode);
	}

	@Override
	public Curricula getCurricula(int cid, String title, String uni,
			Date dtStart, Date dtEnd, boolean isStandard, boolean steopDone,
			boolean active) {
		return new CurriculaImpl(cid, title, uni, dtStart, dtEnd, isStandard,
				steopDone, active);
	}

	@Override
	public Assessment getAssessment(Date date, String title, Term term,
			String courseId, Grade grade, int cid,
			AssessmentType assessmentType, String classCode, double ects,
			double sws, CourseType courseType) {
		return new AssessmentImpl(date, title, term, courseId, grade, cid,
				assessmentType, classCode, ects, sws, courseType);
	}

	@Override
	public Exam getExam(String courseId, Term term, Date dtStart, Date dtEnd,
			String location, String title, int cid, String description,
			String info, boolean isRegistered, int maxParticipants,
			int participants, Date registrationDtStart, Date registrationDtEnd,
			Date unregistrationDt) {
		return new ExamImpl(courseId, term, dtStart, dtEnd, location, title,
				cid, description, info, isRegistered, maxParticipants,
				participants, registrationDtStart, registrationDtEnd,
				unregistrationDt);

	}

}
