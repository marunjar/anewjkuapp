package org.voidsink.anewjkuapp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.voidsink.anewjkuapp.kusss.Lva;

import edu.emory.mathcs.backport.java.util.Collections;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

public class LvaMap {

	private Map<String, Lva> map;

	public LvaMap(Context context) {
		this.map = new HashMap<String, Lva>();

		ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(KusssContentContract.Lva.CONTENT_URI,
				ImportLvaTask.LVA_PROJECTION, null, null,
				KusssContentContract.Lva.LVA_COL_TERM + " DESC");

		if (c != null) {
			while (c.moveToNext()) {
				Lva lva = new Lva(c);
				this.map.put(lva.getKey(), lva);
			}
			c.close();
		}

	}

	public Lva getExactLVA(String term, String lvaNr) {
		return this.map.get(Lva.getKey(term, lvaNr));
	}

	public Lva getLVA(String term, String lvaNr) {
		Lva lva = this.map.get(Lva.getKey(term, lvaNr));
		if (lva != null) {
			return lva;
		}

		List<Lva> lvas = new ArrayList<Lva>();
		for (Lva tmp : this.map.values()) {
			if (lvaNr.equals(tmp.getLvaNr())) {
				lvas.add(tmp);
			}
		}

		if (lvas.size() == 0) {
			return null;
		}

		Collections.sort(lvas, new Comparator<Lva>() {
			@Override
			public int compare(Lva lhs, Lva rhs) {
				// sort by term desc
				return rhs.getTerm().compareTo(lhs.getTerm());
			}
		});
		return lvas.get(0);
	}
	
	public List<Lva> getLVAs() {
		return new ArrayList<Lva>(this.map.values());
	}

}
