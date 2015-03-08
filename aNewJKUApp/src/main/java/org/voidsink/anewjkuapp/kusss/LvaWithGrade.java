package org.voidsink.anewjkuapp.kusss;

public class LvaWithGrade {

	private Course course;
	private Assessment grade;

	public LvaWithGrade(Course course, Assessment grade) {
		this.course = course;
		this.grade = grade;
	}

    public LvaState getState() {
        if (this.grade == null) {return LvaState.OPEN;}
        if (this.grade.getGrade() == Grade.G5) {return LvaState.OPEN;}
        return LvaState.DONE;
    }
	
	public Course getCourse() {
		return course;
	}
	
	public Assessment getGrade() {
		return grade;
	}
	
}
