/*******************************************************************************
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
 ******************************************************************************/

package org.voidsink.anewjkuapp.kusss;

public class LvaWithGrade {

    private final Course course;
    private final Assessment grade;

    public LvaWithGrade(Course course, Assessment grade) {
        this.course = course;
        this.grade = grade;
    }

    public LvaState getState() {
        if (this.grade == null) {
            return LvaState.OPEN;
        }
        if (this.grade.getGrade() == Grade.G5) {
            return LvaState.OPEN;
        }
        return LvaState.DONE;
    }

    public Course getCourse() {
        return course;
    }

    public Assessment getGrade() {
        return grade;
    }

}
