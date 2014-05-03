package org.voidsink.anewjkuapp;

import android.support.v4.app.Fragment;

public class DrawerMenuItem implements DrawerItem{

	private CharSequence label;
	private int iconResID;
	private Class<? extends Fragment> startFragment;

	public DrawerMenuItem(CharSequence label, int iconResID, Class<? extends Fragment> startFragment){
		this.label = label; 
		this.iconResID = iconResID;
		this.startFragment = startFragment;
	}

	public DrawerMenuItem(CharSequence label, int iconResID){
		this(label, iconResID, null); 
	}
	
	public DrawerMenuItem(CharSequence text, Class<? extends Fragment> startFragment){
		this(text, 0, startFragment);
	}

	public DrawerMenuItem(CharSequence text){
		this(text, 0);
	}
	
	@Override
	public CharSequence getLabel() {
		return this.label;
	}
	
	@Override
	public int getIconResID() {
		return this.iconResID;
	}

	@Override
	public boolean isSectionHeader() {
		return false;
	}

	@Override
	public boolean updateActionBarTitle() {
		return true;
	}

	@Override
	public int getType() {
		return ITEM_TYPE;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}


	@Override
	public Class<? extends Fragment> getStartFragment() {
		return this.startFragment;
	}
	
}
