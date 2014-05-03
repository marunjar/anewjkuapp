package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.kusss.GradeType;

public class GradeListType implements GradeListItem {

	private GradeType gradeType;

	public GradeListType(GradeType type) {
		this.gradeType = type;
	}

	public GradeType getGradeType() {
		return this.gradeType;
	}
	
	@Override
	public boolean isGrade() {
		return false;
	}

	@Override
	public int getType() {
		return TYPE_TYPE;
	}

}
