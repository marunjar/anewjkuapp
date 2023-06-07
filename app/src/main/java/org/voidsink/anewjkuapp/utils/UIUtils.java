/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2023 Paul "Marunjar" Pretsch
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

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NavUtils;

import org.voidsink.anewjkuapp.PreferenceHelper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.calendar.DayOfMonthDrawable;
import org.voidsink.anewjkuapp.kusss.Assessment;

import java.util.Calendar;

public class UIUtils {

    private UIUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean handleUpNavigation(Activity activity, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (activity instanceof AppCompatActivity) {
                ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
                if (actionBar != null && (actionBar.getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) != 0) {
                    // app icon in action bar clicked; goto parent activity.
                    NavUtils.navigateUpFromSameTask(activity);
                    return true;
                }
            }
            return false;
        }
        return false;
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
        activity.setTheme(R.style.AppTheme);
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
        DayOfMonthDrawable today = null;

        // Reuse current drawable if possible
        Drawable currentDrawable = icon.findDrawableByLayerId(R.id.today_icon_day);
        if (currentDrawable instanceof DayOfMonthDrawable) {
            today = (DayOfMonthDrawable) currentDrawable;
        } else if (c != null) {
            today = new DayOfMonthDrawable(c);
        }
        // Set the day and update the icon
        if (today != null) {
            today.setDayOfMonth(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            icon.mutate();
            icon.setDrawableByLayerId(R.id.today_icon_day, today);
        }
    }

    public static void setDefaultNightMode(@NonNull Context context) {
        AppCompatDelegate.setDefaultNightMode(PreferenceHelper.getNightMode(context));
    }
}
