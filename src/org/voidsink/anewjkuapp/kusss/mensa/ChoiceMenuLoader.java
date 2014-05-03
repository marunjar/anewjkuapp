package org.voidsink.anewjkuapp.kusss.mensa;

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
	protected String getCacheKey() {
		return "Choice";
	}

}
