package org.voidsink.anewjkuapp.kusss;

import org.voidsink.anewjkuapp.R;

public enum LvaState {
    OPEN, DONE, ALL;

	public int getStringResID() {
		switch (this) {
		case OPEN:
            return R.string.lva_state_open;
        case DONE:
            return R.string.lva_state_done;
        case ALL:
            return R.string.lva_state_all;
		default:
			return R.string.lva_state_unknown;
		}
	}

    public int getStringResIDExt() {
        switch (this) {
            case OPEN:
                return R.string.lva_state_ext_open;
            case DONE:
                return R.string.lva_state_ext_done;
            case ALL:
                return R.string.lva_state_ext_all;
            default:
                return R.string.lva_state_unknown;
        }
    }
}
