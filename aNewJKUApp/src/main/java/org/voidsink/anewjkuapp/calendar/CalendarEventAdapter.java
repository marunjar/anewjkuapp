package org.voidsink.anewjkuapp.calendar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.RecyclerArrayAdapter;
import org.voidsink.anewjkuapp.utils.UIUtils;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarEventAdapter extends RecyclerArrayAdapter<CalendarListEvent, CalendarEventAdapter.EventItemHolder> implements StickyRecyclerHeadersAdapter<CalendarEventAdapter.DateHeaderHolder> {

    private final Context mContext;
    private OnItemClickListener mItemClickListener;

    public CalendarEventAdapter(Context context) {
        super();

        mContext = context;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int viewType, int position);
    }

    protected static class EventItemHolder extends RecyclerView.ViewHolder {

        public final Toolbar mToolbar;
        public final TextView mTitle;
        public final TextView mDescr;
        public final TextView mLocation;
        public final TextView mTime;

        public EventItemHolder(View itemView) {
            super(itemView);

            mToolbar = (Toolbar) itemView.findViewById(R.id.calendar_list_item_toolbar);
            mToolbar.inflateMenu(R.menu.calendar_card_popup_menu);

            mTitle = (TextView) itemView.findViewById(R.id.calendar_list_item_title);
            mDescr = (TextView) itemView.findViewById(R.id.calendar_list_item_descr);
            mTime = (TextView) itemView.findViewById(R.id.calendar_list_item_time);
            mLocation = (TextView) itemView.findViewById(R.id.calendar_list_item_location);
        }
    }

    protected static class DateHeaderHolder extends RecyclerView.ViewHolder {
        public final TextView mText;

        public DateHeaderHolder(View itemView) {
            super(itemView);

            mText = (TextView) itemView.findViewById(R.id.list_header_text);
        }
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public EventItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_list_item, parent, false);
        final EventItemHolder vh = new EventItemHolder(v);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(v, vh.getItemViewType(), vh.getPosition());
                }
            }
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(EventItemHolder holder, int position) {
        final CalendarListEvent eventItem = getItem(position);

        if (eventItem != null) {
            holder.mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.show_in_calendar: {
                            eventItem.showInCalendar(mContext);
                            return true;
                        }
                        case R.id.show_on_map: {
                            eventItem.showOnMap(mContext);
                            return true;
                        }
                    }
                    return false;
                }
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
