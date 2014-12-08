package org.voidsink.anewjkuapp.mensa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.voidsink.anewjkuapp.MensaItem;

public class Mensa {

	private String name;
	private List<MensaDay> days;

	public Mensa(String name) {
		this.name = name;
		this.days = new ArrayList<MensaDay>();
	}

	public void addDay(MensaDay menuDay) {
		if (menuDay != null) {
			this.days.add(menuDay);
            menuDay.setMensa(this);
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

	public MensaDay getDay(Date now) {
		for (MensaDay mensaDay : this.days) {
			if (!mensaDay.isEmpty()
					&& DateUtils.isSameDay(now, mensaDay.getDate())) {
				return mensaDay;
			}
		}
		return null;
	}

}
