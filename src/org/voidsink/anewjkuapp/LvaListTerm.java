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
		return TERM_TYPE;
	}
	
	public String getTerm() {
		return this.term;
	}

}
