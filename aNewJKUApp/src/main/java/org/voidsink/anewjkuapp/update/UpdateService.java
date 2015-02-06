package org.voidsink.anewjkuapp.update;

import android.accounts.Account;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.util.Log;

import net.fortuna.ical4j.data.CalendarBuilder;

import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.utils.Analytics;

public class UpdateService extends IntentService {

    public static final String UPDATE_TYPE = "UPDATE_TYPE";
    public static final String UPDATE_ACCOUNT = "UPDATE_ACCOUNT";
    public static final int UPDATE_CAL_LVA = 1;
    public static final int UPDATE_CAL_EXAM = 2;
    public static final int UPDATE_EXAMS = 3;

    public UpdateService(String name) {
        super(name);
    }

    public UpdateService() {
        this(UpdateService.class.getSimpleName());
    }

        @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(UPDATE_TYPE)) {
            Account account = intent.getParcelableExtra(UPDATE_ACCOUNT);
            switch (intent.getIntExtra(UPDATE_TYPE, 0)) {
                case UPDATE_CAL_LVA: {
                    new ImportCalendarTask(account, this,
                            CalendarUtils.ARG_CALENDAR_LVA, new CalendarBuilder())
                            .execute();
                }
                case UPDATE_CAL_EXAM: {
                    new ImportCalendarTask(account, this,
                            CalendarUtils.ARG_CALENDAR_EXAM, new CalendarBuilder())
                            .execute();
                }
                case UPDATE_EXAMS: {
                    Analytics.eventReloadExams(this);
                    new ImportExamTask(account, this).execute();
                }
            }
        }
    }


}
