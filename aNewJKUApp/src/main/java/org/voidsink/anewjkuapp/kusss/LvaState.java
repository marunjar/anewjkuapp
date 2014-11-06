package org.voidsink.anewjkuapp.kusss;

import org.voidsink.anewjkuapp.R;

public enum LvaState {
    OPEN, DONE, ALL;

	public int getStringResID() {
		switch (this) {
		case OPEN:
            return R.string.stat_lva_open;
        case DONE:
            return R.string.stat_lva_done;
        case ALL:
            return R.string.stat_lva_all;
		default:
			return R.string.grade_type_unknown;
		}
	}
}
