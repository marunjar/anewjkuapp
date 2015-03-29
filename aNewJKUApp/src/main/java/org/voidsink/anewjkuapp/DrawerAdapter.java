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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.voidsink.anewjkuapp.base.BaseArrayAdapter;

public class DrawerAdapter extends BaseArrayAdapter<DrawerItem> {

    private LayoutInflater inflater;

    public DrawerAdapter(Context context) {
        super(context, R.layout.drawer_menu_item);

        this.inflater = LayoutInflater.from(context);
    }

    public DrawerAdapter(Context context, DrawerItem[] objects) {
        super(context, R.layout.drawer_menu_item, objects);

        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        DrawerItem menuItem = this.getItem(position);
        if (menuItem.isSectionHeader()) {
            view = getSectionView(convertView, parent, menuItem, position == 0);
        } else {
            view = getItemView(convertView, parent, menuItem);
        }
        return view;
    }

    private View getItemView(View convertView, ViewGroup parent,
                             DrawerItem drawerMenuItem) {
        DrawerMenuItem menuItem = (DrawerMenuItem) drawerMenuItem;
        DrawerMenuItemHolder menuItemHolder = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.drawer_menu_item, parent,
                    false);
            menuItemHolder = new DrawerMenuItemHolder();
            menuItemHolder.labelView = (TextView) convertView
                    .findViewById(R.id.drawer_menu_item_label);
            menuItemHolder.iconView = (ImageView) convertView
                    .findViewById(R.id.drawer_menu_item_icon);

            convertView.setTag(menuItemHolder);
        }

        if (menuItemHolder == null) {
            menuItemHolder = (DrawerMenuItemHolder) convertView.getTag();
        }

        menuItemHolder.labelView.setText(menuItem.getLabel(getContext()));
        menuItemHolder.iconView.setImageResource(menuItem.getIconResID());

        return convertView;
    }

    private View getSectionView(View convertView, ViewGroup parent,
                                DrawerItem menuItem, boolean isFirstItem) {
        DrawerMenuSection menuSection = (DrawerMenuSection) menuItem;
        DrawerMenuSectionHolder menuSectionHolder = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.drawer_menu_section,
                    parent, false);
            menuSectionHolder = new DrawerMenuSectionHolder();
            menuSectionHolder.labelView = (TextView) convertView
                    .findViewById(R.id.navmenusection_label);
            menuSectionHolder.separator = convertView.findViewById(R.id.separator);
            convertView.setTag(menuSectionHolder);
        }

        if (menuSectionHolder == null) {
            menuSectionHolder = (DrawerMenuSectionHolder) convertView.getTag();
        }

        String mLabel = menuSection.getLabel(getContext());

        if (TextUtils.isEmpty(mLabel)) {
            menuSectionHolder.labelView.setVisibility(View.GONE);
            menuSectionHolder.separator.setVisibility(View.VISIBLE);
        } else {
            menuSectionHolder.labelView.setVisibility(View.VISIBLE);
            menuSectionHolder.labelView.setText(mLabel);

            if (isFirstItem) {
                menuSectionHolder.separator.setVisibility(View.GONE);
            } else {
                menuSectionHolder.separator.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).getType();
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }

    private static class DrawerMenuItemHolder {
        private TextView labelView;
        private ImageView iconView;
    }

    private class DrawerMenuSectionHolder {
        private TextView labelView;
        public View separator;
    }

}
