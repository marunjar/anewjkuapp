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

package org.voidsink.anewjkuapp.base;

import android.content.UriMatcher;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseContentObserver extends ContentObserver {

    private static final Logger logger = LoggerFactory.getLogger(BaseContentObserver.class);

    private final UriMatcher uriMatcher;
    private final ContentObserverListener listener;

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

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        if (uriMatcher != null) {
            if (uriMatcher.match(uri) != UriMatcher.NO_MATCH) {
                super.onChange(selfChange, uri);
            } else {
                logger.warn("onChange({}, {}): ignored", selfChange, uri.toString());
            }
        } else {
            super.onChange(selfChange, uri);
        }
    }

}
