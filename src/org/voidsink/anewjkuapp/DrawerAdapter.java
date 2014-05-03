package org.voidsink.anewjkuapp;

import org.voidsink.anewjkuapp.base.BaseArrayAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerAdapter extends BaseArrayAdapter<DrawerItem> {

	private LayoutInflater inflater;

	public DrawerAdapter(Context context) {
		super(context, R.layout.drawer_menu_item);

		this.inflater = LayoutInflater.from(context);
	}
	
	public DrawerAdapter(Context context, DrawerItem[] objects) {
		super(context, R.layout.drawer_menu_item, objects);

		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		DrawerItem menuItem = this.getItem(position);
		if (menuItem.isSectionHeader()) {
			view = getSectionView(convertView, parent, menuItem);
		} else {
			view = getItemView(convertView, parent, menuItem);
		}
		return view;
	}

	private View getItemView(View convertView, ViewGroup parent,
			DrawerItem drawerMenuItem) {
		DrawerMenuItem menuItem = (DrawerMenuItem) drawerMenuItem;
		DrawerMenuItemHolder menuItemHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.drawer_menu_item, parent,
					false);
			menuItemHolder = new DrawerMenuItemHolder();
			menuItemHolder.labelView = (TextView) convertView
					.findViewById(R.id.drawer_menu_item_label);
			menuItemHolder.iconView = (ImageView) convertView
					.findViewById(R.id.drawer_menu_item_icon);

			convertView.setTag(menuItemHolder);
		}

		if (menuItemHolder == null) {
			menuItemHolder = (DrawerMenuItemHolder) convertView.getTag();
		}

		menuItemHolder.labelView.setText(menuItem.getLabel());
		menuItemHolder.iconView.setImageResource(menuItem.getIconResID());

		return convertView;
	}

	private View getSectionView(View convertView, ViewGroup parent,
			DrawerItem menuItem) {
		DrawerMenuSection menuSection = (DrawerMenuSection) menuItem;
		DrawerMenuSectionHolder menuSectionHolder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.drawer_menu_section,
					parent, false);
			menuSectionHolder = new DrawerMenuSectionHolder();
			menuSectionHolder.labelView = (TextView) convertView
					.findViewById(R.id.navmenusection_label);
			convertView.setTag(menuSectionHolder);
		}

		if (menuSectionHolder == null) {
			menuSectionHolder = (DrawerMenuSectionHolder) convertView.getTag();
		}

		menuSectionHolder.labelView.setText(menuSection.getLabel());

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
		return getItem(position).isEnabled();
	}

	private static class DrawerMenuItemHolder {
		private TextView labelView;
		private ImageView iconView;
	}

	private class DrawerMenuSectionHolder {
		private TextView labelView;
	}

}
