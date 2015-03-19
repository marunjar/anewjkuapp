package org.voidsink.anewjkuapp.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.UIUtils;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ThemedActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UIUtils.applyTheme(this);

        super.onCreate(savedInstanceState);

        initActionBar();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        initActionBar();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected final void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            onInitActionBar(actionBar);
        }
    }

    protected void onInitActionBar(ActionBar actionBar) {

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
