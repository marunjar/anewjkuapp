package org.voidsink.anewjkuapp.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.widget.TextView;
import net.fortuna.ical4j.data.CalendarBuilder;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.voidsink.anewjkuapp.AppUtils;
import org.voidsink.anewjkuapp.DrawerItem;
import org.voidsink.anewjkuapp.ImportCalendarTask;
import org.voidsink.anewjkuapp.ImportExamTask;
import org.voidsink.anewjkuapp.ImportGradeTask;
import org.voidsink.anewjkuapp.KusssAuthenticator;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.fragment.*;

import de.cketti.library.changelog.ChangeLog;

public class MainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	public static final String ARG_SHOW_FRAGMENT = "show_fragment";

	private static final String TAG = MainActivity.class.getSimpleName();

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	public static Account getAccount(Context context) {
		// get first account
		Account[] accounts = AccountManager.get(context).getAccountsByType(
				KusssAuthenticator.ACCOUNT_TYPE);
		if (accounts.length == 0) {
			return null;
		}
		return accounts[0];
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static void StartCreateAccount(Context context) {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			context.startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT)
					.putExtra(Settings.EXTRA_ACCOUNT_TYPES,
							new String[] { KusssAuthenticator.ACCOUNT_TYPE }));
		} else {
			context.startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT)
					.putExtra(
							Settings.EXTRA_AUTHORITIES,
							new String[] { CalendarContractWrapper.AUTHORITY() }));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		Fragment fragment = getSupportFragmentManager().findFragmentByTag(
				ARG_SHOW_FRAGMENT);
		if (fragment != null) {
			outState.putString(ARG_SHOW_FRAGMENT, fragment.getClass().getName());
		}

		// Log.i(TAG, "onSaveInstanceState");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (PreferenceWrapper.getUseLightDesign(this)) {
			this.setTheme(R.style.AppTheme_Light);
		} else {
			this.setTheme(R.style.AppTheme);
		}

		super.onCreate(savedInstanceState);

		// do things if new version was installed
		AppUtils.doOnNewVersion(this);
		
		// initialize graphic factory for mapsforge
		AndroidGraphicFactory.createInstance(this.getApplication());

		setContentView(R.layout.activity_main);

		// Log.i(TAG, "onCreate");

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, mDrawerLayout);

		Intent intent = getIntent();
		handleIntent(intent);

		if (savedInstanceState != null) {
			// restore saved fragment
			attachFragmentByClassName(savedInstanceState
					.getString(ARG_SHOW_FRAGMENT));
		} else {
			attachFragmentByClassName(PreferenceWrapper.getLastFragment(this));
		}

		if (getAccount(this) == null) {
			StartCreateAccount(this);
		} else {
			ChangeLog cl = new ChangeLog(this);
			if (cl.isFirstRun()) {
				cl.getLogDialog().show();
			}
		}
	}

	private void handleIntent(Intent intent) {
		if (intent != null && intent.hasExtra(ARG_SHOW_FRAGMENT)) {
			// show fragment from intent
			attachFragmentByClassName(intent.getStringExtra(ARG_SHOW_FRAGMENT));
		}
		Fragment f = getSupportFragmentManager().findFragmentByTag(
				ARG_SHOW_FRAGMENT);
		if (f != null) {
//			Log.i(TAG, "fragment: " + f.getClass().getSimpleName());
			if (BaseFragment.class.isInstance(f)) {
				((BaseFragment) f).handleIntent(intent);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		handleIntent(intent);

		if (intent != null && intent.hasExtra(ARG_SHOW_FRAGMENT)) {
			// show fragment from intent
			attachFragmentByClassName(intent.getStringExtra(ARG_SHOW_FRAGMENT));
		}
	}

	@SuppressWarnings("unchecked")
	private void attachFragmentByClassName(final String clazzname) {
		if (clazzname != null && !clazzname.isEmpty()) {
			// Log.i(TAG, "attach " + clazzname);
			try {
				Class<?> clazz = getClassLoader().loadClass(clazzname);
				if (Fragment.class.isAssignableFrom(clazz)) {
					attachFragment((Class<? extends Fragment>) clazz);
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onNavigationDrawerItemSelected(int position, DrawerItem item) {
		if (item.isEnabled()) {
			// update the main content by replacing fragments
			if ((item.getStartFragment() != null)) {
				attachFragment(item.getStartFragment());
			}
			if (item.updateActionBarTitle()) {
				mTitle = item.getLabel();
				getSupportActionBar().setTitle(mTitle);
			}
		}
	}

	private void attachFragment(Class<? extends Fragment> startFragment) {
		if (startFragment != null) {
			Fragment f = null;
			try {
				f = (Fragment) startFragment.newInstance();
				PreferenceWrapper.setLastFragment(this,
						startFragment.getCanonicalName());
			} catch (Exception e) {
				f = null;
			}
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, f, ARG_SHOW_FRAGMENT).commit();
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mNavigationDrawerFragment != null
				&& !mNavigationDrawerFragment.isDrawerOpen()) {
			restoreActionBar();
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		Log.i(TAG, "onOptionsItemSelected");

		switch (item.getItemId()) {
		case R.id.action_refresh_exams:
			Log.d(TAG, "importing exams");
			new ImportExamTask(getAccount(this), this).execute();
			return true;
		case R.id.action_refresh_grades:
			Log.d(TAG, "importing grades");
			new ImportGradeTask(getAccount(this), MainActivity.this).execute();
			return true;
		case R.id.action_refresh_calendar:
			Log.d(TAG, "importing calendars");
			new ImportCalendarTask(getAccount(this), this,
					CalendarUtils.ARG_CALENDAR_ID_EXAM, new CalendarBuilder())
					.execute();
			new ImportCalendarTask(getAccount(this), this,
					CalendarUtils.ARG_CALENDAR_ID_LVA, new CalendarBuilder())
					.execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onClickChangeLog(View v) {
		new ChangeLog(this).getFullLogDialog().show();
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
