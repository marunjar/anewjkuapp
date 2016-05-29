/*
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp.kusss;

import android.graphics.Color;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.utils.AppUtils;

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
    private final int value;

    Grade(int value, boolean isPositive, boolean isNumber) {
        this.value = value;
        this.isPositive = isPositive;
        this.isNumber = isNumber;
    }

    public static Grade parseGrade(String text) {
        text = text.trim().toLowerCase();
        switch (text) {
            case "sehr gut":
                return G1;
            case "gut":
                return G2;
            case "befriedigend":
                return G3;
            case "genügend":
                return G4;
            case "nicht genügend":
                return G5;
            case "mit erfolg teilgenommen":
                return GET;
            case "bestanden":
                return GB;
            case "mit auszeichnung bestanden":
                return GAB;
            default:
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
                return AppUtils.getRandomColor();
        }
    }

    public boolean isNumber() {
        return isNumber;
    }

    public boolean isPositive() {
        return isPositive;
    }
}
