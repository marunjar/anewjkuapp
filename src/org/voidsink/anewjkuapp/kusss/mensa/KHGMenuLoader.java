package org.voidsink.anewjkuapp.kusss.mensa;

public class KHGMenuLoader extends JSONMenuLoader {

	@Override
	public String getUrl() {
		return "http://oehjku.appspot.com/rest/mensa?location=3";
	}

	@Override
	protected String getCacheKey() {
		return "KHG";
	}


}
