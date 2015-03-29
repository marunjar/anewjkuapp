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

public class DrawerMenuSection extends BaseDrawerItem {

    public DrawerMenuSection(String label) {
        super(label);
    }

    public DrawerMenuSection(int labelResId) {
        super(labelResId);
    }

    @Override
    public int getIconResID() {
        return 0;
    }

    @Override
    public boolean isSectionHeader() {
        return true;
    }

    @Override
    public boolean updateActionBarTitle() {
        return false;
    }

    @Override
    public int getType() {
        return SECTION_TYPE;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public Class<? extends Fragment> getStartFragment() {
        return null;
    }

}
