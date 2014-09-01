package org.voidsink.anewjkuapp;

import android.support.v4.app.Fragment;

public class DrawerMenuItem implements DrawerItem{

	private String label;
	private int iconResID;
	private Class<? extends Fragment> startFragment;

	public DrawerMenuItem(String label, int iconResID, Class<? extends Fragment> startFragment){
		this.label = label;
		this.iconResID = iconResID;
		this.startFragment = startFragment;
	}

	public DrawerMenuItem(String label, int iconResID){
		this(label, iconResID, null); 
	}
	
	public DrawerMenuItem(String label, Class<? extends Fragment> startFragment){
		this(label, 0, startFragment);
	}

	public DrawerMenuItem(String text){
		this(text, 0);
	}
	
	@Override
	public String getLabel() {
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
