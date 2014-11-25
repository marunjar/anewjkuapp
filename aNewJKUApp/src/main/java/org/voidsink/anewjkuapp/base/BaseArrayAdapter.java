package org.voidsink.anewjkuapp.base;

import java.util.Collection;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.widget.ArrayAdapter;

public class BaseArrayAdapter<T> extends ArrayAdapter<T> {

    public BaseArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public BaseArrayAdapter(Context context, int resource, int textViewResourceId) {
    	super(context, resource, textViewResourceId);
    }

    public BaseArrayAdapter(Context context, int textViewResourceId, T[] objects) {
        super(context, textViewResourceId, objects);
    }

    public BaseArrayAdapter(Context context, int resource, int textViewResourceId, T[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public BaseArrayAdapter(Context context, int textViewResourceId, List<T> objects) {
        super(context, textViewResourceId, objects);
    }

    public BaseArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);
    }


	@SuppressLint("NewApi")
	@Override
    public void addAll(Collection<? extends T> collection) {
        if (collection != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                super.addAll(collection);
            } else {
                for (T item : collection) {
                    super.add(item);
                }
            }
        }
    }

	@SuppressWarnings("unchecked")
	@SuppressLint("NewApi")
	@Override
    public void addAll(T ... items) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			super.addAll(items);
		} else {
			for (T item : items) {
				super.add(item);
			}
		}
    }

}
