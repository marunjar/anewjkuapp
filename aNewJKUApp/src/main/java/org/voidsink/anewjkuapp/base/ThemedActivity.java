package org.voidsink.anewjkuapp.base;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;

import org.voidsink.anewjkuapp.AppUtils;
import org.voidsink.anewjkuapp.R;

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
}
