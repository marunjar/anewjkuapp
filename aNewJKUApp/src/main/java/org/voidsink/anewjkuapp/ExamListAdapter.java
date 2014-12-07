package org.voidsink.anewjkuapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.voidsink.anewjkuapp.base.GridWithHeaderAdapter;
import org.voidsink.anewjkuapp.base.ListWithHeaderAdapter;
import org.voidsink.anewjkuapp.calendar.CalendarListEvent;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.kusss.KusssHandler;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ExamListAdapter extends GridWithHeaderAdapter<ExamListExam> {

	private static final DateFormat df = SimpleDateFormat.getDateInstance();

	public ExamListAdapter(Context context) {
		super(context, R.layout.exam_list_item);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ExamListExam item = this.getItem(position);
        if (item == null) {
            return null;
        }
		return getExamView(convertView, parent, item);
	}

	private View getExamView(View convertView, ViewGroup parent,
			ExamListExam item) {
		ExamListExamHolder eventItemHolder = null;
        final ExamListExam exam = item;

        if (convertView == null) {
            final LayoutInflater mInflater = LayoutInflater.from(getContext());
			convertView = mInflater.inflate(R.layout.exam_list_item, parent,
					false);

			eventItemHolder = new ExamListExamHolder();

            eventItemHolder.toolbar = (Toolbar) convertView.findViewById(R.id.exam_list_item_toolbar);
            eventItemHolder.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.menu_exam_register: {
                            KusssHandler.getInstance().showExamInBrowser(getContext(), exam.getLvaNr());
                            return true;
                        }
                    }
                    return false;
                }
            });
            eventItemHolder.toolbar.inflateMenu(R.menu.exam_card_popup_menu);

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
			eventItemHolder.time = (TextView) convertView
					.findViewById(R.id.exam_list_item_time);
			eventItemHolder.location = (TextView) convertView
					.findViewById(R.id.exam_list_item_location);
			eventItemHolder.chip = (View) convertView
					.findViewById(R.id.empty_chip_background);

			convertView.setTag(eventItemHolder);
		}

		if (eventItemHolder == null) {
			eventItemHolder = (ExamListExamHolder) convertView.getTag();
		}

        if (eventItemHolder.chip != null) {
            if (exam.mark()) {
                eventItemHolder.chip
                        .setBackgroundColor(CalendarUtils.COLOR_DEFAULT_EXAM);
            } else {
                eventItemHolder.chip
                        .setBackgroundColor(CalendarUtils.COLOR_DEFAULT_LVA);
            }
        }

		eventItemHolder.title.setText(exam.getTitle());
		if (!exam.getDescription().isEmpty()) {
			eventItemHolder.description.setText(exam.getDescription());
			eventItemHolder.description.setVisibility(View.VISIBLE);
		} else {
			eventItemHolder.description.setVisibility(View.GONE);
		}
		if (!exam.getInfo().isEmpty()) {
			eventItemHolder.info.setText(exam.getInfo());
			eventItemHolder.info.setVisibility(View.VISIBLE);
		} else {
			eventItemHolder.info.setVisibility(View.GONE);
		}
		eventItemHolder.lvaNr.setText(exam.getLvaNr());
		eventItemHolder.term.setText(exam.getTerm());
		eventItemHolder.skz.setText(String.format("[%s]", exam.getSkz()));
		eventItemHolder.time.setText(exam.getTime());
		eventItemHolder.location.setText(exam.getLocation());

		return convertView;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
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
		private TextView title;
		private TextView lvaNr;
		private TextView skz;
        public Toolbar toolbar;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
        // Build your custom HeaderView
        final LayoutInflater mInflater = LayoutInflater.from(getContext());
        final View headerView = mInflater.inflate(R.layout.list_header, null);
        final TextView tvHeaderTitle = (TextView) headerView.findViewById(R.id.list_header_text);

        ExamListExam exam = getItem(position);
        if (exam != null) {
            tvHeaderTitle.setText(DateFormat.getDateInstance().format(exam.getDate()));
        }

        return headerView;
    }

    @Override
    public long getHeaderId(int position) {
        ExamListExam exam = getItem(position);
        if (exam != null) {
            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTimeInMillis(exam.getDate().getTime());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        }
        return 0;
    }
}
