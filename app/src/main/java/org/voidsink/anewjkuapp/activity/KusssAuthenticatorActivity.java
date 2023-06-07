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
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.KusssAuthenticator;
import org.voidsink.anewjkuapp.KusssContentContract;
import org.voidsink.anewjkuapp.PreferenceHelper;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.analytics.AnalyticsHelper;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;
import org.voidsink.anewjkuapp.utils.UIUtils;
import org.voidsink.anewjkuapp.workaround.AccountAuthenticatorActivity;

import java.lang.ref.WeakReference;

public class KusssAuthenticatorActivity extends AccountAuthenticatorActivity {

    private static final String KEY_ERROR_MESSAGE = "ERR_MSG";
    private final static String PARAM_USER_PASS = "USER_PASS";

    private static final Logger logger = LoggerFactory.getLogger(KusssAuthenticatorActivity.class);

    private AccountManager mAccountManager;
    private String mAuthTokenType;
    private Button mSubmit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        initActionBar();

        mAccountManager = AccountManager.get(this);

        Intent mIntent = getIntent();

        String accountName = null;

        boolean mIsNewAccount = false;
        Account mAccount = AppUtils.getAccount(this);
        if (mAccount != null) {
            accountName = mAccount.name;

            if (mIntent != null) {
                mIntent.removeExtra(Consts.ARG_IS_ADDING_NEW_ACCOUNT);
                mIntent.putExtra(Consts.ARG_ACCOUNT_NAME,
                        mAccount.name);
                mIntent.putExtra(Consts.ARG_ACCOUNT_TYPE,
                        mAccount.type);
            }
        } else if (mIntent != null) {
            accountName = getIntent().getStringExtra(
                    Consts.ARG_ACCOUNT_NAME);
            mIsNewAccount = mIntent.getBooleanExtra(
                    Consts.ARG_IS_ADDING_NEW_ACCOUNT, false);
            if (!mIntent.hasExtra(Consts.ARG_ACCOUNT_TYPE)) {
                mIntent.putExtra(Consts.ARG_ACCOUNT_TYPE,
                        KusssAuthenticator.ACCOUNT_TYPE);
            }
        }

        if (mAuthTokenType == null) {
            mAuthTokenType = KusssAuthenticator.AUTHTOKEN_TYPE_READ_ONLY;
        }

        if (!mIsNewAccount && accountName != null) {
            final TextView tvAccountName = findViewById(R.id.accountName);
            if (tvAccountName != null) {
                tvAccountName.setText(accountName);
                tvAccountName.setEnabled(false);
            }
        }

