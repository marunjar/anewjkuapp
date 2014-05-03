package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.activity.KusssAuthenticatorActivity;
import org.voidsink.anewjkuapp.calendar.CalendarContractWrapper;
import org.voidsink.anewjkuapp.kusss.KusssHandler;
import org.voidsink.anewjkuapp.provider.KusssDatabaseHelper;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

public class KusssAuthenticator extends AbstractAccountAuthenticator {

	// The authority for the sync adapter's content provider
	// public static final String AUTHORITY_CALENDAR =
	// "org.voidsink.anewjkuapp.calendar.provider";
	// An account type, in the form of a domain name
	public static final String ACCOUNT_TYPE = "org.voidsink.anewjkuapp.account";

	public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
	public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an Udinic account";

	public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
	public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an Udinic account";

	public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
	public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
	public final static String ARG_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

	private Context mContext = null;

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
		intent.putExtra(ARG_NEW_ACCOUNT, true);
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
		if (!authTokenType.equals(AUTHTOKEN_TYPE_READ_ONLY)
				&& !authTokenType.equals(AUTHTOKEN_TYPE_FULL_ACCESS)) {
			final Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ERROR_MESSAGE,
					"invalid authTokenType");
			return result;
		}

		// Extract the username and password from the Account Manager, and ask
		// the server for an appropriate AuthToken.
		final AccountManager am = AccountManager.get(mContext);

		String authToken = am.peekAuthToken(account, authTokenType);

		// Lets give another try to authenticate the user
		final String password = am.getPassword(account);
		if (password != null) {
			try {
				if (KusssHandler.handler.login(account.name, password)) {
					authToken = account.name;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// If we get an authToken - we return it
		if (!TextUtils.isEmpty(authToken)) {
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
		if (AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
			return AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
		else if (AUTHTOKEN_TYPE_READ_ONLY.equals(authTokenType))
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
		intent.putExtra(ARG_NEW_ACCOUNT, false);
		intent.putExtra(ARG_ACCOUNT_NAME, account.name);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
				response);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}
	
	@Override
	public Bundle getAccountRemovalAllowed(
			AccountAuthenticatorResponse response, Account account)
			throws NetworkErrorException {
		final Bundle result = super.getAccountRemovalAllowed(response, account);
		
		if (result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT)) {
			KusssDatabaseHelper.drop(mContext);
		}
		return result;
	}

	public static void TriggerRefresh(Context context) {
		Account[] accounts = AccountManager.get(context).getAccountsByType(
				ACCOUNT_TYPE);
		for (Account account : accounts) {
			Bundle b = new Bundle();
			// Disable sync backoff and ignore sync preferences. In other
			// words...perform sync NOW!
			b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
			ContentResolver.requestSync(account, // Sync
					CalendarContractWrapper.AUTHORITY(), // Content authority
					b); // Extras
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
				.appendQueryParameter(CalendarContractWrapper.CALLER_IS_SYNCADAPTER(),
						"true")
				.appendQueryParameter(CalendarContractWrapper.Calendars.ACCOUNT_NAME(),
						account)
				.appendQueryParameter(CalendarContractWrapper.Calendars.ACCOUNT_TYPE(),
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
				.appendQueryParameter(CalendarContractWrapper.CALLER_IS_SYNCADAPTER(),
						"true")
				.appendQueryParameter(CalendarContractWrapper.Events.ACCOUNT_NAME(),
						account)
				.appendQueryParameter(CalendarContractWrapper.Events.ACCOUNT_TYPE(),
						accountType).build();
	}

}
