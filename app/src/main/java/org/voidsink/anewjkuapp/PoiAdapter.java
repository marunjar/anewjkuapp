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

package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

public class PoiAdapter extends ArrayAdapter<Poi> {

    public PoiAdapter(@NonNull Context context, @LayoutRes int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public PoiAdapter(@NonNull Context context) {
        this(context, android.R.layout.simple_list_item_2);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        PoiHolder poiHolder = null;

        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_2,
                    parent, false);
            poiHolder = new PoiHolder(convertView);
            convertView.setTag(poiHolder);
        }

        if (poiHolder == null) {
            poiHolder = (PoiHolder) convertView.getTag();
        }

        Poi poi = getItem(position);
        poiHolder.getText1().setText(poi.getName());
        poiHolder.getText2().setText(poi.getDescr());

        return convertView;
    }

    private static final class PoiHolder {
        private final TextView text1;
        private final TextView text2;

        public PoiHolder(View view) {
            text1 = view.findViewById(android.R.id.text1);
            text2 = view.findViewById(android.R.id.text2);
        }

        public TextView getText1() {
            return text1;
        }

        public TextView getText2() {
            return text2;
        }
    }
}
