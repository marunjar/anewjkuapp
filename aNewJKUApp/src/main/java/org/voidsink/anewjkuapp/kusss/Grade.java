package org.voidsink.anewjkuapp.kusss;

import android.annotation.SuppressLint;
import android.graphics.Color;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;

public enum Grade {
    G1(1, true, true), G2(2, true, true), G3(3, true, true), G4(4, true, true), G5(5, false, true),
    GET(1, true, false), GB(1, true, false), GAB(1, true, false);

    private static final int COLOR_1 = Color.rgb(3, 160, 0);
    private static final int COLOR_2 = Color.rgb(137, 188, 0);
    private static final int COLOR_3 = Color.rgb(203, 203, 0);
    private static final int COLOR_4 = Color.rgb(203, 133, 0);
    private static final int COLOR_5 = Color.rgb(203, 44, 0);

    private final boolean isPositive;
    private final boolean isNumber;
    private int value;

    private Grade(int value, boolean isPositive, boolean isNumber) {
        this.value = value;
        this.isPositive = isPositive;
        this.isNumber = isNumber;
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
        } else if (text.equals("mit erfolg teilgenommen")) {
            return GET;
        } else if (text.equals("bestanden")) {
            return GB;
        } else if (text.equals("mit auszeichnung bestanden")) {
            return GAB;
        } else {
            return null;
        }
    }

    public static Grade parseGradeType(int ordinal) {
        return Grade.values()[ordinal];
    }

    public int getValue() {
        return value;
    }

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
            case GET:
                return R.string.grade_get;
            case GB:
                return R.string.grade_gb;
            case GAB:
                return R.string.grade_gab;
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
            case GET:
                return COLOR_1;
            case GB:
                return COLOR_2;
            case GAB:
                return COLOR_1;
            default:
                return CalendarUtils.COLOR_DEFAULT_LVA;
        }
    }

    public boolean isNumber() {
        return isNumber;
    }

    public boolean isPositive() {
        return isPositive;
    }
}
