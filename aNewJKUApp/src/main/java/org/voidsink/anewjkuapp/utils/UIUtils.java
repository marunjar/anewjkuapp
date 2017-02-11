/*
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 * <p>
 * Copyright (c) 2014-2017 Paul "Marunjar" Pretsch
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.voidsink.anewjkuapp.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.MenuItem;
import android.widget.TextView;

import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.calendar.DayOfMonthDrawable;
import org.voidsink.anewjkuapp.kusss.Assessment;

public class UIUtils {

    private UIUtils() {
    }

    public static boolean handleUpNavigation(Activity activity, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (activity instanceof AppCompatActivity) {
                    ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
                    if (actionBar != null && (actionBar.getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) != 0) {
                        // app icon in action bar clicked; goto parent activity.
                        NavUtils.navigateUpFromSameTask(activity);
                        return true;
                    }
                }
                return false;
            default:
                return false;
        }
    }

    public static void setTextAndVisibility(TextView v, String text) {
        if (!TextUtils.isEmpty(text)) {
            v.setText(text);
            v.setVisibility(TextView.VISIBLE);
        } else {
            v.setVisibility(TextView.GONE);
        }
    }

    public static void applyTheme(Activity activity) {
        activity.setTheme(getAppThemeResId(activity));
    }

    public static int getAppThemeResId(Context context) {
        if (PreferenceWrapper.getUseLightDesign(context)) {
            return R.style.AppTheme_Light;
        } else {
            return R.style.AppTheme;
        }
    }

    public static String getChipGradeText(Context context, Assessment assessment) {
        if (assessment != null) {
            if (assessment.getGrade().isNumber()) {
                return AppUtils.format(context, "%d", assessment.getGrade().getValue());
            }
            if (assessment.getGrade().isPositive()) {
                return "\u2713";
            } else {
                return "\u2717";
            }
        }
        return "?";
    }

    public static int getChipGradeColor(Assessment assessment) {
        if (assessment != null) {
            return assessment.getGrade().getColor();
        }
        return Color.GRAY;
    }

    public static String getChipGradeEcts(Context context, double ects) {
        return AppUtils.format(context, "%.2f ECTS", ects);
    }

    /**
     * Inserts a drawable with today's day into the today's icon in the option menu
     *
     * @param icon - today's icon from the options menu
     */
    public static void setTodayIcon(LayerDrawable icon, Context c, String timezone) {
        DayOfMonthDrawable today;

        // Reuse current drawable if possible
        Drawable currentDrawable = icon.findDrawableByLayerId(R.id.today_icon_day);
        if (currentDrawable != null && currentDrawable instanceof DayOfMonthDrawable) {
            today = (DayOfMonthDrawable) currentDrawable;
        } else {
            today = new DayOfMonthDrawable(c);
        }
        // Set the day and update the icon
        Time now = new Time();
        now.setToNow();
        now.normalize(false);
        today.setDayOfMonth(now.monthDay);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.today_icon_day, today);
    }
}
