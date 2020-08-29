/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2018 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp.kusss;

import net.fortuna.ical4j.model.Calendar;

public class KusssCalendar {

    private final Term mTerm;
    private final boolean mMandatory;
    private final String mUidPrefix;
    private final Calendar mCalendar;
    private final String mName;

    public KusssCalendar(String name, Term term, String uidPrefix, boolean mandatory, Calendar calendar) {
        this.mTerm = term;
        this.mMandatory = mandatory;
        this.mUidPrefix = uidPrefix;
        this.mCalendar = calendar;
        this.mName = name + " " + term.toString();
    }

    public Calendar getCalendar() {
        return mCalendar;
    }

    public boolean isMandatory() {
        return mMandatory;
    }

    public Term getTerm() {
        return mTerm;
    }

    public String getUidPrefix() {
        return mUidPrefix;
    }

    public String getName() {
        return mName;
    }
}
