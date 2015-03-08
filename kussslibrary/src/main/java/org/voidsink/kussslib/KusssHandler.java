package org.voidsink.kussslib;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

public interface KusssHandler {

	public boolean login(String user, String passwort);
	public boolean logout();
	public boolean isLoggedIn();
	
	public Calendar getEvents(EventType eventType, CalendarBuilder calendarBuilder); 
	public Calendar getEvents(EventType eventType);
	
	public List<Course> getCourses(List<Term> terms);
	
	public List<Assessment> getAssessments();
	public List<Exam> getExams(); // old: getNewExams
	public List<Exam> getExamsByCourses(List<Course> lvas); // old: getNewExamsByCourse
	public List<Curricula> getCurricula(); // old: getStudies
	
	public void setExceptionListener(IExceptionListener listener);
	
	public static interface IExceptionListener {
		public void onExceptionOccured(Exception e, boolean fatal);
	}
	
}
