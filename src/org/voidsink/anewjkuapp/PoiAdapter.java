package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.base.BaseArrayAdapter;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PoiAdapter extends BaseArrayAdapter<Poi> {

	public PoiAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public PoiAdapter(Context context) {
		this(context, android.R.layout.simple_list_item_2);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PoiHolder poiHolder = null;

		LayoutInflater inflater = LayoutInflater.from(new ContextThemeWrapper(getContext(), R.style.Theme_Dialog));
		
		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_list_item_2,
					parent, false);
			poiHolder = new PoiHolder();
			poiHolder.text1 = (TextView) convertView
					.findViewById(android.R.id.text1);
			poiHolder.text2 = (TextView) convertView
					.findViewById(android.R.id.text2);
			convertView.setTag(poiHolder);
		}

		if (poiHolder == null) {
			poiHolder = (PoiHolder) convertView.getTag();
		}

		Poi poi = getItem(position);
		poiHolder.text1.setText(poi.getName());
		poiHolder.text2.setText(poi.getDescr());

		return convertView;
	}

	private final class PoiHolder {
		TextView text1;
		TextView text2;
	}
}
