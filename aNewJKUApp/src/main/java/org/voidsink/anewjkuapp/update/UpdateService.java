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

package org.voidsink.anewjkuapp.update;

import android.accounts.Account;
import android.app.IntentService;
import android.content.Intent;

import org.voidsink.anewjkuapp.analytics.Analytics;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
