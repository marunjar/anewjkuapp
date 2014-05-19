package org.voidsink.anewjkuapp.base;

import android.annotation.TargetApi;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

public class BaseContentObserver extends ContentObserver {

	private static final String TAG = BaseContentObserver.class.getSimpleName();
	private UriMatcher uriMatcher;
	private ContentObserverListener listener;

	public BaseContentObserver(Handler handler, UriMatcher uriMatcher,
			ContentObserverListener listener) {
		super(handler);

		this.uriMatcher = uriMatcher;
		this.listener = listener;
	}

	public BaseContentObserver(Handler handler, ContentObserverListener listener) {
		this(handler, null, listener);
	}

	public BaseContentObserver(ContentObserverListener listener) {
		this(new Handler(), listener);
	}

	public BaseContentObserver(UriMatcher uriMatcher,
			ContentObserverListener listener) {
		this(new Handler(), uriMatcher, listener);
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		if (listener != null) {
			listener.onContentChanged(selfChange);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onChange(boolean selfChange, Uri uri) {
		if (uriMatcher != null) {
			if (uriMatcher.match(uri) != UriMatcher.NO_MATCH) {
				super.onChange(selfChange, uri);
			} else {
				Log.w(TAG, "onChange(" + selfChange + ", " + uri.toString() + "): ignored");
			}
		} else {
			super.onChange(selfChange, uri);
		}
	}

}
