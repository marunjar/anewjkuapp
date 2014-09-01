package org.voidsink.anewjkuapp;

import android.support.v4.app.Fragment;

public class DrawerMenuSection implements DrawerItem{

	private String label;

	public DrawerMenuSection(String label){
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return this.label;
	}
	
	@Override
	public int getIconResID() {
		return 0;
	}
	
	@Override
	public boolean isSectionHeader() {
		return true;
	}

	@Override
	public boolean updateActionBarTitle() {
		return false;
	}

	@Override
	public int getType() {
		return SECTION_TYPE;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public Class<? extends Fragment> getStartFragment() {
		return null;
	}
	
}
