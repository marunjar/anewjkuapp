/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2018 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp.update;

import android.Manifest;
import android.accounts.Account;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;

import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import androidx.core.content.ContextCompat;

public class UpdateService extends IntentService {

    public UpdateService(String name) {
        super(name);
    }

    public UpdateService() {
        this(UpdateService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final Account account = AppUtils.getAccount(this);
            if (account != null) {
                List<Callable<?>> callables = new ArrayList<>();

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                    if (intent.getBooleanExtra(Consts.ARG_UPDATE_CAL, false) ||
                            intent.getBooleanExtra(Consts.ARG_UPDATE_CAL_COURSES, false)) {
                        Analytics.eventReloadEventsCourse(this);
                        callables.add(new ImportCalendarTask(account, this, CalendarUtils.ARG_CALENDAR_COURSE, CalendarUtils.newCalendarBuilder()));
                    }
                    if (intent.getBooleanExtra(Consts.ARG_UPDATE_CAL, false) ||
                            intent.getBooleanExtra(Consts.ARG_UPDATE_CAL_EXAM, false)) {
                        Analytics.eventReloadEventsExam(this);
                        callables.add(new ImportCalendarTask(account, this,
                                CalendarUtils.ARG_CALENDAR_EXAM, CalendarUtils.newCalendarBuilder()));
                    }
                }
                if (intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS, false) ||
                        intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS_CURRICULA, false)) {
                    Analytics.eventReloadCurricula(this);
                    callables.add(new ImportCurriculaTask(account, this));
                }
                if (intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS, false) ||
                        intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS_COURSES, false)) {
                    Analytics.eventReloadCourses(this);
                    callables.add(new ImportCourseTask(account, this));
                }
                if (intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS, false) ||
                        intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS_ASSESSMENTS, false)) {
                    Analytics.eventReloadAssessments(this);
                    callables.add(new ImportAssessmentTask(account, this));
                }
                if (intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS, false) ||
                        intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS_EXAMS, false)) {
                    Analytics.eventReloadExams(this);
                    callables.add(new ImportExamTask(account, this));
                }
                if (callables.size() > 0) {
                    AppUtils.executeEm(this, callables.toArray(new Callable<?>[0]), false);
                }
            }
        }
    }
}
