package org.voidsink.anewjkuapp.kusss.mensa;

public class RaabMenuLoader extends JSONMenuLoader {

	@Override
	public String getUrl() {
		return "http://oehjku.appspot.com/rest/mensa?location=4";
	}

	@Override
	protected String getCacheKey() {
		return "Raab";
	}

}
