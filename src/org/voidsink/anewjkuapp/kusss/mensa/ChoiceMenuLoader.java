package org.voidsink.anewjkuapp.kusss.mensa;

import java.util.Date;

public class ChoiceMenuLoader extends JSONMenuLoader {

	@Override
	public String getUrl() {
		return "http://oehjku.appspot.com/rest/mensa?location=2";
	}

	@Override
	protected boolean getNameFromMeal() {
		return true;
	}
	
	@Override
	protected void onNewDay(MensaDay day) {
		if (day.getDate() == null) {
			day.setDate(new Date());
		}
	}

	@Override
	protected String getCacheKey() {
		return "Choice";
	}

}
