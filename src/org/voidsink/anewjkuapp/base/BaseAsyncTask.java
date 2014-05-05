package org.voidsink.anewjkuapp.base;

import android.os.AsyncTask;

public abstract class BaseAsyncTask<T1, T2, T3> extends AsyncTask<T1, T2, T3> {
	
	private boolean mImportDone = false;
	
	public boolean isDone() {
		return mImportDone;
	}
	
	protected void setImportDone(boolean done) {
		this.mImportDone = done;
	}
	
}
