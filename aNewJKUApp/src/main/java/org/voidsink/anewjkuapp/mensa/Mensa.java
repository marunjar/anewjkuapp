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

package org.voidsink.anewjkuapp.mensa;

import org.apache.commons.lang.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Mensa {

    private String name;
    private List<MensaDay> days;

    public Mensa(String name) {
        this.name = name;
        this.days = new ArrayList<MensaDay>();
    }

    public void addDay(MensaDay menuDay) {
        if (menuDay != null) {
            this.days.add(menuDay);
            menuDay.setMensa(this);
        }
    }

    public boolean isEmpty() {
        return this.days.size() == 0;
    }

    public List<MensaDay> getDays() {
        return this.days;
    }

    public String getName() {
        return this.name;
    }

    public MensaDay getDay(Date now) {
        for (MensaDay mensaDay : this.days) {
            if (!mensaDay.isEmpty()
                    && DateUtils.isSameDay(now, mensaDay.getDate())) {
                return mensaDay;
            }
        }
        return null;
    }

}
