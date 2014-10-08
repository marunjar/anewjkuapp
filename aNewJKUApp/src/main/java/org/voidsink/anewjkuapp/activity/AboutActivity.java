package org.voidsink.anewjkuapp.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;

/**
 * Created by paul on 08.10.2014.
 */
public class AboutActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (PreferenceWrapper.getUseLightDesign(this)) {
            this.setTheme(R.style.AppTheme_Light);
        } else {
            this.setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
    }
}
