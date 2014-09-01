package org.voidsink.anewjkuapp.kusss;

public class LvaWithGrade {

	private Lva lva;
	private ExamGrade grade;

	public LvaWithGrade(Lva lva, ExamGrade grade) {
		this.lva = lva;
		this.grade = grade;
	}
	
	public Lva getLva() {
		return lva;
	}
	
	public ExamGrade getGrade() {
		return grade;
	}
	
}
