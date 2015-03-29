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

package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.base.BaseArrayAdapter;

public class PoiAdapter extends BaseArrayAdapter<Poi> {

    public PoiAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public PoiAdapter(Context context) {
        this(context, android.R.layout.simple_list_item_2);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PoiHolder poiHolder = null;

        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_2,
                    parent, false);
            poiHolder = new PoiHolder();
            poiHolder.text1 = (TextView) convertView
                    .findViewById(android.R.id.text1);
            poiHolder.text2 = (TextView) convertView
                    .findViewById(android.R.id.text2);
            convertView.setTag(poiHolder);
        }

        if (poiHolder == null) {
            poiHolder = (PoiHolder) convertView.getTag();
        }

        Poi poi = getItem(position);
        poiHolder.text1.setText(poi.getName());
        poiHolder.text2.setText(poi.getDescr());

        return convertView;
    }

    private final class PoiHolder {
        TextView text1;
        TextView text2;
    }
}
