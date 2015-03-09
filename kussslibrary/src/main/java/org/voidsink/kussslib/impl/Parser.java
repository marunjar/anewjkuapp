package org.voidsink.kussslib.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.voidsink.kussslib.Term;
import org.voidsink.kussslib.Term.TermType;

public class Parser {
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    public static final SimpleDateFormat dateFormatWithTime = new SimpleDateFormat("dd.MM.yyyyHH:mm");
	
    public static final String FAILED_PARSING_DATE = "failed while parsing %s-date";
	public static final String FAILED_PARSING_START_DATE = String.format(FAILED_PARSING_DATE,"start");
	public static final String FAILED_PARSING_END_DATE = String.format(FAILED_PARSING_DATE,"end");
	
    public static final String FAILED_PARSING_DATE_WITH_TIME = "failed while parsing %s-date with time";
	public static final String FAILED_PARSING_START_DATE_WITH_TIME = String.format(FAILED_PARSING_DATE_WITH_TIME,"start");
	public static final String FAILED_PARSING_END_DATE_WITH_TIME = String.format(FAILED_PARSING_DATE_WITH_TIME,"end");
	
	public static final String FAILED_PARSING_NUMERIC_VALUE = "failed while parsing %s numeric value";
	
    public static final String PATTERN_LVA_NR_WITH_DOT = "\\d{3}\\.\\w{3}";
    public static final String PATTERN_LVA_NR = "\\d{3}\\w{3}";
    public static final String PATTERN_TERM = "\\d{4}[swSW]";
    public static final String PATTERN_LVA_NR_COMMA_TERM = "\\("
            + PATTERN_LVA_NR + "," + PATTERN_TERM + "\\)";
    public static final String PATTERN_LVA_NR_SLASH_TERM = "\\("
            + PATTERN_LVA_NR + "\\/" + PATTERN_TERM + "\\)";
	
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
	
	
	public static Date parseDateWithTime(String text, String errorMsg) throws ParseException {
        if (!TextUtils.isEmpty(text)) {
            try {
				return dateFormatWithTime.parse(text);
			} catch (ParseException e) {
				if(errorMsg.trim().isEmpty()) errorMsg = e.getMessage();
				else errorMsg = errorMsg+": "+e.getMessage();
				throw new ParseException(errorMsg,e.getErrorOffset());
			}
        }
        return null;
    }
}

