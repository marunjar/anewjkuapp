package org.voidsink.anewjkuapp.mensa;

import org.json.JSONException;
import org.json.JSONObject;
import org.voidsink.anewjkuapp.MensaItem;

public class MensaMenu implements MensaItem {

	private String name;
	private String soup;
	private String meal;
	private double price;
	private double priceBig;
	private double oehBonus;
    private MensaDay day;

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

    @Override
    public int getType() {
        return TYPE_MENU;
    }

    public void setDay(MensaDay day) {
        this.day = day;
    }

    public MensaDay getDay() {
        return day;
    }

    @Override
    public Mensa getMensa() {
        MensaDay day = getDay();
        if (day != null) {
            return day.getMensa();
        }
        return null;
    }
}
