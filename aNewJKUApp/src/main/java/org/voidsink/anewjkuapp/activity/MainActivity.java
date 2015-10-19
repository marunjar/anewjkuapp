/*
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 *
 * Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.voidsink.anewjkuapp.activity;

import android.Manifest;
import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.voidsink.anewjkuapp.KusssAuthenticator;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PoiContentContract;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.StackedFragment;
import org.voidsink.anewjkuapp.base.ThemedActivity;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.fragment.AssessmentFragment;
import org.voidsink.anewjkuapp.fragment.CalendarFragment;
import org.voidsink.anewjkuapp.fragment.CalendarFragment2;
import org.voidsink.anewjkuapp.fragment.CurriculaFragment;
import org.voidsink.anewjkuapp.fragment.ExamFragment;
import org.voidsink.anewjkuapp.fragment.LvaFragment;
import org.voidsink.anewjkuapp.fragment.MapFragment;
import org.voidsink.anewjkuapp.fragment.MensaFragment;
import org.voidsink.anewjkuapp.fragment.OehInfoFragment;
import org.voidsink.anewjkuapp.fragment.OehNewsFragment;
import org.voidsink.anewjkuapp.fragment.OehRightsFragment;
import org.voidsink.anewjkuapp.fragment.StatFragment;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import de.cketti.library.changelog.ChangeLog;

public class MainActivity extends ThemedActivity {

    public static final String ARG_SHOW_FRAGMENT_ID = "show_fragment_id";
    public static final String ARG_EXACT_LOCATION = "exact_location";
    public static final String ARG_SAVE_LAST_FRAGMENT = "save_last_fragment";

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int MY_PERMISSION_READ_CALENDAR = 0;
    private static final int MY_PERMISSION_WRITE_CALENDAR = 1;
    private static final int MY_PERMISSION_ACCOUNTS = 2;

    /**
     * Fragment managing the behaviors, interactions and presentation of the
     * navigation drawer.
     */
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private boolean mUserLearnedDrawer;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void StartCreateAccount(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                context.startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT)
                        .putExtra(Settings.EXTRA_ACCOUNT_TYPES,
                                new String[]{KusssAuthenticator.ACCOUNT_TYPE}));
            } else {
                context.startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT)
                        .putExtra(
                                Settings.EXTRA_AUTHORITIES,
                                new String[]{CalendarContractWrapper.AUTHORITY()}));
            }
        }
    }

    public static void StartMyCurricula(Context context) {
        //
        Intent i = new Intent(context, MainActivity.class)
                .putExtra(MainActivity.ARG_SHOW_FRAGMENT_ID, R.id.nav_curricula)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(i);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(this.getClass().getCanonicalName(), KusssContentContract.AUTHORITY);
        Log.d(this.getClass().getCanonicalName(), this.getResources().getString(R.string.config_kusss_provider));
        Log.d(this.getClass().getCanonicalName(), PoiContentContract.AUTHORITY);
        Log.d(this.getClass().getCanonicalName(), this.getResources().getString(R.string.config_poi_provider));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // set Statusbar color to transparent if a drawer exists
            // in this case status bar is colored by DrawerLayout.setStatusBarBackgroundColor(). which is default primaryDark
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.fullTransparent));
        }

        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALENDAR)) {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.alert_permission_read_calendar))
                        .setPositiveButton(R.string.button_ok, null)
                        .setCancelable(false)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CALENDAR}, MY_PERMISSION_READ_CALENDAR);
                                }
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, MY_PERMISSION_READ_CALENDAR);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALENDAR)) {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.alert_permission_write_calendar))
                        .setPositiveButton(R.string.button_ok, null)
                        .setCancelable(false)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_CALENDAR}, MY_PERMISSION_WRITE_CALENDAR);
                                }
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, MY_PERMISSION_WRITE_CALENDAR);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.GET_ACCOUNTS)) {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.alert_permission_get_accounts))
                        .setPositiveButton(R.string.button_ok, null)
                        .setCancelable(false)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, MY_PERMISSION_ACCOUNTS);
                                }
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS}, MY_PERMISSION_ACCOUNTS);
            }
        }

        // do things if new version was installed
        AppUtils.doOnNewVersion(this);

        // initialize graphic factory for mapsforge
        AndroidGraphicFactory.createInstance(this.getApplication());

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                initActionBar();
            }
        });

        // set up drawer
        mUserLearnedDrawer = PreferenceWrapper.getUserLearnedDrawer(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }

        Intent intent = getIntent();

        Fragment f = attachFragment(intent, savedInstanceState, true);
        // attach calendar fragment as default
        if (f == null) {
            f = attachFragmentById(R.id.nav_cal, true);
        }
        handleIntent(f, intent);

        ChangeLog cl = new ChangeLog(this);
        if (cl.isFirstRun()) {
            cl.getLogDialog().show();
        }

        if (AppUtils.getAccount(this) == null) {
            StartCreateAccount(this);
        }

        Log.d(TAG, "onCreate finished");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_READ_CALENDAR:
            case MY_PERMISSION_WRITE_CALENDAR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Account account = AppUtils.getAccount(this);
                    CalendarUtils.createCalendarsIfNecessary(this, account);
                    Log.d(TAG, String.format("Permission granted: %d", requestCode));
                } else {
                    Log.d(TAG, String.format("Permission denied: %d", requestCode));
                }
                return;
            case MY_PERMISSION_ACCOUNTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (AppUtils.getAccount(this) == null) {
                        StartCreateAccount(this);
                    }
                    Log.d(TAG, String.format("Permission granted: %d", requestCode));
                } else {
                    Log.d(TAG, String.format("Permission denied: %d", requestCode));
                }
                return;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private Fragment attachFragmentById(int id, boolean saveLastFragment) {
        if (mNavigationView != null) {
            MenuItem mMenuItem = mNavigationView.getMenu().findItem(id);

            return attachFragment(mMenuItem, saveLastFragment);
        }
        return null;
    }

    private void setupDrawerContent(NavigationView navigationView) {
        // set a custom shadow that overlays the main content when the drawer
        // opens
        // mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
        // GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setHomeButtonEnabled(true);

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        checkNavItem(menuItem);

                        switch (menuItem.getItemId()) {
                            case R.id.nav_settings: {
                                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                                break;
                            }
                            case R.id.nav_about: {
                                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                                break;
                            }
                            default:
                                attachFragment(menuItem, true);
                                break;
                        }

                        mDrawerLayout.closeDrawers();
                        return menuItem.isCheckable();
                    }
                });

        if (!mUserLearnedDrawer) {
            mDrawerLayout.openDrawer(navigationView);
        }

        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                if (!mUserLearnedDrawer) {
                    PreferenceWrapper.setPrefUserLearnedDrawer(MainActivity.this, true);
                }

                TextView mDrawerUser = (TextView) mNavigationView.findViewById(R.id.drawer_user);

                if (mDrawerUser != null) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                        mDrawerUser.setText(R.string.missing_app_permission);
                        mDrawerUser.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    MainActivity.this.startActivity(
                                            new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                    .addCategory(Intent.CATEGORY_DEFAULT)
                                                    .setData(Uri.parse("package:org.voidsink.anewjkuapp")));
                                } catch (Exception e) {

                                }
                            }
                        });
                    } else {
                        Account account = AppUtils.getAccount(MainActivity.this);
                        if (account == null) {
                            mDrawerUser.setText(R.string.action_tap_to_login);
                            mDrawerUser.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MainActivity.StartCreateAccount(MainActivity.this);
                                }
                            });
                        } else {
                            mDrawerUser.setText(account.name);
                            mDrawerUser.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MainActivity.StartMyCurricula(MainActivity.this);
                                }
                            });
                        }
                    }
                }

                super.onDrawerOpened(drawerView);
            }
        });
    }

    private Fragment attachFragment(Intent intent, Bundle savedInstanceState,
                                    boolean attachStored) {
        if (intent != null && intent.hasExtra(ARG_SHOW_FRAGMENT_ID)) {
            // show fragment from intent
            return attachFragmentById(
                    intent.getIntExtra(ARG_SHOW_FRAGMENT_ID, 0),
                    intent.getBooleanExtra(ARG_SAVE_LAST_FRAGMENT, true));
        } else if (savedInstanceState != null) {
            // restore saved fragment
            return attachFragmentById(savedInstanceState
                    .getInt(ARG_SHOW_FRAGMENT_ID), true);
        } else if (attachStored) {
            return attachFragmentById(PreferenceWrapper
                    .getLastFragment(this), true);
        } else {
            return getSupportFragmentManager().findFragmentByTag(
                    Consts.ARG_FRAGMENT_TAG);
        }
    }

    private void handleIntent(Fragment f, Intent intent) {
        if (f == null) {
            f = getSupportFragmentManager()
                    .findFragmentByTag(Consts.ARG_FRAGMENT_TAG);
        }

        if (f != null) {
            // Log.i(TAG, "fragment: " + f.getClass().getSimpleName());
            if (BaseFragment.class.isInstance(f)) {
                ((BaseFragment) f).handleIntent(intent);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Fragment f = attachFragment(intent, null, false);
        handleIntent(f, intent);
    }

    private Fragment attachFragment(MenuItem menuItem, boolean saveLastFragment) {

        if (menuItem == null) {
            return null;
        }

        Class<? extends Fragment> startFragment = null;

        switch (menuItem.getItemId()) {
            case R.id.nav_cal:
                if (PreferenceWrapper.getUseCalendarView(this)) {
                    startFragment = CalendarFragment2.class;
                } else {
                    startFragment = CalendarFragment.class;
                }
                break;
            case R.id.nav_exams:
                startFragment = ExamFragment.class;
                break;
            case R.id.nav_grades:
                startFragment = AssessmentFragment.class;
                break;
            case R.id.nav_courses:
                startFragment = LvaFragment.class;
                break;
            case R.id.nav_stats:
                startFragment = StatFragment.class;
                break;
            case R.id.nav_mensa:
                startFragment = MensaFragment.class;
                break;
            case R.id.nav_map:
                startFragment = MapFragment.class;
                break;
            case R.id.nav_oeh_news:
                startFragment = OehNewsFragment.class;
                break;
            case R.id.nav_oeh_info:
                startFragment = OehInfoFragment.class;
                break;
            case R.id.nav_oeh_rigths:
                startFragment = OehRightsFragment.class;
                break;
            case R.id.nav_curricula:
                startFragment = CurriculaFragment.class;
                break;
            default:
                break;
        }

        if (startFragment != null) {
            try {
                final Fragment oldFragment = getSupportFragmentManager().findFragmentByTag(Consts.ARG_FRAGMENT_TAG);

                if (oldFragment != null) {
                    if (oldFragment.getClass().getCanonicalName().equals(startFragment.getCanonicalName())) {
                        return oldFragment;
                    }
                }

                Fragment f = startFragment.newInstance();

                Bundle b = new Bundle();
                b.putCharSequence(Consts.ARG_FRAGMENT_TITLE, menuItem.getTitle());
                b.putInt(Consts.ARG_FRAGMENT_ID, menuItem.getItemId());
                f.setArguments(b);

                final boolean addToBackstack = (oldFragment != null) && (!oldFragment.getClass().equals(f.getClass()));

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, f, Consts.ARG_FRAGMENT_TAG);
                if (addToBackstack) {
                    ft.addToBackStack(f.getClass().getCanonicalName());
                }
                ft.commit();

                if (saveLastFragment) {
                    PreferenceWrapper.setLastFragment(this, menuItem.getItemId());
                }

                initActionBar();

                return f;
            } catch (Exception e) {
                Log.w(TAG, "fragment instantiation failed", e);
                Analytics.sendException(this, e, false);
                if (saveLastFragment) {
                    PreferenceWrapper.setLastFragment(this, PreferenceWrapper.PREF_LAST_FRAGMENT_DEFAULT);
                }
                return null;
            }
        }
        return null;
    }


    @Override
    protected void onInitActionBar(ActionBar actionBar) {
        super.onInitActionBar(actionBar);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);

        if (mNavigationView != null) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(Consts.ARG_FRAGMENT_TAG);
            if (f instanceof StackedFragment) {
                int id = ((StackedFragment) f).getId(this);
                if (id > 0) {
                    MenuItem menuItem = mNavigationView.getMenu().findItem(id);
                    checkNavItem(menuItem);
                }
            }
        }
    }

    private void uncheckMenuItems(Menu menu) {
        if (menu != null) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);

                if (item.isCheckable()) {
                    item.setChecked(false);
                }

                if (item.hasSubMenu()) {
                    uncheckMenuItems(item.getSubMenu());
                }
            }
        }
    }

    private void checkNavItem(MenuItem menuItem) {
        if (mNavigationView != null) {
            if (menuItem != null) {
                if (menuItem.isCheckable() && !menuItem.isChecked()) {
                    // iterate over all items and uncheck them, groups doesn't work with submenus for grouping entries in NavigationView
                    uncheckMenuItems(mNavigationView.getMenu());

                    menuItem.setChecked(true);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        initActionBar();

        menu.clear();
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
    protected void onStop() {
        super.onStop();

        Analytics.clearScreen(this);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            TextView textView = (TextView) rootView
                    .findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(
                    ARG_SECTION_NUMBER)));
            return rootView;
        }
    }
}
