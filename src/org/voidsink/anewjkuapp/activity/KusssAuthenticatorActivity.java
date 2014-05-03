package org.voidsink.anewjkuapp.activity;

import org.voidsink.anewjkuapp.KusssAuthenticator;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.kusss.KusssHandler;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class KusssAuthenticatorActivity extends AccountAuthenticatorActivity {

	public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

	public final static String PARAM_USER_PASS = "USER_PASS";

	private static final long SYNC_FREQUENCY = 60 * 60 * 23; // 23 hour (in
																// Seconds)
	private final int REQ_SIGNUP = 1;

	private final String TAG = this.getClass().getSimpleName();

	public final static String CONTENT_EXAM = "exam";
	public final static String CONTENT_LVA = "lva";

	private AccountManager mAccountManager;

	private boolean mIsNewAccount;
	private Button mSubmit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (PreferenceWrapper.getUseLightDesign(this)) {
			this.setTheme(R.style.AppTheme_Light);
		} else {
			this.setTheme(R.style.AppTheme);
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		mAccountManager = AccountManager.get(this);

		Intent mIntent = getIntent();

		String accountName = null;

		mIsNewAccount = false;
		Account mAccount = MainActivity.getAccount(this);
		if (mAccount != null) {
			accountName = mAccount.name;

			if (mIntent != null) {
				mIntent.removeExtra(KusssAuthenticator.ARG_NEW_ACCOUNT);
				mIntent.putExtra(KusssAuthenticator.ARG_ACCOUNT_NAME,
						mAccount.name);
				mIntent.putExtra(KusssAuthenticator.ARG_ACCOUNT_TYPE,
						mAccount.type);
			}
		} else if (mIntent != null) {
			accountName = getIntent().getStringExtra(
					KusssAuthenticator.ARG_ACCOUNT_NAME);
			mIsNewAccount = mIntent.getBooleanExtra(
					KusssAuthenticator.ARG_NEW_ACCOUNT, false);
		}

		if (!mIsNewAccount) {
			if (accountName != null) {
				((TextView) findViewById(R.id.accountName))
						.setText(accountName);
				((TextView) findViewById(R.id.accountName)).setEnabled(false);
			}
		}

		mSubmit = (Button) findViewById(R.id.submit);
		mSubmit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				submit();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// The sign up activity returned that the user has successfully created
		// an account
		if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
			finishLogin(data);
		} else
			super.onActivityResult(requestCode, resultCode, data);
	}

	public void submit() {

		final String userName = ((TextView) findViewById(R.id.accountName))
				.getText().toString();
		final String userPass = ((TextView) findViewById(R.id.accountPassword))
				.getText().toString();

		final String accountType = getIntent().getStringExtra(
				KusssAuthenticator.ARG_ACCOUNT_TYPE);

		new AsyncTask<String, Void, Intent>() {
			private ProgressDialog progressDialog;

			@Override
			protected void onPreExecute() {
				progressDialog = ProgressDialog.show(
						KusssAuthenticatorActivity.this,
						getString(R.string.progress_title),
						getString(R.string.progress_login), true);
			};

			@Override
			protected Intent doInBackground(String... params) {
				String authtoken = null;
				Bundle data = new Bundle();
				try {
					if (KusssHandler.handler.login(userName, userPass)) {
						authtoken = userName;
					}

					data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
					data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
					data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
					data.putString(PARAM_USER_PASS, userPass);

				} catch (Exception e) {
					data.putString(KEY_ERROR_MESSAGE, e.getMessage());
				}

				final Intent res = new Intent();
				res.putExtras(data);
				return res;
			}

			@Override
			protected void onPostExecute(Intent intent) {
				progressDialog.dismiss();

				if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
					Toast.makeText(getBaseContext(),
							intent.getStringExtra(KEY_ERROR_MESSAGE),
							Toast.LENGTH_SHORT).show();
				} else if (intent.hasExtra(AccountManager.KEY_AUTHTOKEN)) {
					String authToken = intent
							.getStringExtra(AccountManager.KEY_AUTHTOKEN);
					if ((authToken != null) && !TextUtils.isEmpty(authToken)) {
						finishLogin(intent);
					} else {
						Toast.makeText(getBaseContext(),
								"Login failed, wrong Password",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(getBaseContext(),
							"Login failed, AuthToken missing",
							Toast.LENGTH_SHORT).show();
				}
			}
		}.execute();
	}

	private void finishLogin(Intent intent) {
		String accountName = intent
				.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
		String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
		String accountType = intent
				.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

		Log.i(TAG, "finish login to " + accountName);

		final Account account = new Account(accountName, accountType);

		if (getIntent().getBooleanExtra(KusssAuthenticator.ARG_NEW_ACCOUNT,
				false)) {
			// Creating the account on the device and setting the auth token we
			// got
			// (Not setting the auth token will cause another call to the server
			// to authenticate the user)
			mAccountManager
					.addAccountExplicitly(account, accountPassword, null);
			mAccountManager.setPassword(account, accountPassword);
			// Create Calendars
			Uri cal = CalendarUtils.createCalendar(this, intent, "JKU LVAs",
					CalendarUtils.COLOR_DEFAULT_LVA);
			if (cal != null) {
				mAccountManager.setUserData(account,
						CalendarUtils.ARG_CALENDAR_ID_LVA, cal
								.getLastPathSegment().toString());
			}
			cal = CalendarUtils.createCalendar(this, intent, "JKU Exams",
					CalendarUtils.COLOR_DEFAULT_EXAM);
			if (cal != null) {
				mAccountManager.setUserData(account,
						CalendarUtils.ARG_CALENDAR_ID_EXAM, cal
								.getLastPathSegment().toString());
			}

			// Turn on periodic syncing
			ContentResolver.addPeriodicSync(account,
					CalendarContractWrapper.AUTHORITY(), new Bundle(),
					SYNC_FREQUENCY);
			ContentResolver.addPeriodicSync(account,
					KusssContentContract.AUTHORITY, new Bundle(),
					SYNC_FREQUENCY);
			// Inform the system that this account supports sync
			ContentResolver.setIsSyncable(account,
					CalendarContractWrapper.AUTHORITY(), 1);
			ContentResolver.setIsSyncable(account,
					KusssContentContract.AUTHORITY, 1);
			// Inform the system that this account is eligible for auto sync
			// when the network is up
			ContentResolver.setSyncAutomatically(account,
					CalendarContractWrapper.AUTHORITY(), true);
			ContentResolver.setSyncAutomatically(account,
					KusssContentContract.AUTHORITY, true);
			// Recommend a schedule for automatic synchronization. The system
			// may modify this based
			// on other scheduled syncs and network utilization.
		} else {
			mAccountManager.setPassword(account, accountPassword);
			// updateCalendar();
		}

		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
		finish();
	}

}
