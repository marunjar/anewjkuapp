package org.voidsink.anewjkuapp;

public class LvaListLva implements LvaListItem{

	private String term;
	private int lvaNr;
	private String title;
	private int skz;
	private String lvaType;

	public LvaListLva(String term, int lvaNr, String title, int skz, String lvaType) {
		this.term = term;
		this.lvaNr = lvaNr;
		this.title = title;
		this.skz = skz;
		this.lvaType = lvaType;
	}

	@Override
	public boolean isLva() {
		return true;
	}

	@Override
	public int getType() {
		return LVA_TYPE;
	}
	
	public String getTerm() {
		return this.term;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public int getLvaNr() {
		return this.lvaNr;
	}

	public int getSkz() {
		return this.skz;
	}
	
	public String getLvaType() {
		return this.lvaType;
	}
}
