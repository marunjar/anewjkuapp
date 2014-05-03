package org.voidsink.anewjkuapp.kusss.mensa;

import java.util.ArrayList;
import java.util.List;

import org.voidsink.anewjkuapp.MensaItem;

public class Mensa implements MensaItem{

	private String name;
	private List<MensaDay> days;

	public Mensa(String name) {
		this.name = name;
		this.days = new ArrayList<MensaDay>();
	}

	public void addDay(MensaDay menuDay) {
		if (menuDay != null) {
			this.days.add(menuDay);
		}
	}

	public boolean isEmpty() {
		return this.days.size() == 0;
	}

	public List<MensaDay> getDays() {
		return this.days;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public int getType() {
		return TYPE_MENSA;
	}

}
