/*
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2015 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.mensa.IDay;
import org.voidsink.anewjkuapp.mensa.IMensa;
import org.voidsink.anewjkuapp.mensa.IMenu;

public class MensaMenuItem implements MensaItem {

    private final IMensa mensa;
    private final IDay day;
    private final IMenu menu;

    public MensaMenuItem(IMensa mensa, IDay day, IMenu menu) {
        this.mensa = mensa;
        this.day = day;
        this.menu = menu;
    }

    @Override
    public int getType() {
        return MensaItem.TYPE_MENU;
    }

    @Override
    public IMensa getMensa() {
        return this.mensa;
    }

    @Override
    public IDay getDay() {
        return this.day;
    }

    @Override
    public IMenu getMenu() {
        return this.menu;
    }
}
