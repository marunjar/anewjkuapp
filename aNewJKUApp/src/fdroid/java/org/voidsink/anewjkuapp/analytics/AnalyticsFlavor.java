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

package org.voidsink.anewjkuapp.analytics;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class AnalyticsFlavor implements IAnalytics {


    private static final String TAG = AnalyticsFlavor.class.getSimpleName();

    @Override
    public void init(Application app) {
        Log.d(TAG, "init");
    }

    @Override
    public void sendException(Context c, Exception e, boolean fatal, String additionalData) {
        Log.d(TAG, String.format("%s (%s)", e.toString(), additionalData));
    }

    @Override
    public void sendScreen(Context c, String screenName) {
        Log.d(TAG, String.format("screen: %s", screenName));
    }

    @Override
    public void sendButtonEvent(String label) {
        Log.d(TAG, String.format("buttonEvent: %s", label));
    }
}
