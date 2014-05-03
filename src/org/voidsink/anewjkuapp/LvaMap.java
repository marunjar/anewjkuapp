package org.voidsink.anewjkuapp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.voidsink.anewjkuapp.kusss.LVA;

import edu.emory.mathcs.backport.java.util.Collections;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

public class LvaMap {

	Map<String, LVA> map;

	public LvaMap(Context context) {
		this.map = new HashMap<String, LVA>();

		ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(KusssContentContract.Lva.CONTENT_URI,
				ImportLvaTask.LVA_PROJECTION, null, null,
				KusssContentContract.Lva.LVA_COL_TERM + " DESC");

		if (c != null) {
			while (c.moveToNext()) {
				LVA lva = new LVA(c);
				this.map.put(lva.getKey(), lva);
			}
			c.close();
		}

	}

	public LVA getExactLVA(String term, int lvaNr) {
		return this.map.get(LVA.getKey(term, lvaNr));
	}

	public LVA getLVA(String term, int lvaNr) {
		LVA lva = this.map.get(LVA.getKey(term, lvaNr));
		if (lva != null) {
			return lva;
		}

		List<LVA> lvas = new ArrayList<LVA>();
		for (LVA tmp : this.map.values()) {
			if (lvaNr == tmp.getLvaNr()) {
				lvas.add(tmp);
			}
		}

		if (lvas.size() == 0) {
			return null;
		}

		Collections.sort(lvas, new Comparator<LVA>() {
			@Override
			public int compare(LVA lhs, LVA rhs) {
				// sort by term desc
				return rhs.getTerm().compareTo(lhs.getTerm());
			}
		});
		return lvas.get(0);
	}

}
