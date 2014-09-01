package org.voidsink.anewjkuapp.service;

import org.voidsink.anewjkuapp.KusssCalendarSyncAdapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class KusssCalendarSyncService extends Service {

    // Storage for an instance of the sync adapter
    private static KusssCalendarSyncAdapter sSyncAdapter = null;
    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();
    /*
     * Instantiate the sync adapter object.
     */
    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new KusssCalendarSyncAdapter(getApplicationContext(), true);
            }
        }
    }
    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return sSyncAdapter.getSyncAdapterBinder();
    }

}
