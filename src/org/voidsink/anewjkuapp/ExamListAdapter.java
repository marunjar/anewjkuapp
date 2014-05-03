package org.voidsink.anewjkuapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseArrayAdapter;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ExamListAdapter extends BaseArrayAdapter<ExamListItem> {

	private static final DateFormat df = SimpleDateFormat.getDateInstance();

	private LayoutInflater inflater;

	public ExamListAdapter(Context context) {
		super(context, R.layout.exam_list_item);

		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ExamListItem item = this.getItem(position);
		if (item.isExam()) {
			view = getExamView(convertView, parent, item);
		} else {
			view = null;
		}
		return view;
	}

	private View getExamView(View convertView, ViewGroup parent,
			ExamListItem item) {
		ExamListExam eventItem = (ExamListExam) item;
		ExamListExamHolder eventItemHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.exam_list_item, parent,
					false);

			eventItemHolder = new ExamListExamHolder();

			eventItemHolder.title = (TextView) convertView
					.findViewById(R.id.exam_list_item_title);
			eventItemHolder.description = (TextView) convertView
					.findViewById(R.id.exam_list_item_description);
			eventItemHolder.info = (TextView) convertView
					.findViewById(R.id.exam_list_item_info);
			eventItemHolder.lvaNr = (TextView) convertView
					.findViewById(R.id.exam_list_item_lvanr);
			eventItemHolder.term = (TextView) convertView
					.findViewById(R.id.exam_list_item_term);
			eventItemHolder.skz = (TextView) convertView
					.findViewById(R.id.exam_list_item_skz);
			eventItemHolder.date = (TextView) convertView
					.findViewById(R.id.exam_list_item_date);
			eventItemHolder.time = (TextView) convertView
					.findViewById(R.id.exam_list_item_time);
			eventItemHolder.location = (TextView) convertView
					.findViewById(R.id.exam_list_item_location);
			eventItemHolder.chip = (View) convertView
					.findViewById(R.id.exam_list_item_chip);

			convertView.setTag(eventItemHolder);
		}

		if (eventItemHolder == null) {
			eventItemHolder = (ExamListExamHolder) convertView.getTag();
		}

		if (eventItem.mark()) {
			eventItemHolder.chip
					.setBackgroundColor(CalendarUtils.COLOR_DEFAULT_EXAM);
		} else {
			eventItemHolder.chip
					.setBackgroundColor(CalendarUtils.COLOR_DEFAULT_LVA);
		}

		eventItemHolder.title.setText(eventItem.getTitle());
		eventItemHolder.description.setText(eventItem.getDescription());
		eventItemHolder.info.setText(eventItem.getInfo());
		eventItemHolder.lvaNr.setText(Integer.toString(eventItem.getLvaNr()));
		eventItemHolder.term.setText(eventItem.getTerm());
		eventItemHolder.skz.setText(String.format("[%s]", eventItem.getSkz()));
		eventItemHolder.date.setText(df.format(eventItem.getDate()));
		eventItemHolder.time.setText(eventItem.getTime());
		eventItemHolder.location.setText(eventItem.getLocation());

		return convertView;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public int getItemViewType(int position) {
		return this.getItem(position).getType();
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	private static class ExamListExamHolder {
		public TextView term;
		public View chip;
		public TextView location;
		public TextView info;
		public TextView description;
		public TextView time;
		public TextView date;
		private TextView title;
		private TextView lvaNr;
		private TextView skz;
	}
}
