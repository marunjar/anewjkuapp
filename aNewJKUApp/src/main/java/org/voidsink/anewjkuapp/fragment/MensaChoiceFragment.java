package org.voidsink.anewjkuapp.fragment;

import org.voidsink.anewjkuapp.mensa.ChoiceMenuLoader;
import org.voidsink.anewjkuapp.mensa.MenuLoader;

public class MensaChoiceFragment extends MensaFragmentDetail {

	@Override
	protected MenuLoader createLoader() {
		return new ChoiceMenuLoader();
	}

}
