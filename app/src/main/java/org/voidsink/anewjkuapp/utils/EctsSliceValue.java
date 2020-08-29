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
 */

package org.voidsink.anewjkuapp.utils;

import org.voidsink.anewjkuapp.kusss.Grade;

import lecho.lib.hellocharts.model.SliceValue;

public class EctsSliceValue extends SliceValue {

    private final float mEcts;
    private final Grade mGrade;

    public EctsSliceValue(float val, int color) {
        this(val, val, Grade.G3, color);
    }

    public EctsSliceValue(float val, float ects, Grade grade, int color) {
        super(val, color);
        if (grade != null) {
            this.setColor(grade.getColor());
        }
        this.mEcts = ects;
        this.mGrade = grade;
    }

    public float getEcts() {
        return mEcts;
    }

    public Grade getGrade() {
        return mGrade;
    }
}