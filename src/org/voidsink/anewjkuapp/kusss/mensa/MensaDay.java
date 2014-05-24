package org.voidsink.anewjkuapp.kusss.mensa;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.voidsink.anewjkuapp.MensaItem;

public class MensaDay implements MensaItem {

	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat df = new SimpleDateFormat(
			"yyyy/MM/dd");

	private Date date;
	private List<MensaMenu> menus;
	private boolean isModified = false;

	public MensaDay(Date date) {
		this.date = date;
		this.menus = new ArrayList<MensaMenu>();
	}
	
	public MensaDay(JSONObject jsonDay) {
		try {
			this.date = df.parse(jsonDay.getString("date"));
		} catch (ParseException | JSONException e) {
			this.date = null;
		}
		this.menus = new ArrayList<MensaMenu>();
	}

	public void addMenu(MensaMenu menu) {
		this.menus.add(menu);
	}

	public List<MensaMenu> getMenus() {
		return this.menus;
	}

	public boolean isEmpty() {
		return (this.date == null) || (this.menus.size() == 0);
	}

	public Date getDate() {
		return this.date;
	}

	@Override
	public int getType() {
		return TYPE_DAY;
	}

	public void setDate(Date date) {
		this.date = date;
		this.isModified = true;
	}

	public boolean isModified() {
		return this.isModified;
	}
}
