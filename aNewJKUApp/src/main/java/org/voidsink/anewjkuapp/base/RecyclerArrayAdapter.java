/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2018 Paul "Marunjar" Pretsch
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

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;

import androidx.recyclerview.widget.RecyclerView;


public abstract class RecyclerArrayAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {
    private final ArrayList<T> items = new ArrayList<>();
    private final Context mContext;

    public RecyclerArrayAdapter(Context context) {
        super();
        this.mContext = context;
        setHasStableIds(true);
    }

    protected Context getContext() {
        return mContext;
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
            items.addAll(collection);
            notifyDataSetChanged();
        }
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
        if (position < 0) return null;
        if (position >= getItemCount()) return null;

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
