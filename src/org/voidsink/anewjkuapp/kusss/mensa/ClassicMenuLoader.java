package org.voidsink.anewjkuapp.kusss.mensa;

public class ClassicMenuLoader extends JSONMenuLoader {

	@Override
	public String getUrl() {
		return "http://oehjku.appspot.com/rest/mensa?location=1";
	}

	@Override
	protected String getCacheKey() {
		return "Classic";
	}


}
