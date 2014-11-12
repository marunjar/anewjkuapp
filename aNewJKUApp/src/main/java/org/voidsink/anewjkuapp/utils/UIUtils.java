package org.voidsink.anewjkuapp.utils;

import android.app.ActionBar;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.TextView;

import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;

public class UIUtils {

    private UIUtils() {
    }

    public static boolean handleUpNavigation(Activity activity, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActionBar actionBar = activity.getActionBar();
                if (actionBar != null && (actionBar.getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP) != 0) {
                    // app icon in action bar clicked; goto parent activity.
                    NavUtils.navigateUpFromSameTask(activity);
                    return true;
                }
            default:
                return false;
        }
    }

    public static void setTextAndVisibility(TextView v, String text) {
        if (text != null && !text.isEmpty()) {
            v.setText(text);
            v.setVisibility(TextView.VISIBLE);
        } else {
            v.setVisibility(TextView.GONE);
        }
    }

    public static void applyTheme(Activity activity) {
        if (PreferenceWrapper.getUseLightDesign(activity)) {
            activity.setTheme(R.style.AppTheme_Light);
        } else {
            activity.setTheme(R.style.AppTheme);
        }
    }
}
