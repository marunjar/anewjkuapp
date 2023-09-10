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

package org.voidsink.anewjkuapp.activity;

import android.Manifest;
import android.accounts.Account;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.KusssAuthenticator;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PoiContentContract;
import org.voidsink.anewjkuapp.PreferenceHelper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.PendingIntentHandler;
import org.voidsink.anewjkuapp.base.StackedFragment;
import org.voidsink.anewjkuapp.base.ThemedActivity;
import org.voidsink.anewjkuapp.fragment.AssessmentFragment;
import org.voidsink.anewjkuapp.fragment.CalendarFragment;
import org.voidsink.anewjkuapp.fragment.CalendarFragment2;
import org.voidsink.anewjkuapp.fragment.CurriculaFragment;
import org.voidsink.anewjkuapp.fragment.ExamFragment;
import org.voidsink.anewjkuapp.fragment.LvaFragment;
import org.voidsink.anewjkuapp.fragment.MapFragment;
import org.voidsink.anewjkuapp.fragment.MensaFragment;
import org.voidsink.anewjkuapp.fragment.OehInfoFragment;
import org.voidsink.anewjkuapp.fragment.OehRightsFragment;
import org.voidsink.anewjkuapp.fragment.StatFragment;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends ThemedActivity {

    public static final String ARG_SHOW_FRAGMENT_ID = "show_fragment_id";
    public static final String ARG_EXACT_LOCATION = "exact_location";
    public static final String ARG_SAVE_LAST_FRAGMENT = "save_last_fragment";

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private static final int PERMISSIONS_REQUEST_ACCOUNT = 2;
    private static final String[] ACCOUNT_PERMISSIONS = {Manifest.permission.GET_ACCOUNTS};

    /**
     * Fragment managing the behaviors, interactions and presentation of the
     * navigation drawer.
     */
    private DrawerLayout mDrawerLayout;
    private DrawerLayout.DrawerListener mDrawerListener;
    private NavigationView mNavigationView;
    private TextView mDrawerUser = null;
    private Intent mPendingIntent = null;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mNavigationView != null) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(Consts.ARG_FRAGMENT_TAG);
            if (f instanceof StackedFragment) {
                int id = ((StackedFragment) f).getId(this);
                if (id > 0) {
                    outState.putInt(ARG_SHOW_FRAGMENT_ID, id);
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        logger.debug(KusssContentContract.AUTHORITY);
        logger.debug(this.getResources().getString(R.string.config_kusss_provider));
        logger.debug(PoiContentContract.AUTHORITY);
        logger.debug(this.getResources().getString(R.string.config_poi_provider));

        // set Statusbar color to transparent if a drawer exists
        // in this case status bar is colored by DrawerLayout.setStatusBarBackgroundColor(). which is default primaryDark
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.fullTransparent));

        setContentView(R.layout.activity_main);

        // do things if new version was installed
        AppUtils.doOnNewVersion(this);

        // initialize graphic factory for mapsforge
        AndroidGraphicFactory.createInstance(this.getApplication());

        // set up drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);

        mDrawerListener = new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                final TextView drawerUser = getDrawerUser();
                if (drawerUser != null) {
                    if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M) && (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)) {
                        drawerUser.setText(R.string.missing_app_permission);
                        drawerUser.setOnClickListener(v -> {
                            try {
                                MainActivity.this.startActivity(
                                        new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                .addCategory(Intent.CATEGORY_DEFAULT)
                                                .setData(Uri.parse("package:org.voidsink.anewjkuapp")));
                            } catch (Exception e) {
                                AnalyticsHelper.sendException(MainActivity.this, e, false);
                            }
                        });
                    } else {
                        Account account = AppUtils.getAccount(MainActivity.this);
                        if (account == null) {
                            drawerUser.setText(R.string.action_tap_to_login);
                            drawerUser.setOnClickListener(v -> startCreateAccount());
                        } else {
                            drawerUser.setText(account.name);
                            drawerUser.setOnClickListener(v -> startMyCurricula());
                        }
                    }
                }

                super.onDrawerOpened(drawerView);
            }
        };

        mNavigationView = findViewById(R.id.nav_view);
        if (mNavigationView != null) {
            mDrawerUser = mNavigationView.getHeaderView(0).findViewById(R.id.drawer_user);
            setupDrawerContent(mNavigationView);
        }

        Intent intent = getIntent();
        // attach calendar fragment as default
        if (!attachFragment(intent, savedInstanceState, true)) {
            attachFragmentById(intent, R.id.nav_cal, true);
        }

        startCreateAccount();

        logger.debug("onCreate finished");
    }

    protected TextView getDrawerUser() {
        return mDrawerUser;
    }

    @AfterPermissionGranted(PERMISSIONS_REQUEST_ACCOUNT)
    public void startCreateAccount() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) || EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            if (AppUtils.getAccount(this) == null) {
                this.startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT)
                        .putExtra(Settings.EXTRA_ACCOUNT_TYPES,
                                new String[]{KusssAuthenticator.ACCOUNT_TYPE}));
            }
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.alert_permission_get_accounts),
                    PERMISSIONS_REQUEST_ACCOUNT,
                    ACCOUNT_PERMISSIONS);
        }
    }

    public void startMyCurricula() {
        Intent i = new Intent(this, MainActivity.class)
                .putExtra(MainActivity.ARG_SHOW_FRAGMENT_ID, R.id.nav_curricula)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        this.startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private boolean attachFragmentById(Intent intent, int id, boolean saveLastFragment) {
        if (mNavigationView != null) {
            MenuItem mMenuItem = mNavigationView.getMenu().findItem(id);

            return attachFragmentByMenuItem(intent, mMenuItem, saveLastFragment);
        }
        return false;
    }

    private void setupDrawerContent(NavigationView navigationView) {
        // set a custom shadow that overlays the main content when the drawer
        // opens
        // mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
        // GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
        actionBar.setHomeButtonEnabled(true);

        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    int itemId = menuItem.getItemId();
                    if (itemId == R.id.nav_settings) {
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    } else if (itemId == R.id.nav_about) {
                        startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    } else {
                        attachFragmentByMenuItem(null, menuItem, true);
                    }

                    mDrawerLayout.closeDrawers();
                    return menuItem.isCheckable();
                });
    }

    private boolean attachFragment(Intent intent, Bundle savedInstanceState,
                                   boolean attachStored) {
        if (intent != null && intent.hasExtra(ARG_SHOW_FRAGMENT_ID)) {
            // show fragment from intent
            return attachFragmentById(
                    intent,
                    intent.getIntExtra(ARG_SHOW_FRAGMENT_ID, 0),
                    intent.getBooleanExtra(ARG_SAVE_LAST_FRAGMENT, true));
        } else if (savedInstanceState != null) {
            // restore saved fragment
            return attachFragmentById(intent, savedInstanceState
                    .getInt(ARG_SHOW_FRAGMENT_ID), true);
        } else if (attachStored) {
            return attachFragmentById(intent, PreferenceHelper
                    .getLastFragment(this), true);
        } else {
            this.mPendingIntent = intent;
            handleIntentOnFragment();
            return getSupportFragmentManager().findFragmentByTag(
                    Consts.ARG_FRAGMENT_TAG) != null;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        this.mPendingIntent = intent;

        attachFragment(intent, null, false);
    }

    private boolean attachFragmentByMenuItem(Intent intent, MenuItem menuItem, boolean saveLastFragment) {

        if (menuItem == null) {
            return false;
        }

        Class<? extends Fragment> startFragment = getFragmentClassById(menuItem.getItemId());
        if (startFragment == null) {
            return false;
        }

        try {
            this.mPendingIntent = intent;

            Bundle args = new Bundle();
            args.putCharSequence(Consts.ARG_FRAGMENT_TITLE, menuItem.getTitle());
            args.putInt(Consts.ARG_FRAGMENT_ID, menuItem.getItemId());

            final Fragment oldFragment = getSupportFragmentManager().findFragmentByTag(Consts.ARG_FRAGMENT_TAG);
            final boolean fragmentChanged = (oldFragment == null) || !oldFragment.getClass().equals(startFragment);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container, startFragment, args, Consts.ARG_FRAGMENT_TAG);
            if (fragmentChanged && oldFragment != null) {
                ft.addToBackStack(startFragment.getCanonicalName());
            }
            ft.commit();

            if (saveLastFragment) {
                PreferenceHelper.setLastFragment(this, menuItem.getItemId());
            }

            if (!fragmentChanged) {
                handleIntentOnFragment();
            }

            return true;
        } catch (Exception e) {
            AnalyticsHelper.sendException(this, e, false, startFragment.getName());
            if (saveLastFragment) {
                PreferenceHelper.setLastFragment(this, PreferenceHelper.PREF_LAST_FRAGMENT_DEFAULT);
            }
            return false;
        }
    }

    private Class<? extends Fragment> getFragmentClassById(int itemId) {
        if (itemId == R.id.nav_cal) {
            if (PreferenceHelper.getUseCalendarView(this)) {
                return CalendarFragment2.class;
            } else {
                return CalendarFragment.class;
            }
        } else if (itemId == R.id.nav_exams) {
            return ExamFragment.class;
        } else if (itemId == R.id.nav_grades) {
            return AssessmentFragment.class;
        } else if (itemId == R.id.nav_courses) {
            return LvaFragment.class;
        } else if (itemId == R.id.nav_stats) {
            return StatFragment.class;
        } else if (itemId == R.id.nav_mensa) {
            return MensaFragment.class;
        } else if (itemId == R.id.nav_map) {
            return MapFragment.class;
        } else if (itemId == R.id.nav_oeh_info) {
            return OehInfoFragment.class;
        } else if (itemId == R.id.nav_oeh_rigths) {
            return OehRightsFragment.class;
        } else if (itemId == R.id.nav_curricula) {
            return CurriculaFragment.class;
        }
        return null;
    }

    private void handleIntentOnFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(Consts.ARG_FRAGMENT_TAG);

        if (fragment instanceof BaseFragment) {
            ((BaseFragment) fragment).handleIntent();
        }
    }

    public void handlePendingIntent(PendingIntentHandler handler) {
        if (mPendingIntent != null) {
            handler.handlePendingIntent(mPendingIntent);
            mPendingIntent = null;
        }
    }

    @Override
    protected void onInitActionBar(ActionBar actionBar) {
        super.onInitActionBar(actionBar);

        actionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDrawerLayout.addDrawerListener(mDrawerListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mDrawerLayout.removeDrawerListener(mDrawerListener);

        AnalyticsHelper.clearScreen();
    }
}
