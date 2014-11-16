package org.voidsink.anewjkuapp.utils;

import android.graphics.Color;

/**
 * Created by paul on 05.11.2014.
 */
public class Consts {

    public static final int COLOR_DEFAULT_EXAM = Color.rgb(240, 149, 0);
    public static final int COLOR_DEFAULT_LVA = Color.rgb(43, 127, 194);

    public static final String SYNC_SHOW_PROGRESS = "showProgress";

    public static final String ARG_FEED_URL = "feedURL";
    public static final String ARG_FEED_ID = "feedID";

    public static final Integer FEED_ID_OEH = 1;
    public static final Integer FEED_ID_REWI = 2;
    public static final Integer FEED_ID_SOWI = 3;
    public static final Integer FEED_ID_TNF = 4;
    public static final Integer FEED_ID_MED = 5;

    public static final String FEED_URL_OEH = "http://oeh.jku.at/news.xml";
    public static final String FEED_URL_REWI = "http://oeh.jku.at/news/49/news.xml";
    public static final String FEED_URL_SOWI = "http://oeh.jku.at/news/23/news.xml";
    public static final String FEED_URL_TNF = "http://oeh.jku.at/news/31/news.xml";
    public static final String FEED_URL_MED = "http://oeh.jku.at/news/58/news.xml";

    //strings for logging of screens

    public static final String SCREEN_CALENDAR = "/calendar";
    public static final String SCREEN_EXAMS = "/exams";
    public static final String SCREEN_GRADES = "/grades";
    public static final String SCREEN_LVAS = "/lvas";
    public static final String SCREEN_STAT = "/stat";
    public static final String SCREEN_MENSA = "/mensa";
    public static final String SCREEN_MAP = "/map";
    public static final String SCREEN_OEH_INFO = "/info";
    public static final String SCREEN_OEH_RIGHTS = "/rights";

    public static final String SCREEN_ABOUT = "/about";
    public static final String SCREEN_SETTINGS = "/settings";

    public static final String SCREEN_SETTINGS_DASHCLOCK = "/dashclock";
    public static final String SCREEN_LOGIN = "/login";

    private Consts() {
    }
}
