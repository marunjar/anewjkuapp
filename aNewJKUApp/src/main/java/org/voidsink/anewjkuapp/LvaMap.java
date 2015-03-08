package org.voidsink.anewjkuapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import org.voidsink.anewjkuapp.kusss.KusssHelper;
import org.voidsink.anewjkuapp.kusss.Lva;
import org.voidsink.anewjkuapp.update.ImportLvaTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.Collections;

public class LvaMap {

    private static final Comparator<Lva> LvaTermComparator = new Comparator<Lva>() {
        @Override
        public int compare(Lva lhs, Lva rhs) {
            // sort lvas by term desc
            return rhs.getTerm().compareTo(lhs.getTerm());
        }
    };

    private Map<String, Lva> map;

    public LvaMap(Context context) {
        this.map = new HashMap<String, Lva>();

        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(KusssContentContract.Lva.CONTENT_URI,
                ImportLvaTask.LVA_PROJECTION, null, null,
                KusssContentContract.Lva.LVA_COL_TERM + " DESC");

        if (c != null) {
            while (c.moveToNext()) {
                Lva lva = KusssHelper.createLva(c);
                this.map.put(KusssHelper.getLvaKey(lva.getTerm(), lva.getLvaNr()), lva);
            }
            c.close();
        }

    }

    public Lva getExactLVA(String term, String lvaNr) {
        return this.map.get(KusssHelper.getLvaKey(term, lvaNr));
    }

    public Lva getLVA(String term, String lvaNr) {
        Lva lva = this.map.get(KusssHelper.getLvaKey(term, lvaNr));
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

        Collections.sort(lvas, LvaTermComparator);
        return lvas.get(0);
    }

    public List<Lva> getLVAs() {
        return new ArrayList<Lva>(this.map.values());
    }

}
