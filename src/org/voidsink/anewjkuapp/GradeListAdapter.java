package org.voidsink.anewjkuapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.voidsink.anewjkuapp.R;
import org.voidsink.anewjkuapp.base.BaseArrayAdapter;
import org.voidsink.anewjkuapp.kusss.ExamGrade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GradeListAdapter extends BaseArrayAdapter<GradeListItem> {

	private LayoutInflater inflater;
	private Context mContext;

	private static final DateFormat df = SimpleDateFormat.getDateInstance();

	public static List<GradeListItem> insertSections(List<GradeListItem> objects) {
		if (objects != null) {
			GradeListItem last = null;
			GradeListItem item = null;
			GradeListItem insert = null;

			int i = 0;
			while (i < objects.size()) {
				last = item;
				item = objects.get(i);
				if (last == null) {
					if (item.isGrade()) {
						insert = new GradeListType(
								((ExamGrade) item).getGradeType());
						objects.add(i, insert);
						item = insert;
					}
				} else {
					if (last.isGrade() && item.isGrade()) {
						if (!((ExamGrade) last).getGradeType().equals(
								((ExamGrade) item).getGradeType())) {
							insert = new GradeListType(
									((ExamGrade) item).getGradeType());
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

	public GradeListAdapter(Context context) {
		super(context, R.layout.grade_list_grade);

		this.mContext = context;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		GradeListItem item = this.getItem(position);
		if (item.isGrade()) {
			view = getGradeView(convertView, parent, item);
		} else {
			view = getTypeView(convertView, parent, item);
		}
		return view;
	}

	private View getGradeView(View convertView, ViewGroup parent,
			GradeListItem item) {
		ExamGrade gradeItem = (ExamGrade) item;
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
		if (gradeItem.getLvaNr() > 0) {
			gradeItemHolder.lvaNr
					.setText(Integer.toString(gradeItem.getLvaNr()));
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

		// gradeItemHolder.skz.setText(String.format("[%s]",
		// gradeItem.getSkz()));
		gradeItemHolder.date.setText(df.format(gradeItem.getDate()));
		gradeItemHolder.grade.setText(mContext.getString(gradeItem.getGrade()
				.getStringResID()));
		gradeItemHolder.chip
				.setBackgroundColor(gradeItem.getGrade().getColor());

		return convertView;
	}

	private View getTypeView(View convertView, ViewGroup parent,
			GradeListItem item) {
		GradeListType gradeType = (GradeListType) item;
		GradeListTypeHolder gradeTypeHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.grade_list_type, parent,
					false);
			gradeTypeHolder = new GradeListTypeHolder();
			gradeTypeHolder.type = (TextView) convertView
					.findViewById(R.id.grade_list_type_title);
			convertView.setTag(gradeTypeHolder);
		}

		if (gradeTypeHolder == null) {
			gradeTypeHolder = (GradeListTypeHolder) convertView.getTag();
		}

		gradeTypeHolder.type.setText(mContext.getString(gradeType
				.getGradeType().getStringResID()));

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
	}

}
