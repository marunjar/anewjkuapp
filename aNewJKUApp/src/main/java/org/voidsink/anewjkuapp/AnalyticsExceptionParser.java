package org.voidsink.anewjkuapp;

import android.content.Context;

import com.google.android.gms.analytics.StandardExceptionParser;

import java.util.Collection;

/**
 * Created by paul on 13.09.2014.
 */
public class AnalyticsExceptionParser extends StandardExceptionParser {

    public AnalyticsExceptionParser(Context context, Collection<String> additionalPackages) {
        super(context, additionalPackages);
    }

    @Override
    public String getDescription(String threadName, Throwable t) {
        return super.getDescription(threadName, t);
    }
}
