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

package org.voidsink.anewjkuapp.analytics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;

import org.jsoup.HttpStatusException;
import org.voidsink.anewjkuapp.utils.AppUtils;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLException;

public class AnalyticsHelper {
    private volatile static IAnalytics sAnalytics;

    private static synchronized IAnalytics getAnalytics() {
        if (sAnalytics == null) {
            synchronized (AnalyticsHelper.class) {
                if (sAnalytics == null) sAnalytics = new AnalyticsFlavor();
            }
        }
        return sAnalytics;
    }

    public static void init(Application app) {
        getAnalytics().init(app);
    }

    public static void sendException(Context context, Exception e, boolean fatal) {
        sendException(context, e, fatal, (String[]) null);
    }

    @SuppressLint("DefaultLocale")
    public static void sendException(Context context, Exception e, boolean assumeFatal, String... additionalData) {
        List<String> additionalDataList = additionalData == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(additionalData));

        boolean fatal = assumeFatal;
        boolean send = true;
        if (e instanceof UnknownHostException || e instanceof NoRouteToHostException || e instanceof SocketTimeoutException || e instanceof ConnectException) {
            fatal = false;
            send = AppUtils.isNetworkAvailable(context, true);
        } else if (e instanceof HttpStatusException) {
            additionalDataList.add(String.format("%d: %s", ((HttpStatusException) e).getStatusCode(), ((HttpStatusException) e).getUrl()));
            send = (((HttpStatusException) e).getStatusCode() != 503) && AppUtils.isNetworkAvailable(context, true);
        } else if (e instanceof SSLException) {
            additionalDataList.add(getAnalytics().getPsStatus().toString());
            send = AppUtils.isNetworkAvailable(context, true);
        }
        if (send) {
            getAnalytics().sendException(context, e, fatal, additionalDataList);
        }
    }

    public static void sendScreen(Activity activity, String screenName) {
        getAnalytics().sendScreen(activity, screenName);
    }

    public static void clearScreen(Activity activity) {
        sendScreen(activity, null);
    }

    public static void eventReloadCourses(Context c) {
        getAnalytics().sendButtonEvent("reload_courses");
    }

    public static void eventReloadEventsCourse(Context c) {
        getAnalytics().sendButtonEvent("reload_events_exam");
    }

    public static void eventReloadEventsExam(Context c) {
        getAnalytics().sendButtonEvent("reload_events_course");
    }

    public static void eventLoadMoreEvents(Context c, long value) {
        getAnalytics().sendButtonEvent("more_events");
    }

    public static void eventReloadExams(Context c) {
        getAnalytics().sendButtonEvent("reload_exams");
    }

    public static void eventReloadAssessments(Context c) {
        getAnalytics().sendButtonEvent("reload_assessments");
    }

    public static void eventReloadCurricula(Context c) {
        getAnalytics().sendButtonEvent("reload_curricula");
    }

    public static void sendPreferenceChanged(String key, String value) {
        getAnalytics().sendPreferenceChanged(key, value);
    }

    public static void setEnabled(boolean enabled) {
        getAnalytics().setEnabled(enabled);
    }
}
