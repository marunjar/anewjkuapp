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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MensaDay implements IDay {

    private Date date;
    private List<IMenu> menus;
    private boolean isModified = false;
    private Mensa mensa;

    public MensaDay(Date date) {
        this.date = date;
        this.menus = new ArrayList<>();
    }

    public MensaDay(JSONObject jsonDay) {
        try {
            final SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            this.date = df.parse(jsonDay.getString("date"));
        } catch (ParseException | JSONException e) {
            this.date = null;
        }
        this.menus = new ArrayList<>();
    }

    public void addMenu(MensaMenu menu) {
        this.menus.add(menu);
    }

    public List<IMenu> getMenus() {
        return this.menus;
    }

    public boolean isEmpty() {
        return (this.date == null) || (this.menus.size() == 0);
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
        this.isModified = true;
    }

    public boolean isModified() {
        return this.isModified;
    }

    public void setMensa(Mensa mensa) {
        this.mensa = mensa;
    }

    public Mensa getMensa() {
        return mensa;
    }
}
