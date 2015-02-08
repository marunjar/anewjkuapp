package org.voidsink.anewjkuapp.update;

import android.accounts.Account;
import android.app.IntentService;
import android.content.Intent;

import net.fortuna.ical4j.data.CalendarBuilder;

import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.AppUtils;
import org.voidsink.anewjkuapp.utils.Consts;

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
                if (intent.getBooleanExtra(Consts.ARG_UPDATE_CAL, false) ||
                        intent.getBooleanExtra(Consts.ARG_UPDATE_CAL_LVA, false)) {
                    Analytics.eventReloadEventsLva(this);
                    new ImportCalendarTask(account, this,
                            CalendarUtils.ARG_CALENDAR_LVA, new CalendarBuilder())
                            .execute();
                }
                if (intent.getBooleanExtra(Consts.ARG_UPDATE_CAL, false) ||
                        intent.getBooleanExtra(Consts.ARG_UPDATE_CAL_EXAM, false)) {
                    Analytics.eventReloadEventsExam(this);
                    new ImportCalendarTask(account, this,
                            CalendarUtils.ARG_CALENDAR_EXAM, new CalendarBuilder())
                            .execute();
                }
                if (intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS, false) ||
                        intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS_STUDIES, false)) {
                    Analytics.eventReloadStudies(this);
                    new ImportStudiesTask(account, this).execute();
                }
                if (intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS, false) ||
                        intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS_LVAS, false)) {
                    Analytics.eventReloadLvas(this);
                    new ImportLvaTask(account, this).execute();
                }
                if (intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS, false) ||
                        intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS_GRADES, false)) {
                    Analytics.eventReloadGrades(this);
                    new ImportGradeTask(account, this).execute();
                }
                if (intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS, false) ||
                        intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS_EXAMS, false)) {
                    Analytics.eventReloadExams(this);
                    new ImportExamTask(account, this).execute();
                }
            }
        }
    }
}
