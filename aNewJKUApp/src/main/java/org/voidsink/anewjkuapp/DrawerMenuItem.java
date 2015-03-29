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

import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.base.BaseDrawerItem;

public class DrawerMenuItem extends BaseDrawerItem {

    private int iconResID;
    private Class<? extends Fragment> startFragment;

    public DrawerMenuItem(String label, int iconResID, Class<? extends Fragment> startFragment) {
        super(label);

        this.iconResID = iconResID;
        this.startFragment = startFragment;
    }

    public DrawerMenuItem(int labelResId, int iconResID, Class<? extends Fragment> startFragment) {
        super(labelResId);

        this.iconResID = iconResID;
        this.startFragment = startFragment;
    }

    public DrawerMenuItem(String label, int iconResID) {
        this(label, iconResID, null);
    }

    public DrawerMenuItem(int labelResId, int iconResID) {
        this(labelResId, iconResID, null);
    }

    public DrawerMenuItem(String label, Class<? extends Fragment> startFragment) {
        this(label, 0, startFragment);
    }

    public DrawerMenuItem(int labelResId, Class<? extends Fragment> startFragment) {
        this(labelResId, 0, startFragment);
    }

    public DrawerMenuItem(String label) {
        this(label, 0);
    }

    public DrawerMenuItem(int labelResId) {
        this(labelResId, 0);
    }

    @Override
    public int getIconResID() {
        return this.iconResID;
    }

    @Override
    public boolean isSectionHeader() {
        return false;
    }

    @Override
    public boolean updateActionBarTitle() {
        return true;
    }

    @Override
    public int getType() {
        return ITEM_TYPE;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    @Override
    public Class<? extends Fragment> getStartFragment() {
        return this.startFragment;
    }

}
