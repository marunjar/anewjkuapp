package org.voidsink.anewjkuapp.calendar;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseArrayAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CalendarEventAdapter extends BaseArrayAdapter<CalendarListItem> {

	private LayoutInflater inflater;

	public static List<CalendarListItem> insertSections(
			List<CalendarListItem> objects) {
		if (objects != null) {
			CalendarListItem last = null;
			CalendarListItem item = null;
			CalendarListItem insert = null;

			int i = 0;
			while (i < objects.size()) {
				last = item;
				item = objects.get(i);
				if (last == null) {
					if (item.isEvent()) {
						insert = new CalendarListSection(
								((CalendarListEvent) item).getDtStart());
						objects.add(i, insert);
						item = insert;
					}
				} else {
					if (last.isEvent() && item.isEvent()) {
						if (!DateUtils.isSameDay(
								new Date(((CalendarListEvent) last)
										.getDtStart()),
								new Date(((CalendarListEvent) item)
										.getDtStart()))) {
							insert = new CalendarListSection(
									((CalendarListEvent) item).getDtStart());
							objects.add(i, insert);
							item = insert;
						}
					}
				}
				i++;
			}
		}
		return objects;
	}

	public CalendarEventAdapter(Context context) {
		super(context, R.layout.calendar_list_item);

		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		CalendarListItem item = this.getItem(position);
		if (item.isEvent()) {
			view = getEventView(convertView, parent, item);
		} else {
			view = getSectionView(convertView, parent, item);
		}
		return view;
	}

	private View getEventView(View convertView, ViewGroup parent,
			CalendarListItem item) {
		CalendarListEvent eventItem = (CalendarListEvent) item;
		CalendarListEventHolder eventItemHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.calendar_list_item, parent,
					false);
			eventItemHolder = new CalendarListEventHolder();
			eventItemHolder.chip = convertView
					.findViewById(R.id.calendar_list_item_chip);
			eventItemHolder.title = (TextView) convertView
					.findViewById(R.id.calendar_list_item_title);
			eventItemHolder.time = (TextView) convertView
					.findViewById(R.id.calendar_list_item_time);
			eventItemHolder.location = (TextView) convertView
					.findViewById(R.id.calendar_list_item_location);

			convertView.setTag(eventItemHolder);
		}

		if (eventItemHolder == null) {
			eventItemHolder = (CalendarListEventHolder) convertView.getTag();
		}

		eventItemHolder.chip.setBackgroundColor(eventItem.getColor());
		eventItemHolder.title.setText(eventItem.getTitle());
		eventItemHolder.time.setText(eventItem.getTime());
		eventItemHolder.location.setText(eventItem.getLocation());

		return convertView;
	}

	private View getSectionView(View convertView, ViewGroup parent,
			CalendarListItem item) {
		CalendarListSection section = (CalendarListSection) item;
		CalendarListSectionHolder sectionHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.calendar_list_section,
					parent, false);
			sectionHolder = new CalendarListSectionHolder();
			sectionHolder.text = (TextView) convertView
					.findViewById(R.id.calendar_list_section_text);
			convertView.setTag(sectionHolder);
		}

		if (sectionHolder == null) {
			sectionHolder = (CalendarListSectionHolder) convertView.getTag();
		}

		sectionHolder.text.setText(section.getText());

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
		return false;
	}

	private static class CalendarListEventHolder {
		private TextView title;
		private View chip;
		private TextView time;
		private TextView location;
	}

	private class CalendarListSectionHolder {
		private TextView text;
	}

}
