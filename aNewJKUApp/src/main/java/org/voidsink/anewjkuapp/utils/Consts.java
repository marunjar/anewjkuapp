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

package org.voidsink.anewjkuapp.utils;

import org.voidsink.anewjkuapp.BuildConfig;

public class Consts {

    public static final String ARG_WORKER_CAL_COURSES = "UPDATE_CAL_LVA";
    public static final String ARG_WORKER_CAL_EXAM = "UPDATE_CAL_EXAM";
    public static final String ARG_WORKER_KUSSS_CURRICULA = "UPDATE_KUSSS_CURRICULA";
    public static final String ARG_WORKER_KUSSS_COURSES = "UPDATE_KUSSS_COURSES";
    public static final String ARG_WORKER_KUSSS_ASSESSMENTS = "UPDATE_KUSSS_ASSESSMENTS";
    public static final String ARG_WORKER_KUSSS_EXAMS = "UPDATE_KUSSS_EXAMS";
    public static final String ARG_WORKER_POI = "UPDATE_POI";

    public static final String ARG_TERMS = "TERMS";

    public static final String ARG_FRAGMENT_TITLE = "FRAGMENT_TITLE";
    public static final String ARG_FRAGMENT_ID = "FRAGMENT_ID";
    public static final String ARG_FRAGMENT_TAG = "show_fragment";

    public static final String SYNC_SHOW_PROGRESS = "showProgress";

    public static final String ARG_CALENDAR_NOW = "cal_now";
    public static final String ARG_CALENDAR_THEN = "cal_then";

    public static final String ARG_TAB_FRAGMENT_POS = "st_pos";

    public static final String ARG_FILENAME = "FILENAME";
    public static final String ARG_IS_DEFAULT = "IS_DEFAULT";

    public static final String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public static final String ARG_AUTH_TYPE = "AUTH_TYPE";
    public static final String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public static final String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    //strings for logging of screens

    public static final String SCREEN_CALENDAR = "/calendar";
    public static final String SCREEN_CALENDAR_2 = "/calendar2";
    public static final String SCREEN_EXAMS = "/exams";
    public static final String SCREEN_ASSESSMENTS = "/assessments";
    public static final String SCREEN_COURSES = "/courses";
    public static final String SCREEN_STAT = "/stat";
    public static final String SCREEN_MENSA = "/mensa";
    public static final String SCREEN_MAP = "/map";
    public static final String SCREEN_OEH_INFO = "/info";
    public static final String SCREEN_OEH_RIGHTS = "/rights";
    public static final String SCREEN_CURRICULA = "/curricula";

    public static final String SCREEN_ABOUT = "/about";
    public static final String SCREEN_SETTINGS = "/settings";
    public static final String SCREEN_SETTINGS_KUSSS = "/settings/kusss";
    public static final String SCREEN_SETTINGS_APP = "/settings/app";
    public static final String SCREEN_SETTINGS_TIMETABLE = "/settings/timetable";

    public static final String SCREEN_SETTINGS_DASHCLOCK = "/settings/dashclock";
    public static final String SCREEN_LOGIN = "/login";
    public static final String SCREEN_DASHCLOCK = "/dashclock";

    // Loader IDs
    public static final int LOADER_ID_COURSES = 1;
    public static final int LOADER_ID_ASSESSMENTS = 2;

    public static final String CHANNEL_ID_GRADES = BuildConfig.APPLICATION_ID + ".GRADES";
    public static final String CHANNEL_ID_EXAMS = BuildConfig.APPLICATION_ID + ".EXAMS";
    public static final String CHANNEL_ID_DEFAULT = BuildConfig.APPLICATION_ID + ".DEFAULT";

    // URLs
    public static final String MENSA_MENU_JKU = "https://www.mensen.at/";
    public static final String MENSA_MENU_CLASSIC = "https://menu.mensen.at/index/index/locid/1";
    public static final String MENSA_MENU_CHOICE = "https://menu.mensen.at/index/index/locid/1";
    public static final String MENSA_MENU_TAGESTELLER = "https://menu.mensen.at/index/index/locid/1";
    public static final String MENSA_MENU_KHG = "https://www.dioezese-linz.at/institution/807510/essen/menueplan";
    public static final String MENSA_MENU_RAAB = "http://www.sommerhaus-hotel.at/de/linz#speiseplan";

    private Consts() {
        throw new UnsupportedOperationException();
    }
}
