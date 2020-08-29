/*
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
 *
 */

package org.voidsink.anewjkuapp.analytics;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import java.util.List;

enum PlayServiceStatus {
    PS_INSTALLED, PS_NOT_AVAILABLE, PS_REPAIRABLE
}

interface IAnalytics {

    void init(Application app);

    void sendException(Context c, Exception e, boolean fatal, List<String> additionalData);

    void sendScreen(Activity activity, String screenName);

    void sendButtonEvent(String label);

    void sendPreferenceChanged(String key, String value);

    void setEnabled(boolean enabled);

    PlayServiceStatus getPsStatus();
}


