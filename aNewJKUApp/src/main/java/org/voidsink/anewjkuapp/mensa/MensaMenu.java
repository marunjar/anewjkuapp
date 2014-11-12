package org.voidsink.anewjkuapp.mensa;

import org.json.JSONException;
import org.json.JSONObject;

public class MensaMenu {

	private String name;
	private String soup;
	private String meal;
	private double price;
	private double priceBig;
	private double oehBonus;

	public MensaMenu(JSONObject jsonObject) {
		try {
			this.name = jsonObject.getString("name").trim();
			if (jsonObject.isNull("soup")) {
				this.soup = "";
			} else {
				this.soup = jsonObject.getString("soup").trim();
			}
			this.meal = jsonObject.getString("meal").trim();
			this.price = jsonObject.getInt("price") / 100f;
			this.priceBig = jsonObject.getInt("priceBig") / 100f;
			this.oehBonus = jsonObject.getInt("oeh_bonus") / 100f;

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getName() {
		return this.name;
	}

	public String getSoup() {
		return this.soup;
	}

	public String getMeal() {
		return this.meal;
	}

	public double getPrice() {
		return this.price;
	}

	public double getPriceBig() {
		return this.priceBig;
	}

	public double getOehBonus() {
		return this.oehBonus;
	}

}
