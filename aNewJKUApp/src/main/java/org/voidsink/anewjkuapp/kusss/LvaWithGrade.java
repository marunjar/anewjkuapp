package org.voidsink.anewjkuapp.kusss;

public class LvaWithGrade {

	private Lva lva;
	private ExamGrade grade;

	public LvaWithGrade(Lva lva, ExamGrade grade) {
		this.lva = lva;
		this.grade = grade;
	}

    public LvaState getState() {
        if (this.grade == null) {return LvaState.OPEN;}
        if (this.grade.getGrade() == Grade.G5) {return LvaState.OPEN;}
        return LvaState.DONE;
    }
	
	public Lva getLva() {
		return lva;
	}
	
	public ExamGrade getGrade() {
		return grade;
	}
	
}
