/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2020 Paul "Marunjar" Pretsch
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

package org.voidsink.anewjkuapp.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.utils.UIUtils;
import org.voidsink.sectionedrecycleradapter.SectionedAdapter;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarEventAdapter extends RecyclerArrayAdapter<CalendarListEvent, CalendarEventAdapter.EventItemHolder> implements SectionedAdapter<CalendarEventAdapter.DateHeaderHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, int viewType, int position);
    }

    private OnItemClickListener mItemClickListener;

    public CalendarEventAdapter(Context context) {
        super(context);
    }

    static class EventItemHolder extends RecyclerView.ViewHolder {

        private final Toolbar mToolbar;
        private final TextView mTitle;
        private final TextView mDescr;
        private final TextView mLocation;
        private final TextView mTime;

        EventItemHolder(View itemView) {
            super(itemView);

            mToolbar = itemView.findViewById(R.id.calendar_list_item_toolbar);
            mToolbar.inflateMenu(R.menu.calendar_card_popup_menu);

            mTitle = itemView.findViewById(R.id.calendar_list_item_title);
            mDescr = itemView.findViewById(R.id.calendar_list_item_descr);
            mTime = itemView.findViewById(R.id.calendar_list_item_time);
            mLocation = itemView.findViewById(R.id.calendar_list_item_location);
        }
    }

    static class DateHeaderHolder extends RecyclerView.ViewHolder {
        private final TextView mText;

        DateHeaderHolder(View itemView) {
            super(itemView);

            mText = itemView.findViewById(R.id.list_header_text);
        }
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @NonNull
    @Override
    public EventItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_list_item, parent, false);
        final EventItemHolder vh = new EventItemHolder(view);

        view.setOnClickListener(v -> {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, vh.getItemViewType(), vh.getAdapterPosition());
            }
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull EventItemHolder holder, int position) {
        final CalendarListEvent eventItem = getItem(position);

        if (eventItem != null) {
            holder.mToolbar.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.show_in_calendar: {
                        eventItem.showInCalendar(getContext());
                        return true;
                    }
                    case R.id.show_on_map: {
                        eventItem.showOnMap(getContext());
                        return true;
                    }
                }
                return false;
            });

            holder.mTitle.setText(eventItem.getTitle());

            UIUtils.setTextAndVisibility(holder.mDescr, eventItem.getDescr());
            UIUtils.setTextAndVisibility(holder.mTime, eventItem.getTime());
            UIUtils.setTextAndVisibility(holder.mLocation, eventItem.getLocation());
        }
    }

    @Override
    public long getHeaderId(int position) {
        CalendarListEvent e = getItem(position);

        if (e != null) {
            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTimeInMillis(e.getDtStart());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        }
        return 0;
    }

    @Override
    public DateHeaderHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_header, viewGroup, false);
        return new DateHeaderHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(DateHeaderHolder dateHeaderHolder, int position) {
        CalendarListEvent e = getItem(position);

        if (e != null) {
            dateHeaderHolder.mText.setText(DateFormat.getDateInstance().format(new Date(e.getDtStart())));
        } else {
            dateHeaderHolder.mText.setText("");
        }
    }
}
