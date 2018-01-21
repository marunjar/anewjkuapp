/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2017 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp;

import android.database.Cursor;

import org.voidsink.anewjkuapp.kusss.Course;
import org.voidsink.anewjkuapp.kusss.Exam;
import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.kusss.Term;

import java.text.ParseException;
import java.util.Date;

public class ExamListExam {

    private final Exam exam;
    private final Course course;

    public ExamListExam(Cursor c, CourseMap map) throws ParseException {
        this.exam = KusssHelper.createExam(c);
        this.course = map.getCourse(exam.getTerm(), exam.getCourseId());
    }

    public String getTitle() {
        if (course != null) {
            return course.getTitle();
        }
        return exam.getTitle();
    }

    public String getDescription() {
        return exam.getDescription();
    }

    public String getInfo() {
        return exam.getInfo();
    }

    public String getCourseId() {
        return exam.getCourseId();
    }

    public Term getTerm() {
        return exam.getTerm();
    }

    public int getCid() {
        if (course != null) {
            return course.getCid();
        }
        return 0;
    }

    public Date getDtStart() {
        return exam.getDtStart();
    }

    public Date getDtEnd() {
        return exam.getDtEnd();
    }

    public String getLocation() {
        return exam.getLocation();
    }

    public boolean isRegistered() {
        return exam.isRegistered();
    }

}
