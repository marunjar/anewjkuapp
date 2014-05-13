package org.voidsink.anewjkuapp;

public class LvaListTerm implements LvaListItem {

	private String term;

	public LvaListTerm(String term) {
		this.term = term;
	}
	
	@Override
	public boolean isLva() {
		return false;
	}

	@Override
	public int getType() {
		return TYPE_TERM;
	}
	
	public String getTerm() {
		return this.term;
	}

}
