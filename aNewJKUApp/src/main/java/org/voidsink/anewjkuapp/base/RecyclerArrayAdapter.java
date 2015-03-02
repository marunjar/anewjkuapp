package org.voidsink.anewjkuapp.base;

import android.os.Build;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


public abstract class RecyclerArrayAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {
    private ArrayList<T> items = new ArrayList<T>();

    public RecyclerArrayAdapter() {
        super();
        setHasStableIds(true);
    }

    public void add(T object) {
        items.add(object);
        notifyDataSetChanged();
    }

    public void add(int index, T object) {
        items.add(index, object);
        notifyDataSetChanged();
    }

    public void addAll(Collection<? extends T> collection) {
        if (collection != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                items.addAll(collection);
            } else {
                for (T item : collection) {
                    items.add(item);
                }
            }
            notifyDataSetChanged();
        }
    }

    public void addAll(T... items) {
        addAll(Arrays.asList(items));
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void remove(T object) {
        items.remove(object);
        notifyDataSetChanged();
    }

    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
