package org.voidsink.anewjkuapp;

import android.content.Context;
import android.support.v4.app.Fragment;

import org.voidsink.anewjkuapp.base.BaseDrawerItem;

public class DrawerMenuSection extends BaseDrawerItem {

    public DrawerMenuSection(String label) {
        super(label);
    }

    public DrawerMenuSection(int labelResId) {
        super(labelResId);
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
