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

package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.mensa.IDay;
import org.voidsink.anewjkuapp.mensa.IMensa;
import org.voidsink.anewjkuapp.mensa.IMenu;

public class MensaInfoItem implements MensaItem {
    private final IDay mDay;
    private final String title;
    private final String descr;
    private final IMensa mMensa;

    public MensaInfoItem(IMensa mensa, IDay day, String title, String descr) {
        this.mDay = day;
        this.descr = descr;
        this.mMensa = mensa;
        this.title = title;
    }

    @Override
    public int getType() {
        return MensaItem.TYPE_INFO;
    }

    @Override
    public IDay getDay() {
        return mDay;
    }

    @Override
    public IMenu getMenu() {
        return null;
    }

    @Override
    public IMensa getMensa() {
        return mMensa;
    }

    public String getTitle() {
        return title;
    }

    public String getDescr() {
        return descr;
    }
}
