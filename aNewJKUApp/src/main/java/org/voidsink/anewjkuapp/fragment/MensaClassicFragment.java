package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.mensa.ClassicMenuLoader;
import org.voidsink.anewjkuapp.mensa.MenuLoader;

public class MensaClassicFragment extends MensaFragmentDetail {

	@Override
	protected MenuLoader createLoader() {
		return new ClassicMenuLoader();
	}

}
