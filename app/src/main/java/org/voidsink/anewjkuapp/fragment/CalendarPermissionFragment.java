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

package org.voidsink.anewjkuapp.fragment;

import android.Manifest;
import android.accounts.Account;
import android.content.UriMatcher;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseContentObserver;
import org.voidsink.anewjkuapp.base.BaseFragment;
import org.voidsink.anewjkuapp.base.ContentObserverListener;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.utils.AppUtils;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CalendarPermissionFragment extends BaseFragment implements ContentObserverListener {

    private static final int PERMISSIONS_REQUEST_READ_CALENDAR = 0;
    private static final int PERMISSIONS_REQUEST_FULL_CALENDAR = 1;

    private static final String[] CALENDAR_PERMISSIONS_READ = {Manifest.permission.READ_CALENDAR};
    private static final String[] CALENDAR_PERMISSIONS_FULL = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};

    private BaseContentObserver mDataObserver;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        startCreateCalendars();
    }

    @Override
    public void onStart() {
        super.onStart();

        initDataObserver();
    }

    protected boolean hasCalendarPermission() {
        return EasyPermissions.hasPermissions(getContext(), CALENDAR_PERMISSIONS_READ);
    }

    @AfterPermissionGranted(PERMISSIONS_REQUEST_READ_CALENDAR)
    private void initDataObserver() {
        mDataObserver = null;
        if (hasCalendarPermission()) {
            // check permission
            UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            uriMatcher.addURI(CalendarContract.AUTHORITY, CalendarContract.Events.CONTENT_URI.buildUpon().appendPath("#").build().toString(), 0);

            mDataObserver = new BaseContentObserver(uriMatcher, this);

            // listen to all changes
            getContext().getContentResolver().registerContentObserver(
                    CalendarContract.Events.CONTENT_URI.buildUpon()
                            .appendPath("#").build(), false, mDataObserver);

            onContentChanged(true);
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.alert_permission_read_calendar),
                    PERMISSIONS_REQUEST_READ_CALENDAR,
                    CALENDAR_PERMISSIONS_READ);

        }
    }

    @AfterPermissionGranted(PERMISSIONS_REQUEST_FULL_CALENDAR)
    private void startCreateCalendars() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) || EasyPermissions.hasPermissions(getContext(), Manifest.permission.GET_ACCOUNTS)) {
            if (EasyPermissions.hasPermissions(getContext(), CALENDAR_PERMISSIONS_FULL)) {
                Account account = AppUtils.getAccount(getContext());
                CalendarUtils.createCalendarsIfNecessary(getContext(), account);
            } else {
                EasyPermissions.requestPermissions(
                        this,
                        getString(R.string.alert_permission_write_calendar),
                        PERMISSIONS_REQUEST_FULL_CALENDAR,
                        CALENDAR_PERMISSIONS_FULL);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mDataObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(
                    mDataObserver);
            mDataObserver = null;
        }
    }

    @Override
    public void onContentChanged(boolean selfChange) {

    }
}
