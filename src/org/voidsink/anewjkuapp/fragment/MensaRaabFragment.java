package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.kusss.mensa.MenuLoader;
import org.voidsink.anewjkuapp.kusss.mensa.RaabMenuLoader;

public class MensaRaabFragment extends MensaFragmentDetail {

	@Override
	protected MenuLoader createLoader() {
		return new RaabMenuLoader();
	}

}
