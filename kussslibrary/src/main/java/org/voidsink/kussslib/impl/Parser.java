package org.voidsink.kussslib.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Parser {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	public static final String FAILED_PARSING_DATE = "failed while parsing %s-date";
	public static final String FAILED_PARSING_START_DATE = String.format(FAILED_PARSING_DATE,"start");
	public static final String FAILED_PARSING_END_DATE = String.format(FAILED_PARSING_DATE,"end");
	
	
	public static Date parseDate(String text, String errorMsg) throws ParseException {
        if (!TextUtils.isEmpty(text)) {
            try {
				return dateFormat.parse(text);
			} catch (ParseException e) {
				if(errorMsg.trim().isEmpty()) errorMsg = e.getMessage();
				else errorMsg = errorMsg+": "+e.getMessage();
				throw new ParseException(errorMsg,e.getErrorOffset());
			}
        }
        return null;
    }
}

