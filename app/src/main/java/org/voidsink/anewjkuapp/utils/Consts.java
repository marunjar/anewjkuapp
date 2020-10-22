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

public interface Consts {

    String ARG_WORKER_CAL_COURSES = "UPDATE_CAL_LVA";
    String ARG_WORKER_CAL_EXAM = "UPDATE_CAL_EXAM";
    String ARG_WORKER_KUSSS_CURRICULA = "UPDATE_KUSSS_CURRICULA";
    String ARG_WORKER_KUSSS_COURSES = "UPDATE_KUSSS_COURSES";
    String ARG_WORKER_KUSSS_ASSESSMENTS = "UPDATE_KUSSS_ASSESSMENTS";
    String ARG_WORKER_KUSSS_EXAMS = "UPDATE_KUSSS_EXAMS";
    String ARG_WORKER_POI = "UPDATE_POI";

    String ARG_TERMS = "TERMS";

    String ARG_FRAGMENT_TITLE = "FRAGMENT_TITLE";
    String ARG_FRAGMENT_ID = "FRAGMENT_ID";
    String ARG_FRAGMENT_TAG = "show_fragment";

    String SYNC_SHOW_PROGRESS = "showProgress";

    String ARG_CALENDAR_NOW = "cal_now";
    String ARG_CALENDAR_THEN = "cal_then";

    String ARG_TAB_FRAGMENT_POS = "st_pos";

    String ARG_FILENAME = "FILENAME";
    String ARG_IS_DEFAULT = "IS_DEFAULT";

    String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    String ARG_AUTH_TYPE = "AUTH_TYPE";
    String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    //strings for logging of screens

    String SCREEN_CALENDAR = "/calendar";
    String SCREEN_CALENDAR_2 = "/calendar2";
    String SCREEN_EXAMS = "/exams";
    String SCREEN_ASSESSMENTS = "/assessments";
    String SCREEN_COURSES = "/courses";
    String SCREEN_STAT = "/stat";
    String SCREEN_MENSA = "/mensa";
    String SCREEN_MAP = "/map";
    String SCREEN_OEH_INFO = "/info";
    String SCREEN_OEH_RIGHTS = "/rights";
    String SCREEN_CURRICULA = "/curricula";

    String SCREEN_ABOUT = "/about";
    String SCREEN_SETTINGS = "/settings";
    String SCREEN_SETTINGS_KUSSS = "/settings/kusss";
    String SCREEN_SETTINGS_APP = "/settings/app";
    String SCREEN_SETTINGS_TIMETABLE = "/settings/timetable";

    String SCREEN_SETTINGS_DASHCLOCK = "/settings/dashclock";
    String SCREEN_LOGIN = "/login";
    String SCREEN_DASHCLOCK = "/dashclock";

    // Loader IDs
    int LOADER_ID_COURSES = 1;
    int LOADER_ID_ASSESSMENTS = 2;

    String CHANNEL_ID_GRADES = BuildConfig.APPLICATION_ID + ".GRADES";
    String CHANNEL_ID_EXAMS = BuildConfig.APPLICATION_ID + ".EXAMS";
    String CHANNEL_ID_DEFAULT = BuildConfig.APPLICATION_ID + ".DEFAULT";

    // URLs
    String MENSA_MENU_JKU = "https://www.mensen.at/";
    String MENSA_MENU_CLASSIC = "https://menu.mensen.at/index/index/locid/1";
    String MENSA_MENU_CHOICE = "https://menu.mensen.at/index/index/locid/1";
    String MENSA_MENU_TAGESTELLER = "https://menu.mensen.at/index/index/locid/1";
    String MENSA_MENU_KHG = "https://www.dioezese-linz.at/institution/807510/essen/menueplan";
    String MENSA_MENU_RAAB = "https://www.mittag.at/w/raabmensa";
}
