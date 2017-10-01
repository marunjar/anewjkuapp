/*
 *      ____.____  __.____ ___     _____
 *     |    |    |/ _|    |   \   /  _  \ ______ ______
 *     |    |      < |    |   /  /  /_\  \\____ \\____ \
 * /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 * \________|____|__ \______/   \____|__  /   __/|   __/
 *                  \/                  \/|__|   |__|
 * <p>
 * Copyright (c) 2014-2015 Paul "Marunjar" Pretsch
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.voidsink.anewjkuapp.activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.voidsink.anewjkuapp.KusssAuthenticator;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PreferenceWrapper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.anewjkuapp.utils.UIUtils;
import org.voidsink.anewjkuapp.workaround.AccountAuthenticatorActivity;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class KusssAuthenticatorActivity extends AccountAuthenticatorActivity {

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";
    public final static String PARAM_USER_PASS = "USER_PASS";

    private final static String TAG = KusssAuthenticatorActivity.class.getSimpleName();

    private AccountManager mAccountManager;
    private String mAuthTokenType;
    private Button mSubmit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        UIUtils.applyTheme(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        initActionBar();

        mAccountManager = AccountManager.get(this);

        Intent mIntent = getIntent();

        String accountName = null;

        Boolean mIsNewAccount = false;
        Account mAccount = AppUtils.getAccount(this);
        if (mAccount != null) {
            accountName = mAccount.name;

            if (mIntent != null) {
                mIntent.removeExtra(KusssAuthenticator.ARG_IS_ADDING_NEW_ACCOUNT);
                mIntent.putExtra(KusssAuthenticator.ARG_ACCOUNT_NAME,
                        mAccount.name);
                mIntent.putExtra(KusssAuthenticator.ARG_ACCOUNT_TYPE,
                        mAccount.type);
            }
        } else if (mIntent != null) {
            accountName = getIntent().getStringExtra(
                    KusssAuthenticator.ARG_ACCOUNT_NAME);
            mIsNewAccount = mIntent.getBooleanExtra(
                    KusssAuthenticator.ARG_IS_ADDING_NEW_ACCOUNT, false);
            if (!mIntent.hasExtra(KusssAuthenticator.ARG_ACCOUNT_TYPE)) {
                mIntent.putExtra(KusssAuthenticator.ARG_ACCOUNT_TYPE,
                        KusssAuthenticator.ACCOUNT_TYPE);
            }
        }

        if (mAuthTokenType == null) {
            mAuthTokenType = KusssAuthenticator.AUTHTOKEN_TYPE_READ_ONLY;
        }

        if (!mIsNewAccount) {
            if (accountName != null) {
                final TextView tvAccountName = (TextView) findViewById(R.id.accountName);
                if (tvAccountName != null) {
                    tvAccountName.setText(accountName);
                    tvAccountName.setEnabled(false);
                }
            }
        }

        mSubmit = (Button) findViewById(R.id.accountLogin);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) && (ContextCompat.checkSelfPermission(KusssAuthenticatorActivity.this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)) {
                    new AlertDialog.Builder(KusssAuthenticatorActivity.this)
                            .setMessage(R.string.alert_permission_get_accounts)
                            .setPositiveButton(R.string.button_ok, null)
                            .setCancelable(false)
                            .show();
                } else {
                    submit();
                }
            }
        });
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void submit() {
        mSubmit.setEnabled(false);

        final String userName = ((TextView) findViewById(R.id.accountName))
                .getText().toString();
        final String userPass = ((TextView) findViewById(R.id.accountPassword))
                .getText().toString();

        final String accountType = getIntent().getStringExtra(
                KusssAuthenticator.ARG_ACCOUNT_TYPE);

        new AsyncTask<String, Void, Intent>() {
            private ProgressDialog progressDialog;
            private Context mContext;

            @Override
            protected void onPreExecute() {
                mContext = KusssAuthenticatorActivity.this;

                progressDialog = ProgressDialog.show(
                        KusssAuthenticatorActivity.this,
                        mContext.getString(R.string.progress_title),
                        mContext.getString(R.string.progress_login), true);
            }

            @Override
            protected Intent doInBackground(String... params) {
                Bundle data = new Bundle();
                try {
                    final String authtoken = KusssHandler.getInstance().login(KusssAuthenticatorActivity.this, userName, userPass);

                    KusssHandler.getInstance().logout(KusssAuthenticatorActivity.this);

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
                                mContext.getString(R.string.account_login_failed_wrong_pwd),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(),
                            mContext.getString(R.string.account_login_failed_wrong_auth_token),
                            Toast.LENGTH_SHORT).show();
                }

                progressDialog.dismiss();

                mSubmit.setEnabled(true);

                mContext = null;
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

        Account account;
        boolean addNewAccount;

        final Account oldAccount = AppUtils.getAccount(this);
        if (oldAccount != null) {
            if (oldAccount.name.equals(accountName)) {
                account = oldAccount;
                addNewAccount = false;
            } else {
                AppUtils.removeAccout(mAccountManager, oldAccount);
                account = new Account(accountName, accountType);
                addNewAccount = true;
            }
        } else {
            account = new Account(accountName, accountType);
            addNewAccount = true;
        }

        if (addNewAccount) {
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we
            // got
            // (Not setting the auth token will cause another call to the server
            // to authenticate the user)
            mAccountManager
                    .addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
            mAccountManager.setPassword(account, accountPassword);

            // Turn on periodic syncing
            long interval = PreferenceWrapper.getSyncInterval(this) * 60L * 60;

            ContentResolver.addPeriodicSync(account,
                    CalendarContractWrapper.AUTHORITY(), new Bundle(),
                    interval);
            ContentResolver.addPeriodicSync(account,
                    KusssContentContract.AUTHORITY, new Bundle(),
                    interval);
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
        }

        // Kalender aktualisieren
        CalendarUtils.createCalendarsIfNecessary(this, account);

        // Sync NOW
        KusssAuthenticator.triggerSync(this);

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (UIUtils.handleUpNavigation(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();

        Analytics.sendScreen(this, Consts.SCREEN_LOGIN);
    }
}
