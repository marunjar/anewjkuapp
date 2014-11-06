package org.voidsink.anewjkuapp;

import android.content.Context;
import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.base.BaseDrawerItem;

public class DrawerMenuItem extends BaseDrawerItem {

	private int iconResID;
	private Class<? extends Fragment> startFragment;

	public DrawerMenuItem(String label, int iconResID, Class<? extends Fragment> startFragment){
        super(label);

		this.iconResID = iconResID;
		this.startFragment = startFragment;
	}

    public DrawerMenuItem(int labelResId, int iconResID, Class<? extends Fragment> startFragment){
        super(labelResId);

        this.iconResID = iconResID;
        this.startFragment = startFragment;
    }

	public DrawerMenuItem(String label, int iconResID){
		this(label, iconResID, null); 
	}

    public DrawerMenuItem(int labelResId, int iconResID){
        this(labelResId, iconResID, null);
    }

	public DrawerMenuItem(String label, Class<? extends Fragment> startFragment){
		this(label, 0, startFragment);
	}

    public DrawerMenuItem(int labelResId, Class<? extends Fragment> startFragment){
        this(labelResId, 0, startFragment);
    }

	public DrawerMenuItem(String label){
		this(label, 0);
	}

    public DrawerMenuItem(int labelResId){
        this(labelResId, 0);
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
