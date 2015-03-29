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
import org.voidsink.anewjkuapp.MensaItem;

public class MensaMenu implements MensaItem {

    private String name;
    private String soup;
    private String meal;
    private double price;
    private double priceBig;
    private double oehBonus;
    private MensaDay day;

    public MensaMenu(JSONObject jsonObject) {
        try {
            this.name = jsonObject.getString("name").trim();
            if (jsonObject.isNull("soup")) {
                this.soup = "";
            } else {
                this.soup = jsonObject.getString("soup").trim();
            }
            this.meal = jsonObject.getString("meal").trim();
            this.price = jsonObject.getInt("price") / 100f;
            this.priceBig = jsonObject.getInt("priceBig") / 100f;
            this.oehBonus = jsonObject.getInt("oeh_bonus") / 100f;

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return this.name;
    }

    public String getSoup() {
        return this.soup;
    }

    public String getMeal() {
        return this.meal;
    }

    public double getPrice() {
        return this.price;
    }

    public double getPriceBig() {
        return this.priceBig;
    }

    public double getOehBonus() {
        return this.oehBonus;
    }

    @Override
    public int getType() {
        return TYPE_MENU;
    }

    public void setDay(MensaDay day) {
        this.day = day;
    }

    public MensaDay getDay() {
        return day;
    }

    @Override
    public Mensa getMensa() {
        MensaDay day = getDay();
        if (day != null) {
            return day.getMensa();
        }
        return null;
    }
}
