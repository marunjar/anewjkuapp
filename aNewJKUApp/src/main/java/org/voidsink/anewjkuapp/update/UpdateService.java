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
                        intent.getBooleanExtra(Consts.ARG_UPDATE_CAL_COURSES, false)) {
                    Analytics.eventReloadEventsCourse(this);
                    new ImportCalendarTask(account, this,
                            CalendarUtils.ARG_CALENDAR_COURSE, new CalendarBuilder())
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
                        intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS_CURRICULA, false)) {
                    Analytics.eventReloadCurricula(this);
                    new ImportCurriculaTask(account, this).execute();
                }
                if (intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS, false) ||
                        intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS_COURSES, false)) {
                    Analytics.eventReloadCourses(this);
                    new ImportCourseTask(account, this).execute();
                }
                if (intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS, false) ||
                        intent.getBooleanExtra(Consts.ARG_UPDATE_KUSSS_ASSESSMENTS, false)) {
                    Analytics.eventReloadAssessments(this);
                    new ImportAssessmentTask(account, this).execute();
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
