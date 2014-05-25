package org.voidsink.anewjkuapp;

import android.support.v4.app.Fragment;

public interface DrawerItem {

	public static final int SECTION_TYPE = 0;
	public static final int ITEM_TYPE = 1;
	
	public String getLabel();
	public int getIconResID();
	public boolean isSectionHeader();
	public boolean updateActionBarTitle();
	public int getType();
	public boolean isEnabled();
	public Class<? extends Fragment> getStartFragment();
}
