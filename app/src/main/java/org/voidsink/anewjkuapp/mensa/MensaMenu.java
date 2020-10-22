/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2020 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp.mensa;

public class MensaMenu implements IMenu {

    private final String name;
    private final String soup;
    private final String meal;
    private final double price;
    private final double priceBig;
    private final double oehBonus;

    public MensaMenu(String name, String soup, String meal, double price, double priceBig, double oehBonus) {
        this.name = name;
        this.soup = soup;
        this.meal = meal;
        this.price = price;
        this.priceBig = priceBig;
        this.oehBonus = oehBonus;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSoup() {
        return this.soup;
    }

    @Override
    public String getMeal() {
        return this.meal;
    }

    @Override
    public String getDessert() {
        return null;
    }

    @Override
    public double getPrice() {
        return this.price;
    }

    @Override
    public double getPriceBig() {
        return this.priceBig;
    }

    @Override
    public double getOehBonus() {
        return this.oehBonus;
    }
}
