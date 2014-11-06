package org.voidsink.anewjkuapp.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;

import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.AppUtils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ThemedActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppUtils.applyTheme(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        initActionBar();

        return super.onCreateView(parent, name, context, attrs);
    }

    protected void initActionBar() {
    }

    @Override
    protected void onStart() {
        super.onStart();

        final String screenName = getScreenName();
        if (screenName != null && !screenName.isEmpty()) {
            Analytics.sendScreen(this, screenName);
        }
    }

    /*
     * returns screen name for logging activity
     */
    protected String getScreenName() {
        return null;
    }
}
