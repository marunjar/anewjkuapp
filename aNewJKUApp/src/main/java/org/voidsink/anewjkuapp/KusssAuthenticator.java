/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2019 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.activity.KusssAuthenticatorActivity;
import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;
import org.voidsink.anewjkuapp.utils.AppUtils;

public class KusssAuthenticator extends AbstractAccountAuthenticator {

    // The authority for the sync adapter's content provider
    // public static final String AUTHORITY_CALENDAR =
    // "org.voidsink.anewjkuapp.calendar.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = BuildConfig.APPLICATION_ID + ".account";

    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    private static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to a KUSSS account";

    // public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    // public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL =
    // "Full access to a KUSSS account";

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    private final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    private static final Logger logger = LoggerFactory.getLogger(KusssAuthenticator.class);

    private final Context mContext;

    public KusssAuthenticator(Context context) {
        super(context);

        this.mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
                             String accountType, String authTokenType,
                             String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        final Intent intent = new Intent(mContext,
                KusssAuthenticatorActivity.class);
        intent.putExtra(ARG_ACCOUNT_TYPE, accountType);
        intent.putExtra(ARG_AUTH_TYPE, authTokenType);
        intent.putExtra(ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
                                     Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
                                 String accountType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
                               Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        // If the caller requested an authToken type we don't support, then
        // return an error
        if (!authTokenType.equals(AUTHTOKEN_TYPE_READ_ONLY)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE,
                    "invalid authTokenType");
            return result;
        }

        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        final AccountManager am = AccountManager.get(mContext);

        String authToken = am.peekAuthToken(account, authTokenType);

        logger.debug("authToken={}", authToken);

        if (TextUtils.isEmpty(authToken)
                || !KusssHandler.getInstance().isLoggedIn(mContext, authToken)) {
            // Lets give another try to authenticate the user
            final String password = am.getPassword(account);
            if (password != null) {
                try {
                    authToken = KusssHandler.getInstance().login(mContext, account.name,
                            password);
                } catch (Exception e) {
                    authToken = null;
                }
            }
        }

        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            // set new auth token (SessionID)
            AccountManager.get(mContext).setAuthToken(account, authTokenType, authToken);

            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        final Intent intent = new Intent(mContext,
                KusssAuthenticatorActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                response);
        intent.putExtra(ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(ARG_ACCOUNT_NAME, account.name);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        /*
         * if (AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType)) return
         * AUTHTOKEN_TYPE_FULL_ACCESS_LABEL; else
         */
        if (AUTHTOKEN_TYPE_READ_ONLY.equals(authTokenType))
            return AUTHTOKEN_TYPE_READ_ONLY_LABEL;
        else
            return authTokenType + " (Label)";
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
                              Account account, String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
                                    Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {

        final Intent intent = new Intent(mContext,
                KusssAuthenticatorActivity.class);
        intent.putExtra(ARG_ACCOUNT_TYPE, account.type);
        intent.putExtra(ARG_IS_ADDING_NEW_ACCOUNT, false);
        intent.putExtra(ARG_ACCOUNT_NAME, account.name);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public @NonNull
    Bundle getAccountRemovalAllowed(
            AccountAuthenticatorResponse response, Account account)
            throws NetworkErrorException {
        final Bundle result = super.getAccountRemovalAllowed(response, account);

        if (result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT)) {
            KusssDatabaseHelper.dropUserData(mContext);
        }
        return result;
    }

    public static void triggerSync(Context context) {
        try {
            Account[] accounts = AccountManager.get(context).getAccountsByType(
                    ACCOUNT_TYPE);
            for (Account account : accounts) {
                AppUtils.triggerSync(context, account, true);
                AppUtils.syncCalendars(context, true);
                AppUtils.syncCurricula(context, true);
            }
        } catch (SecurityException e) {
            Analytics.sendException(context, e, true);
        } catch (Exception e) {
            Analytics.sendException(context, e, false);
        }
    }

    /**
     * Creates an updated URI that includes query parameters that identify the
     * source as a sync adapter.
     */
    public static Uri asCalendarSyncAdapter(Uri uri, String account,
                                            String accountType) {
        return uri
                .buildUpon()
                .appendQueryParameter(
                        CalendarContractWrapper.CALLER_IS_SYNCADAPTER(), "true")
                .appendQueryParameter(
                        CalendarContractWrapper.Calendars.ACCOUNT_NAME(),
                        account)
                .appendQueryParameter(
                        CalendarContractWrapper.Calendars.ACCOUNT_TYPE(),
                        accountType).build();
    }

    /**
     * Creates an updated URI that includes query parameters that identify the
     * source as a sync adapter.
     */
    public static Uri asEventSyncAdapter(Uri uri, String account,
                                         String accountType) {
        return uri
                .buildUpon()
                .appendQueryParameter(
                        CalendarContractWrapper.CALLER_IS_SYNCADAPTER(), "true")
                .appendQueryParameter(
                        CalendarContractWrapper.Events.ACCOUNT_NAME(), account)
                .appendQueryParameter(
                        CalendarContractWrapper.Events.ACCOUNT_TYPE(),
                        accountType).build();
    }

}
