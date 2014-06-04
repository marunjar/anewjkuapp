package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.mensa.KHGMenuLoader;
import org.voidsink.anewjkuapp.mensa.MenuLoader;

public class MensaKHGFragment extends MensaFragmentDetail {

	@Override
	protected MenuLoader createLoader() {
		return new KHGMenuLoader();
	}

}
