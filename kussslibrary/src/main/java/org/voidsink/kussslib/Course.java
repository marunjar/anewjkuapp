package org.voidsink.kussslib;

public interface Course {
	public Term getTerm();
	public int getCid();
	public String getClassCode();
	public CourseType getCourseType();
	public String getTitle();
	public String getCourseId(); 
	public String getLecturer();
	public double getEcts();
	public double getSws();
}