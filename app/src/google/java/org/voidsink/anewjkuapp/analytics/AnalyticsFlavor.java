/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2020 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp.analytics;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voidsink.anewjkuapp.BuildConfig;
import org.voidsink.anewjkuapp.PreferenceHelper;

import java.util.List;

public class AnalyticsFlavor implements IAnalytics {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsFlavor.class);

    private FirebaseAnalytics mAnalytics = null;
    private FirebaseCrashlytics mCrashlytics = null;

    private Application mApp = null;
    private PlayServiceStatus mPlayServiceStatus = PlayServiceStatus.PS_NOT_AVAILABLE;

    @Override
    public void init(@NonNull Application app) {
        if (mApp == null) {
            mApp = app;

            if (FirebaseApp.getApps(app).isEmpty()) {
                FirebaseApp.initializeApp(app);
            }

            try {
                ProviderInstaller.installIfNeeded(mApp);
                mPlayServiceStatus = PlayServiceStatus.PS_INSTALLED;
            } catch (GooglePlayServicesRepairableException e) {
                mPlayServiceStatus = PlayServiceStatus.PS_REPAIRABLE;
            } catch (GooglePlayServicesNotAvailableException e) {
                mPlayServiceStatus = PlayServiceStatus.PS_NOT_AVAILABLE;
            }

            mAnalytics = FirebaseAnalytics.getInstance(app);
            if (BuildConfig.DEBUG) {
                mAnalytics.resetAnalyticsData();
            }

            mCrashlytics = FirebaseCrashlytics.getInstance();

            setEnabled(PreferenceHelper.trackingErrors(mApp));
        }
    }

    @Override
    public void sendException(Context c, Exception e, boolean fatal, List<String> additionalData) {
        try {
            if (mCrashlytics != null && e != null) {
                mCrashlytics.setCustomKey("fatal", fatal);
                if (additionalData != null) {
                    for (String value : additionalData) {
                        if (!TextUtils.isEmpty(value)) {
                            mCrashlytics.log(value.substring(0, Math.min(value.length(), 4096)));
                        }
                    }
                }
                mCrashlytics.recordException(e);
            }
        } catch (Exception e2) {
            logger.error("sendException", e2);
        }
        if (e != null) {
            logger.error("", e);
            logger.error("{} (fatal={}, {})", e.getClass().getSimpleName(), fatal, additionalData);
        }
    }

    @Override
    public void sendScreen(String screenName) {
        if (mAnalytics != null) {
            Bundle params = new Bundle();

            params.putString(FirebaseAnalytics.Param.SCREEN_NAME, TextUtils.isEmpty(screenName) ? null : screenName.substring(0, Math.min(screenName.length(), 36)));

            mAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params);

        }
        logger.debug("screen: {}", screenName);
    }

    @Override
    public void sendButtonEvent(String label) {
        if (mAnalytics != null && !TextUtils.isEmpty(label)) {
            Bundle params = new Bundle();

            params.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "button");
            params.putString(FirebaseAnalytics.Param.ITEM_NAME, label);

            mAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
        }
        logger.debug("buttonEvent: {}", label);
    }

    @Override
    public void sendPreferenceChanged(String key, String value) {
        if (mAnalytics != null && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            Bundle params = new Bundle();

            params.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "preference");
            params.putString(FirebaseAnalytics.Param.ITEM_NAME, key);
            params.putString(FirebaseAnalytics.Param.VALUE, value);

            mAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
        }
        logger.debug("preferenceChanged: {}={}", key, value);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mApp != null) {
            logger.debug("setEnabled: {}", enabled);
            if (mAnalytics != null) {
                mAnalytics.setAnalyticsCollectionEnabled(enabled);
            }

            if (mCrashlytics != null) {
                mCrashlytics.setCrashlyticsCollectionEnabled(enabled);
            }
        }
    }

    @Override
    public PlayServiceStatus getPsStatus() {
        return mPlayServiceStatus;
    }
}
