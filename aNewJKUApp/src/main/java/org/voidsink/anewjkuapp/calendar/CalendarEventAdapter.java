package org.voidsink.anewjkuapp.calendar;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.activity.MainActivity;
import org.voidsink.anewjkuapp.base.ListWithHeaderAdapter;
import org.voidsink.anewjkuapp.fragment.MapFragment;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarEventAdapter extends ListWithHeaderAdapter<CalendarListItem> {

    private final LayoutInflater mInflater;

    public CalendarEventAdapter(Context context) {
        super(context, R.layout.calendar_list_item);

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CalendarListItem item = this.getItem(position);
        if (item == null) {
            return null;
        }
        if (!item.isEvent()) {
            return null;
        }
        return getEventView(convertView, parent, item);
    }

    private View getEventView(View convertView, ViewGroup parent,
                              CalendarListItem item) {
        final CalendarListEvent eventItem = (CalendarListEvent) item;
        CalendarListEventHolder eventItemHolder = null;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.calendar_list_item, parent,
                    false);
            eventItemHolder = new CalendarListEventHolder();

            eventItemHolder.toolbar = (Toolbar) convertView.findViewById(R.id.calendar_list_item_toolbar);
            eventItemHolder.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.show_in_calendar: {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                Uri.Builder builder = CalendarContractWrapper.CONTENT_URI().buildUpon();
                                builder.appendPath("time");
                                ContentUris.appendId(builder, eventItem.getDtStart());
                                Intent intent = new Intent(Intent.ACTION_VIEW)
                                        .setData(builder.build());
                                getContext().startActivity(intent);
                            } else {
                                Uri uri = ContentUris.withAppendedId(CalendarContractWrapper.Events.CONTENT_URI(), eventItem.getEventId());
                                Intent intent = new Intent(Intent.ACTION_VIEW)
                                        .setData(uri);
                                getContext().startActivity(intent);
                            }
                            return true;
                        }
                        case R.id.show_on_map: {
                            Intent intent = new Intent(getContext(), MainActivity.class).putExtra(
                                    MainActivity.ARG_SHOW_FRAGMENT,
                                    MapFragment.class.getName()).setAction(
                                    Intent.ACTION_SEARCH).addFlags(
                                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            if (!TextUtils.isEmpty(eventItem.getLocation())) {
                                intent.putExtra(SearchManager.QUERY, eventItem.getLocation());
                                intent.putExtra(MainActivity.ARG_EXACT_LOCATION, true);
                            } else {
                                intent.putExtra(SearchManager.QUERY, "Uniteich");
                            }
                            getContext().startActivity(intent);
                            return true;
                        }
                    }
                    return false;
                }
            });

            eventItemHolder.toolbar.inflateMenu(R.menu.calendar_card_popup_menu);

            eventItemHolder.title = (TextView) convertView
                    .findViewById(R.id.calendar_list_item_title);
            eventItemHolder.descr = (TextView) convertView
                    .findViewById(R.id.calendar_list_item_descr);
            eventItemHolder.time = (TextView) convertView
                    .findViewById(R.id.calendar_list_item_time);
            eventItemHolder.location = (TextView) convertView
                    .findViewById(R.id.calendar_list_item_location);

            convertView.setTag(eventItemHolder);
        }

        if (eventItemHolder == null) {
            eventItemHolder = (CalendarListEventHolder) convertView.getTag();
        }

        eventItemHolder.title.setText(eventItem.getTitle());

        if (eventItem.getDescr().isEmpty()) {
            eventItemHolder.descr.setVisibility(View.GONE);
        } else {
            eventItemHolder.descr.setVisibility(View.VISIBLE);
            eventItemHolder.descr.setText(eventItem.getDescr());
        }

        if (eventItem.getTime().isEmpty()) {
            eventItemHolder.time.setVisibility(View.GONE);
        } else {
            eventItemHolder.time.setVisibility(View.VISIBLE);
            eventItemHolder.time.setText(eventItem.getTime());
        }

        if (eventItem.getLocation().isEmpty()) {
            eventItemHolder.location.setVisibility(View.GONE);
        } else {
            eventItemHolder.location.setVisibility(View.VISIBLE);
            eventItemHolder.location.setText(eventItem.getLocation());
        }

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).getType();
    }

    @Override
    public boolean isEnabled(int position) {
        return (getItem(position) instanceof CalendarListEvent);
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
        // Build your custom HeaderView
        final View headerView = mInflater.inflate(R.layout.list_header, null);

        final TextView tvHeaderTitle = (TextView) headerView.findViewById(R.id.list_header_text);
        CalendarListItem card = getItem(position);
        if (card instanceof CalendarListEvent) {
            tvHeaderTitle.setText(DateFormat.getDateInstance().format(new Date(((CalendarListEvent) card).getDtStart())));
        }
        return headerView;
    }

    @Override
    public long getHeaderId(int position) {
        CalendarListItem card = getItem(position);
        if (card instanceof CalendarListEvent) {

            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTimeInMillis(((CalendarListEvent) card).getDtStart());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        }
        return 0;
    }

    private static class CalendarListEventHolder {
        private Toolbar toolbar;
        private TextView title;
        private TextView descr;
        private TextView time;
        private TextView location;
    }

    private class CalendarListSectionHolder {
        private TextView text;
    }

}