        mSubmit = findViewById(R.id.accountLogin);
        mSubmit.setOnClickListener(v -> {
            if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M) && (ContextCompat.checkSelfPermission(KusssAuthenticatorActivity.this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)) {
                new MaterialAlertDialogBuilder(KusssAuthenticatorActivity.this)
                        .setMessage(R.string.alert_permission_get_accounts)
                        .setPositiveButton(R.string.button_ok, null)
                        .setCancelable(false)
                        .show();
            } else {
                submit();
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
        getSubmit().setEnabled(false);

        final String userName = ((TextView) findViewById(R.id.accountName))
                .getText().toString();
        final String userPass = ((TextView) findViewById(R.id.accountPassword))
                .getText().toString();

        final String accountType = getIntent().getStringExtra(
                Consts.ARG_ACCOUNT_TYPE);

        new LoginTask(this, accountType, userName, userPass).execute();
    }

    private static class LoginTask extends AsyncTask<String, Void, Intent> {
        private final String mAccountType;
        private final String mUserName;
        private final String mUserPass;
        private ProgressDialog progressDialog;
        private final WeakReference<KusssAuthenticatorActivity> activityReference;

        public LoginTask(KusssAuthenticatorActivity context, String accountType, String userName, String userPass) {
            super();

            this.activityReference = new WeakReference<>(context);
            this.mAccountType = accountType;
            this.mUserName = userName;
            this.mUserPass = userPass;
        }

        @Override
        protected void onPreExecute() {
            KusssAuthenticatorActivity activity = activityReference.get();

            progressDialog = ProgressDialog.show(
                    activity,
                    activity.getString(R.string.progress_title),
                    activity.getString(R.string.progress_login), true);
        }

        @Override
        protected Intent doInBackground(String... params) {
            Bundle data = new Bundle();
            try {
                KusssAuthenticatorActivity activity = activityReference.get();

                final String authtoken = KusssHandler.getInstance().login(activity, mUserName, mUserPass);
                KusssHandler.getInstance().logout(activity);

                data.putString(AccountManager.KEY_ACCOUNT_NAME, mUserName);
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);
                data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
                data.putString(PARAM_USER_PASS, mUserPass);

            } catch (Exception e) {
                data.putString(KEY_ERROR_MESSAGE, e.getMessage());
            }

            final Intent res = new Intent();
            res.putExtras(data);
            return res;
        }

        @Override
        protected void onPostExecute(Intent intent) {
            KusssAuthenticatorActivity activity = activityReference.get();

            String message = null;
            if (TextUtils.isEmpty(intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME))) {
                message = activity.getString(R.string.account_login_failed_wrong_user);
            } else if (TextUtils.isEmpty(intent.getStringExtra(PARAM_USER_PASS))) {
                message = activity.getString(R.string.account_login_failed_wrong_pwd);
            } else if (!TextUtils.isEmpty(intent.getStringExtra(KEY_ERROR_MESSAGE))) {
                message = intent.getStringExtra(KEY_ERROR_MESSAGE);
            } else if (intent.hasExtra(AccountManager.KEY_AUTHTOKEN)) {
                String authToken = intent
                        .getStringExtra(AccountManager.KEY_AUTHTOKEN);
                if (!TextUtils.isEmpty(authToken)) {
                    activity.finishLogin(intent);
                } else {
                    message = activity.getString(R.string.account_login_failed_wrong_pwd);
                }
            } else {
                message = activity.getString(R.string.account_login_failed_wrong_auth_token);
            }
            if (!TextUtils.isEmpty(message)) {
                Toast.makeText(activity,
                        message,
                        Toast.LENGTH_SHORT).show();
            }

            progressDialog.dismiss();

            activity.getSubmit().setEnabled(true);
        }
    }

    protected void finishLogin(Intent intent) {
        String accountName = intent
                .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        String accountType = intent
                .getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

        logger.info("finish login to {}", accountName);

        Account account;
        boolean addNewAccount;

        final Account oldAccount = AppUtils.getAccount(KusssAuthenticatorActivity.this);
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
            long interval = PreferenceHelper.getSyncInterval(KusssAuthenticatorActivity.this) * 60L * 60;

            ContentResolver.addPeriodicSync(account,
                    CalendarContract.AUTHORITY, new Bundle(),
                    interval);
            ContentResolver.addPeriodicSync(account,
                    KusssContentContract.AUTHORITY, new Bundle(),
                    interval);
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account,
                    CalendarContract.AUTHORITY, 1);
            ContentResolver.setIsSyncable(account,
                    KusssContentContract.AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync
            // when the network is up
            ContentResolver.setSyncAutomatically(account,
                    CalendarContract.AUTHORITY, true);
            ContentResolver.setSyncAutomatically(account,
                    KusssContentContract.AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system
            // may modify this based
            // on other scheduled syncs and network utilization.
        } else {
            mAccountManager.setPassword(account, accountPassword);
        }

        // Kalender aktualisieren
        CalendarUtils.createCalendarsIfNecessary(KusssAuthenticatorActivity.this, account);

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        finish();

        AppUtils.enableSync(KusssAuthenticatorActivity.this, true);
        AppUtils.triggerSync(KusssAuthenticatorActivity.this, true, Consts.ARG_WORKER_CAL_COURSES, Consts.ARG_WORKER_CAL_COURSES, Consts.ARG_WORKER_KUSSS_CURRICULA, Consts.ARG_WORKER_KUSSS_COURSES, Consts.ARG_WORKER_KUSSS_ASSESSMENTS, Consts.ARG_WORKER_KUSSS_EXAMS);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (UIUtils.handleUpNavigation(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        AnalyticsHelper.sendScreen(Consts.SCREEN_LOGIN);
    }

    protected Button getSubmit() {
        return mSubmit;
    }
}
