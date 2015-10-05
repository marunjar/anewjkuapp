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

import android.annotation.SuppressLint;

import org.voidsink.anewjkuapp.R;

public enum AssessmentType {
    INTERIM_COURSE_ASSESSMENT, FINAL_COURSE_ASSESSMENT, RECOGNIZED_COURSE_CERTIFICATE, RECOGNIZED_EXAM, RECOGNIZED_ASSESSMENT, FINAL_EXAM, ALL, NONE_AVAILABLE;

    public int getStringResID() {
        switch (this) {
            case INTERIM_COURSE_ASSESSMENT:
                return R.string.grade_type_interim_ca;
            case FINAL_COURSE_ASSESSMENT:
                return R.string.grade_type_final_ca;
            case RECOGNIZED_COURSE_CERTIFICATE:
                return R.string.grade_type_recognized_cc;
            case RECOGNIZED_EXAM:
                return R.string.grade_type_recognized_exam;
            case RECOGNIZED_ASSESSMENT:
                return R.string.grade_type_recognized_a;
            case FINAL_EXAM:
                return R.string.grade_type_final_exam;
            case ALL:
                return R.string.grade_type_all;
            case NONE_AVAILABLE:
                return R.string.grade_type_none_available;
            default:
                return R.string.grade_type_unknown;
        }
    }

    @SuppressLint("DefaultLocale")
    public static AssessmentType parseGradeType(String text) {
        text = text.trim().toLowerCase();

        switch (text) {
            case "vorläufige lehrveranstaltungsbeurteilungen":
            case "interim course assessments":
                return INTERIM_COURSE_ASSESSMENT;
            case "lehrveranstaltungsbeurteilungen":
            case "course assessments":
                return FINAL_COURSE_ASSESSMENT;
            case "sonstige beurteilungen":
            case "recognized course certificates (ilas)":
                return RECOGNIZED_COURSE_CERTIFICATE;
            case "anerkannte beurteilungen":
            case "recognized assessments":
                return RECOGNIZED_ASSESSMENT;
            case "prüfungen":
            case "exams":
                return RECOGNIZED_EXAM;
            case "anerkannte prüfungen":
            case "recognized exams":
                return RECOGNIZED_EXAM;
            default:
                return null;
        }
    }

    public static AssessmentType parseAssessmentType(int ordinal) {
        return AssessmentType.values()[ordinal];
    }
}
