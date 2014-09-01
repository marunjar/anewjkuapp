package org.voidsink.anewjkuapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.voidsink.anewjkuapp.kusss.ExamGrade;
import org.voidsink.anewjkuapp.kusss.GradeType;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public class GradeListAdapter extends BaseExpandableListAdapter implements
		ListAdapter {

	private LayoutInflater inflater;
	private Context mContext;

	private HashMap<GradeType, List<ExamGrade>> mGrades;
	private List<ExamGrade> mAllGrades;

	private static final DateFormat df = SimpleDateFormat.getDateInstance();

	public GradeListAdapter(Context context) {
		super();

		this.mContext = context;
		this.inflater = LayoutInflater.from(context);
		this.mGrades = new HashMap<GradeType, List<ExamGrade>>();
		this.mAllGrades = new ArrayList<ExamGrade>();
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	private static class GradeListGradeHolder {
		public TextView grade;
		public TextView date;
		public View chip;
		public TextView term;
		private TextView title;
		private TextView lvaNr;
		// private TextView skz;
	}

	private class GradeListTypeHolder {
		private TextView type;
		private TextView avgGrade;

	}

	@Override
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
		return 2;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	public GradeType getGradeTypeByGroupPosition(int groupPosition) {
		return this.mGrades.keySet().toArray(new GradeType[] {})[groupPosition];
	}

	public List<ExamGrade> getGradesByGroupPosition(int groupPosition) {
		if (groupPosition < this.mGrades.size()) {
			return this.mGrades.get(getGradeTypeByGroupPosition(groupPosition));
		}
		return this.mAllGrades;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		List<ExamGrade> gradesByType = getGradesByGroupPosition(groupPosition);
		if (gradesByType == null) {
			return null;
		}
		return gradesByType.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		ExamGrade gradeItem = (ExamGrade) getChild(groupPosition, childPosition);
		GradeListGradeHolder gradeItemHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.grade_list_grade, parent,
					false);
			gradeItemHolder = new GradeListGradeHolder();
			gradeItemHolder.title = (TextView) convertView
					.findViewById(R.id.grade_list_grade_title);
			gradeItemHolder.lvaNr = (TextView) convertView
					.findViewById(R.id.grade_list_grade_lvanr);
			gradeItemHolder.term = (TextView) convertView
					.findViewById(R.id.grade_list_grade_term);
			// gradeItemHolder.skz = (TextView) convertView
			// .findViewById(R.id.grade_list_grade_skz);
			gradeItemHolder.date = (TextView) convertView
					.findViewById(R.id.grade_list_grade_date);
			gradeItemHolder.grade = (TextView) convertView
					.findViewById(R.id.grade_list_grade_grade);
			gradeItemHolder.chip = (View) convertView
					.findViewById(R.id.grade_list_grade_chip);

			convertView.setTag(gradeItemHolder);
		}

		if (gradeItemHolder == null) {
			gradeItemHolder = (GradeListGradeHolder) convertView.getTag();
		}

		gradeItemHolder.title.setText(gradeItem.getTitle());
		if (!gradeItem.getLvaNr().isEmpty()) {
			gradeItemHolder.lvaNr
					.setText(gradeItem.getLvaNr());
			gradeItemHolder.lvaNr.setVisibility(View.VISIBLE);
		} else {
			gradeItemHolder.lvaNr.setVisibility(View.GONE);
		}

		if (!gradeItem.getTerm().isEmpty()) {
			gradeItemHolder.term.setText(gradeItem.getTerm());
			gradeItemHolder.term.setVisibility(View.VISIBLE);
		} else {
			gradeItemHolder.term.setVisibility(View.GONE);
		}

		gradeItemHolder.date.setText(df.format(gradeItem.getDate()));
		gradeItemHolder.grade.setText(mContext.getString(gradeItem.getGrade()
				.getStringResID()));
		gradeItemHolder.chip
				.setBackgroundColor(gradeItem.getGrade().getColor());

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		List<ExamGrade> gradesByType = getGradesByGroupPosition(groupPosition);
		if (gradesByType == null) {
			return 0;
		}
		return gradesByType.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return getGradesByGroupPosition(groupPosition);
	}

	@Override
	public int getGroupCount() {
		int count = this.mGrades.size();
		if (count > 1) {
			count++;
		}
		return count++;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GradeType gradeType = GradeType.ALL;
		if (groupPosition < this.mGrades.size()) {
			gradeType = getGradeTypeByGroupPosition(groupPosition);
		}
		GradeListTypeHolder gradeTypeHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.grade_list_type, parent,
					false);
			gradeTypeHolder = new GradeListTypeHolder();
			gradeTypeHolder.type = (TextView) convertView
					.findViewById(R.id.grade_list_type_title);
			gradeTypeHolder.avgGrade = (TextView) convertView
					.findViewById(R.id.grade_list_type_avg_grade);

			convertView.setTag(gradeTypeHolder);
		}

		if (getChildrenCount(groupPosition) > 0) {
			convertView.setVisibility(View.VISIBLE);
		} else {
			convertView.setVisibility(View.GONE);
		}

		if (gradeTypeHolder == null) {
			gradeTypeHolder = (GradeListTypeHolder) convertView.getTag();
		}

		gradeTypeHolder.type.setText(mContext.getString(gradeType
				.getStringResID()));
		double avgGrade = AppUtils.getAvgGrade(
				getGradesByGroupPosition(groupPosition), false);
		gradeTypeHolder.avgGrade.setText(String.format("avg %.2f", avgGrade));

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	public void clear() {
		this.mGrades.clear();
		this.notifyDataSetInvalidated();
	}

	public void addAll(List<ExamGrade> listItems) {
		for (ExamGrade listItem : listItems) {
			add(listItem);
		}
		this.notifyDataSetChanged();
	}

	public void add(ExamGrade grade) {
		List<ExamGrade> gradesByType = this.mGrades.get(grade.getGradeType());
		if (gradesByType == null) {
			gradesByType = new ArrayList<ExamGrade>();
			this.mGrades.put(grade.getGradeType(), gradesByType);
		}
		gradesByType.add(grade);

		this.mAllGrades.add(grade);
	}

}
