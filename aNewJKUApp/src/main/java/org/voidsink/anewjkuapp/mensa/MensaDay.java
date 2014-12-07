package org.voidsink.anewjkuapp.mensa;

import org.json.JSONException;
import org.json.JSONObject;
import org.voidsink.anewjkuapp.MensaItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MensaDay {

    private Date date;
    private List<MensaMenu> menus;
    private boolean isModified = false;
    private Mensa mensa;

    public MensaDay(Date date) {
        this.date = date;
        this.menus = new ArrayList<>();
    }

    public MensaDay(JSONObject jsonDay) {
        try {
            final SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            this.date = df.parse(jsonDay.getString("date"));
        } catch (ParseException | JSONException e) {
            this.date = null;
        }
        this.menus = new ArrayList<>();
    }

    public void addMenu(MensaMenu menu) {
        this.menus.add(menu);
        menu.setDay(this);
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

    public void setDate(Date date) {
        this.date = date;
        this.isModified = true;
    }

    public boolean isModified() {
        return this.isModified;
    }

    public void setMensa(Mensa mensa) {
        this.mensa = mensa;
    }

    public Mensa getMensa() {
        return mensa;
    }
}
