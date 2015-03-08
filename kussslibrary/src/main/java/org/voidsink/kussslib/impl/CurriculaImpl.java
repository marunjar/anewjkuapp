package org.voidsink.kussslib.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.kussslib.Curricula;

public class CurriculaImpl implements Curricula {

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    private boolean isStandard;
    private int cid;
    private String title;
    private boolean steopDone;
    private boolean active;
    private String uni;
    private Date dtStart;
    private Date dtEnd;
    
    
    public CurriculaImpl(Element row) throws ParseException {
        Elements columns = row.getElementsByTag("td");
        if (columns.size() >= 8) {
            isStandard = (columns.get(0).getElementsByAttributeValue("checked", "checked").size() > 0);

            cid = Integer.parseInt(columns.get(1).text());

            title = columns.get(2).text();

            steopDone = parseSteopDone(columns.get(3).text());

            active = parseActive(columns.get(4).text());

            uni = columns.get(5).text();

			dtStart = Parser.parseDate(columns.get(6).text(), Parser.FAILED_PARSING_START_DATE);

            dtEnd = Parser.parseDate(columns.get(7).text(), Parser.FAILED_PARSING_END_DATE);
        }
    }
    
    
    public CurriculaImpl(Date dtStart, Date dtEnd) {
        this.dtStart = dtStart;
        this.dtEnd = dtEnd;
    }
    
    
    CurriculaImpl(int cid, String title, String uni,
			Date dtStart, Date dtEnd, boolean isStandard, boolean steopDone,
			boolean active) {
		this(dtStart, dtEnd);
		
		this.cid = cid;
		this.uni = uni;
		this.isStandard = isStandard;
		this.steopDone = steopDone;
		this.active = active;
	}


	private boolean parseActive(String text) {
        return text.equalsIgnoreCase("aktiv");
    }

    private boolean parseSteopDone(String text) {
        return text.equalsIgnoreCase("abgeschlossen") || text.equalsIgnoreCase("completed");
    }

    
	boolean isInitialized() {
		//TODO: isEmpty Method in TextUtils auch für Zahlenwerte wie cid?
		//return !TextUtils.isEmpty(cid) && !TextUtils.isEmpty(uni) && (dtStart != null);
		return false;
	}

	
	public boolean isStandard() {
		return isStandard;
	}


	public int getCid() {
		return cid;
	}

	
	public String getTitle() {
		return title;
	}


	public boolean isSteopDone() {
		return steopDone;
	}


	public boolean isActive() {
		return active;
	}


	public String getUniversity() {
		return uni;
	}


	public Date getDtStart() {
		return dtStart;
	}


	public Date getDtEnd() {
		return dtEnd;
	}

}
