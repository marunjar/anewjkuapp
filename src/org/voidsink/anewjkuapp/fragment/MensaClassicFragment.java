package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.kusss.mensa.ClassicMenuLoader;
import org.voidsink.anewjkuapp.kusss.mensa.MenuLoader;

public class MensaClassicFragment extends MensaFragmentDetail {

	@Override
	protected MenuLoader createLoader() {
		return new ClassicMenuLoader();
	}

}
