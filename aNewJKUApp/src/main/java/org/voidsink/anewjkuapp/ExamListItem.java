package org.voidsink.anewjkuapp;

public interface ExamListItem {

	public static final int EXAM_TYPE = 0;
	
	public int getType();
	public boolean mark();
	public boolean isExam();
}
