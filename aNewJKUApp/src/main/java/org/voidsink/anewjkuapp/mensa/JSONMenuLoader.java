package org.voidsink.anewjkuapp.mensa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.voidsink.anewjkuapp.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

public abstract class JSONMenuLoader implements MenuLoader {
	protected abstract String getUrl();

	protected abstract String getCacheKey();

	private static final String PREF_DATA_PREFIX = "MENSA_DATA_";
	private static final String PREF_DATE_PREFIX = "MENSA_DATE_";

	private String getData(Context context) {
		String result = null;
		String cacheDateKey = PREF_DATE_PREFIX + getCacheKey();
		String cacheDataKey = PREF_DATA_PREFIX + getCacheKey();

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (sp.getLong(cacheDateKey, 0) > (System.currentTimeMillis() - 6 * DateUtils.HOUR_IN_MILLIS)) {
			result = sp.getString(cacheDataKey, null);
		}

		if (result == null) {
			try {
				URL url = new URL(getUrl());
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();

				Writer writer = new StringWriter();

				char[] buffer = new char[1024];
				Reader reader = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
				result = writer.toString();

				Editor editor = sp.edit();
				editor.putString(cacheDataKey, result);
				editor.putLong(cacheDateKey, System.currentTimeMillis());
				editor.commit();

			} catch (IOException e) {
				e.printStackTrace();
				result = sp.getString(cacheDataKey, null);
			} finally {
			}
		}

		return result;
	}

	private String getLocation(Context c, int nr) {
		switch (nr) {
		case 1:
			return c.getString(R.string.mensa_title_classic);
		case 2:
			return c.getString(R.string.mensa_title_choice);
		case 3:
			return c.getString(R.string.mensa_title_khg);
		case 4:
			return c.getString(R.string.mensa_title_raab);
		default:
			return c.getString(R.string.mensa_title_unknown);
		}
	}

	public Mensa getMensa(Context context) {
		Mensa mensa = null;
		try {
			String data = getData(context);
			if (data != null) {
				JSONObject jsonData = new JSONObject(data);
				if (jsonData.getString("success").equals("true")) {
					JSONObject jsonMensa = jsonData.getJSONObject("result");

					mensa = new Mensa(getLocation(context, Integer.parseInt(jsonMensa
							.getString("location"))));
					JSONArray jsonDays = jsonMensa.getJSONArray("offers");
					for (int i = 0; i < jsonDays.length(); i++) {
						JSONObject jsonDay = jsonDays.getJSONObject(i);
						MensaDay day = new MensaDay(jsonDay);

						onNewDay(day);

						mensa.addDay(day);
						JSONArray jsonMenus = jsonDay.getJSONArray("menus");
                        			normalize(jsonMenus);
						for (int j = 0; j < jsonMenus.length(); j++) {
							day.addMenu(new MensaMenu(jsonMenus
									.getJSONObject(j), getNameFromMeal()));
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return mensa;
	}

    public static final String pricePattern = "[0-9],[0-9][0-9]";

    protected void normalize(JSONArray jsonDays) {
        for (int i = 0; i < jsonDays.length(); i++) {
            try {
                JSONObject jsonDay = jsonDays.getJSONObject(i);
                String meal = jsonDay.getString("meal").trim();
                if (meal != null) {
                    String[] ms = meal.split(pricePattern);
                    if (ms.length > 1) {
                        Log.d("meal", ms[1]);
                        JSONObject clone = new JSONObject(jsonDay.toString());
                        int startOfSecond = meal.indexOf(ms[1]);
                        jsonDay.put("meal", meal.substring(0, startOfSecond));
                        clone.put("meal", meal.substring(startOfSecond));
                        jsonDays.put(i + 1, clone);
                        i--;
                        continue;
                    }
                }
                String postfix = meal.substring(meal.length()-4);
                if(postfix.matches(pricePattern)) {
                    jsonDay.put("meal", meal.substring(0, meal.length()-4));
                    jsonDay.put("price", postfix.replace(",", ""));
                }
            } catch (JSONException e) {     Log.d("meal", e.getMessage())     ;        }
        }
    }


	protected void onNewDay(MensaDay day) {

	}

	protected boolean getNameFromMeal() {
		return false;
	}
}
