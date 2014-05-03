package org.voidsink.anewjkuapp;

import java.util.List;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseArrayAdapter;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LvaListAdapter extends BaseArrayAdapter<LvaListItem> {

	private LayoutInflater inflater;

	public static List<LvaListItem> insertSections(List<LvaListItem> objects) {
		if (objects != null) {
			LvaListItem last = null;
			LvaListItem item = null;
			LvaListItem insert = null;

			int i = 0;
			while (i < objects.size()) {
				last = item;
				item = objects.get(i);
				if (last == null) {
					if (item.isLva()) {
						insert = new LvaListTerm(((LvaListLva) item).getTerm());
						objects.add(i, insert);
						item = insert;
					}
				} else {
					if (last.isLva() && item.isLva()) {
						if (!((LvaListLva) last).getTerm().equals(
								((LvaListLva) item).getTerm())) {
							insert = new LvaListTerm(
									((LvaListLva) item).getTerm());
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

	public LvaListAdapter(Context context) {
		super(context, R.layout.lva_list_lva);

		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		LvaListItem item = this.getItem(position);
		if (item.isLva()) {
			view = getLvaView(convertView, parent, item);
		} else {
			view = getTermView(convertView, parent, item);
		}
		return view;
	}

	private View getLvaView(View convertView, ViewGroup parent, LvaListItem item) {
		LvaListLva eventItem = (LvaListLva) item;
		LvaListLvaHolder eventItemHolder = null;

		if (convertView == null) {
			convertView = inflater
					.inflate(R.layout.lva_list_lva, parent, false);
			eventItemHolder = new LvaListLvaHolder();
			eventItemHolder.title = (TextView) convertView
					.findViewById(R.id.lva_list_lva_title);
			eventItemHolder.lvaNr = (TextView) convertView
					.findViewById(R.id.lva_list_lva_nr);
			eventItemHolder.skz = (TextView) convertView
					.findViewById(R.id.lva_list_lva_skz);
			eventItemHolder.type = (TextView) convertView
					.findViewById(R.id.lva_list_lva_type);
			eventItemHolder.chip = (View) convertView
					.findViewById(R.id.lva_list_chip);

			convertView.setTag(eventItemHolder);
		}

		if (eventItemHolder == null) {
			eventItemHolder = (LvaListLvaHolder) convertView.getTag();
		}

		eventItemHolder.chip
				.setBackgroundColor(CalendarUtils.COLOR_DEFAULT_LVA);
		eventItemHolder.lvaNr.setText(Integer.toString(eventItem.getLvaNr()));
		eventItemHolder.title.setText(eventItem.getTitle());
		eventItemHolder.skz.setText(String.format("[%s]", eventItem.getSkz()));
		eventItemHolder.type.setText(eventItem.getLvaType());

		return convertView;
	}

	private View getTermView(View convertView, ViewGroup parent,
			LvaListItem item) {
		LvaListTerm section = (LvaListTerm) item;
		LvaListTermHolder sectionHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.lva_list_term, parent,
					false);
			sectionHolder = new LvaListTermHolder();
			sectionHolder.term = (TextView) convertView
					.findViewById(R.id.lva_list_term_title);
			convertView.setTag(sectionHolder);
		}

		if (sectionHolder == null) {
			sectionHolder = (LvaListTermHolder) convertView.getTag();
		}

		sectionHolder.term.setText(section.getTerm());

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

	private static class LvaListLvaHolder {
		public View chip;
		public TextView type;
		private TextView title;
		private TextView lvaNr;
		private TextView skz;
	}

	private class LvaListTermHolder {
		private TextView term;
	}

}
