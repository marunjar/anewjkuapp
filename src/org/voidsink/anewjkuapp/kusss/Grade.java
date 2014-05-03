package org.voidsink.anewjkuapp.kusss;

import android.annotation.SuppressLint;
import android.graphics.Color;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;

public enum Grade {
	G1, G2, G3, G4, G5;

	private static final int COLOR_1 = Color.rgb(0, 240, 0);
	private static final int COLOR_2 = Color.rgb(160, 240, 0);
	private static final int COLOR_3 = Color.rgb(240, 240, 0);
	private static final int COLOR_4 = Color.rgb(240, 120, 0);
	private static final int COLOR_5 = Color.rgb(240, 0, 0);
	
	public int getStringResID() {
		switch (this) {
		case G1:
			return R.string.grade_1;
		case G2:
			return R.string.grade_2;
		case G3:
			return R.string.grade_3;
		case G4:
			return R.string.grade_4;
		case G5:
			return R.string.grade_5;
		default:
			return R.string.grade_unknown;
		}
	}
	
	public int getColor() {
		switch (this) {
		case G1:
			return COLOR_1;
		case G2:
			return COLOR_2;
		case G3:
			return COLOR_3;
		case G4:
			return COLOR_4;
		case G5:
			return COLOR_5;
		default:
			return CalendarUtils.COLOR_DEFAULT_LVA;
		}
	}

	@SuppressLint("DefaultLocale")
	public static Grade parseGrade(String text) {
		text = text.trim().toLowerCase();
		if (text.equals("sehr gut")) {
			return G1;
		} else if (text.equals("gut")) {
			return G2;
		} else if (text.equals("befriedigend")) {
			return G3;
		} else if (text.equals("genügend")) {
			return G4;
		} else if (text.equals("nicht genügend")) {
			return G5;
		} else {
			return null;
		}
	}

	public static Grade parseGradeType(int ordinal) {
		return Grade.values()[ordinal];
	}
}
