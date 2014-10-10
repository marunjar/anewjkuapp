package org.voidsink.anewjkuapp.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.voidsink.anewjkuapp.AppUtils;

public class ThemedActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppUtils.applyTheme(this);

        super.onCreate(savedInstanceState);
    }
}
