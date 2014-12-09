package org.voidsink.anewjkuapp.activity;

import android.os.Bundle;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.ThemedActivity;
import org.voidsink.anewjkuapp.utils.Consts;

public class AboutActivity extends ThemedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
    }

    @Override
    protected String getScreenName() {
        return Consts.SCREEN_ABOUT;
    }
}
