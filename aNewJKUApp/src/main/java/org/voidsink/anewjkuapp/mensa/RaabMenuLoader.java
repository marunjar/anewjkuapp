/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2018 Paul "Marunjar" Pretsch
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

import android.content.Context;

import org.voidsink.anewjkuapp.R;

import static org.voidsink.anewjkuapp.utils.Consts.MENSA_MENU_RAAB;

public class RaabMenuLoader extends BaseMenuLoader implements MenuLoader {

    @Override
    public IMensa getMensa(Context context) {
        return new Mensa(Mensen.MENSA_RAAB, context.getString(R.string.mensa_title_raab));
    }

    @Override
    protected String getCacheKey() {
        return Mensen.MENSA_RAAB;
    }

    @Override
    protected String getUrl() {
        return MENSA_MENU_RAAB;
    }
}
