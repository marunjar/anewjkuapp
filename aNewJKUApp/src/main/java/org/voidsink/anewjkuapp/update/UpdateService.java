package org.voidsink.anewjkuapp.update;

import android.accounts.Account;
import android.app.IntentService;
import android.content.Intent;

import net.fortuna.ical4j.data.CalendarBuilder;

import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.utils.Analytics;
import org.voidsink.anewjkuapp.utils.AppUtils;

public class UpdateService extends IntentService {

    public static final String UPDATE_TYPE = "UPDATE_TYPE";
    public static final int UPDATE_CAL_LVA = 1;
    public static final int UPDATE_CAL_EXAM = 2;
    public static final int UPDATE_EXAMS = 3;
    public static final int UPDATE_LVAS = 4;
    public static final int UPDATE_GRADES = 5;

    public UpdateService(String name) {
        super(name);
    }

    public UpdateService() {
        this(UpdateService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(UPDATE_TYPE)) {
            final Account account = AppUtils.getAccount(this);
            if (account != null) {
                switch (intent.getIntExtra(UPDATE_TYPE, 0)) {
                    case UPDATE_CAL_LVA: {
                        Analytics.eventReloadEventsLva(this);
                        new ImportCalendarTask(account, this,
                                CalendarUtils.ARG_CALENDAR_LVA, new CalendarBuilder())
                                .execute();
                        break;
                    }
                    case UPDATE_CAL_EXAM: {
                        Analytics.eventReloadEventsExam(this);
                        new ImportCalendarTask(account, this,
                                CalendarUtils.ARG_CALENDAR_EXAM, new CalendarBuilder())
                                .execute();
                        break;
                    }
                    case UPDATE_EXAMS: {
                        Analytics.eventReloadExams(this);
                        new ImportExamTask(account, this).execute();
                        break;
                    }
                    case UPDATE_LVAS: {
                        Analytics.eventReloadLvas(this);
                        new ImportLvaTask(account, this).execute();
                        break;
                    }
                    case UPDATE_GRADES: {
                        Analytics.eventReloadGrades(this);
                        new ImportGradeTask(account, this).execute();
                        break;
                    }
                }
            }
        }
    }


}
