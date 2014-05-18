package org.voidsink.anewjkuapp;

import java.util.ArrayList;
import java.util.List;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.calendar.CalendarUtils;
import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.LvaWithGrade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public class LvaListAdapter extends BaseExpandableListAdapter implements
		ListAdapter {

	private List<LvaWithGrade> mDoneLvas;
	private List<LvaWithGrade> mOpenLvas;
	private List<LvaWithGrade> mFailedLvas;
	private List<LvaWithGrade> mAllLvas;
	private LayoutInflater inflater;
	private Context mContext;

	public LvaListAdapter(Context context, List<LvaWithGrade> doneLvas,
			List<LvaWithGrade> openLvas, List<LvaWithGrade> failedLvas) {
		this.inflater = LayoutInflater.from(context);
		this.mContext = context;

		this.mDoneLvas = doneLvas;
		this.mOpenLvas = openLvas;
		this.mFailedLvas = failedLvas;

		this.mAllLvas = new ArrayList<LvaWithGrade>();
		this.mAllLvas.addAll(this.mDoneLvas);
		this.mAllLvas.addAll(this.mOpenLvas);
		this.mAllLvas.addAll(this.mFailedLvas);

		AppUtils.sortLVAsWithGrade(this.mAllLvas);
		AppUtils.sortLVAsWithGrade(this.mDoneLvas);
		AppUtils.sortLVAsWithGrade(this.mOpenLvas);
		AppUtils.sortLVAsWithGrade(this.mFailedLvas);
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return getLvaList(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		LvaWithGrade lva = getLvaList(groupPosition).get(childPosition);
		LvaList2ItemHolder lvaHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.lva_list_item, parent,
					false);
			lvaHolder = new LvaList2ItemHolder();

			lvaHolder.title = (TextView) convertView
					.findViewById(R.id.lva_list2_item_title);
			lvaHolder.lvaNr = (TextView) convertView
					.findViewById(R.id.lva_list2_item_lvanr);
			lvaHolder.skz = (TextView) convertView
					.findViewById(R.id.lva_list2_item_skz);
			lvaHolder.type = (TextView) convertView
					.findViewById(R.id.lva_list2_item_type);
			lvaHolder.chip = (View) convertView
					.findViewById(R.id.lva_list2_chip);
			lvaHolder.ects = (TextView) convertView
					.findViewById(R.id.lva_list2_item_ects);

			convertView.setTag(lvaHolder);
		}

		if (lvaHolder == null) {
			lvaHolder = (LvaList2ItemHolder) convertView.getTag();
		}

		ExamGrade grade = lva.getGrade();
		if (grade == null) {
			lvaHolder.chip.setBackgroundColor(CalendarUtils.COLOR_DEFAULT_LVA);
		} else {
			lvaHolder.chip.setBackgroundColor(grade.getGrade().getColor());
		}

		lvaHolder.lvaNr.setText(Integer.toString(lva.getLva().getLvaNr()));
		lvaHolder.title.setText(lva.getLva().getTitle());
		lvaHolder.skz.setText(String.format("[%s]", lva.getLva().getSKZ()));
		lvaHolder.type.setText(lva.getLva().getLvaType());
		lvaHolder.ects.setText(String.format("%.2f ECTS", lva.getLva()
				.getECTS()));

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return getLvaList(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return getLvaList(groupPosition);
	}

	private List<LvaWithGrade> getLvaList(int groupPosition) {
		switch (groupPosition) {
		case 0:
			return this.mDoneLvas;
		case 1:
			return this.mOpenLvas;
		case 2:
			return this.mFailedLvas;
		case 3:
			return this.mAllLvas;
		default:
			return null;
		}
	}

	private CharSequence getGroupTitle(int groupPosition) {
		switch (groupPosition) {
		case 0:
			return mContext.getString(R.string.lva_done);
		case 1:
			return mContext.getString(R.string.lva_open);
		case 2:
			return mContext.getString(R.string.lva_failed);
		case 3:
			return mContext.getString(R.string.lva_all);
		default:
			return null;
		}
	}

	@Override
	public int getGroupCount() {
		return 4;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		LvaList2GroupHolder groupHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.lva_list_group, parent,
					false);
			groupHolder = new LvaList2GroupHolder();
			groupHolder.term = (TextView) convertView
					.findViewById(R.id.lva_list2_group_term);
			groupHolder.ects = (TextView) convertView
					.findViewById(R.id.lva_list2_group_ects);
			convertView.setTag(groupHolder);
		}

		if (groupHolder == null) {
			groupHolder = (LvaList2GroupHolder) convertView.getTag();
		}

		groupHolder.term.setText(getGroupTitle(groupPosition));
		groupHolder.ects.setText(String.format("%.2f ECTS",
				AppUtils.getECTS(getLvaList(groupPosition))));

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		return false;
	}

	private class LvaList2GroupHolder {
		private TextView term;
		private TextView ects;
	}

	private static class LvaList2ItemHolder {
		public View chip;
		public TextView type;
		private TextView title;
		private TextView lvaNr;
		private TextView skz;
		private TextView ects;
	}

}
